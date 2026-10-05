package net.dawson.adorablehamsterpets.entity.client;

import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import net.dawson.adorablehamsterpets.AdorableHamsterPets;
import net.dawson.adorablehamsterpets.AdorableHamsterPetsClient;
import net.dawson.adorablehamsterpets.config.Configs;
import net.dawson.adorablehamsterpets.entity.custom.HamsterEntity;
import net.dawson.adorablehamsterpets.item.ModItems;
import net.dawson.adorablehamsterpets.item.custom.HamsterArmorItem;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

/**
 * GeckoLib 5 model for the hamster.
 *
 * <p>GeckoLib 5 renders from a render state instead of the entity itself, so all per-frame bone tweaks
 * (scaling, cheek pouches, flowers, acorn hat, pitch) are captured into a {@link BoneData} record on the
 * main thread ({@link #capture}) and applied later to the bone snapshots ({@link #applyBones}).
 */
public class HamsterModel extends GeoModel<HamsterEntity> {

    private static final float ADULT_SCALE = 0.8f;
    private static final float ADULT_HEAD_SCALE = 1.0f;
    private static final float BABY_SCALE = 0.5f;
    private static final float BABY_HEAD_SCALE = 1.2f;

    private static final Identifier MODEL_RESOURCE = Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, "hamster");
    private static final Identifier ANIMATION_RESOURCE = Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, "anim_hamster");
    private static final Identifier FALLBACK_TEXTURE = Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, "textures/entity/hamster/fur_base_pattern/fur_pattern.png");

    private static final Set<String> PERFORMANCE_MODE_BONES = Set.of("root", "body_parent", "body_child");

    /** The dynamically composited hamster texture (see HamsterTextureUtil). */
    public static final DataTicket<Identifier> TEXTURE = DataTicket.create("adorablehamsterpets_texture", Identifier.class);
    /** Captured per-frame bone adjustments. */
    public static final DataTicket<BoneData> BONE_DATA = DataTicket.create("adorablehamsterpets_bone_data", BoneData.class);

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL_RESOURCE;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        Identifier texture = renderState.getGeckolibData(TEXTURE);
        return texture != null ? texture : FALLBACK_TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(HamsterEntity animatable) {
        return ANIMATION_RESOURCE;
    }

    /** Snapshot of everything the bone adjustments need, captured on the main thread. */
    public record BoneData(boolean performanceMode, boolean noAi,
                           int flowerType, boolean useArmorFlowers,
                           boolean leftCheekFull, boolean rightCheekFull,
                           boolean hideRightEar, boolean showAcornHat,
                           float baseScale, float scaleY, float headScale,
                           float pitch, boolean moonwalking) {}

    public static BoneData capture(HamsterEntity entity, float partialTick) {
        boolean performanceMode = AdorableHamsterPetsClient.isPerformanceModeEnabled;

        // --- Equipment State ---
        ItemStack armorStack = entity.getArmorStack();
        boolean isArmorVisible = Configs.AHP_MAIN.enableArmorVisuals
                && entity.isArmorVisible()
                && !armorStack.isEmpty()
                && armorStack.getItem() instanceof HamsterArmorItem;

        // --- Pink Petal Logic ---
        int flowerType = entity.getEntityData().get(HamsterEntity.FLOWER_POS);
        boolean useArmorFlowers = isArmorVisible && Configs.AHP_MAIN.renderFlowersWithArmor.get();

        // --- Armor/Accessory Logic ---
        boolean hideEar = false;
        boolean showHat = false;
        if (entity.getAccessoryStack().is(ModItems.ACORN_HAT.get())) {
            hideEar = true;
            showHat = true;
        }
        if (!showHat && isArmorVisible
                && armorStack.is(ModItems.HAMSTER_ARMOR_ACORN.get())
                && Configs.AHP_MAIN.renderAcornHat.get()) {
            hideEar = true;
            showHat = true;
        }

        // --- Scaling ---
        float ageProgress = 1.0f;
        if (entity.isBaby()) {
            int exactAge = entity.getEntityData().get(HamsterEntity.EXACT_AGE);
            ageProgress = 1.0f - (Math.abs(exactAge) / 24000.0f);
        }
        float baseScale = Mth.lerp(ageProgress, BABY_SCALE, ADULT_SCALE);
        float headScale = Mth.lerp(ageProgress, BABY_HEAD_SCALE, ADULT_HEAD_SCALE);
        float scaleY = entity.isShoulderPet() ? baseScale * entity.dynamicScaleY : baseScale;

        // --- Dynamic Pitch Rotation ---
        float pitchOffset = 0.0f;
        if (entity.clientFluteMountFlight) {
            pitchOffset = Mth.lerp(partialTick, entity.prevClientFluteMountPitch, entity.clientFluteMountPitch);
        } else if (entity.isFluteMountFlight()) {
            pitchOffset = entity.getFluteMountPitch();
        } else if (entity.isProjectileDummy) {
            Vec3 velocity = entity.getDeltaMovement();
            double horizontalSpeed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
            pitchOffset = (float) Math.atan2(velocity.y, horizontalSpeed);
        } else if (entity.isInWater() || entity.isInLava()) {
            pitchOffset = Mth.lerp(partialTick, entity.prevClientSwimPitch, entity.clientSwimPitch);
        } else if (entity.clientFallPitchProgress > 0.0f || entity.prevClientFallPitchProgress > 0.0f) {
            float lerpedProgress = Mth.lerp(partialTick, entity.prevClientFallPitchProgress, entity.clientFallPitchProgress);
            float interpolated = (1.0f - Mth.cos(lerpedProgress * (float) Math.PI)) * 0.5f;
            pitchOffset = (float) (-Math.PI / 2.0) * interpolated;
        }

        return new BoneData(performanceMode, entity.isNoAi(), flowerType, useArmorFlowers,
                entity.isLeftCheekFull(), entity.isRightCheekFull(), hideEar, showHat,
                baseScale, scaleY, headScale, pitchOffset, entity.isMoonwalking());
    }

    /** Applies the captured adjustments to this frame's bone snapshots. */
    public static void applyBones(BakedGeoModel model, BoneData data, BoneSnapshots snapshots) {
        // --- Performance Mode ---
        if (data.performanceMode()) {
            for (GeoBone bone : model.topLevelBones()) {
                hideRecursively(bone, snapshots);
            }
            return;
        }

        // --- Statue / AI Disabled: keep eyes open ---
        snapshots.ifPresent("upper_eye_lid", b -> b.skipRender(data.noAi()));
        snapshots.ifPresent("lower_eye_lid", b -> b.skipRender(data.noAi()));

        // --- Pink Petals ---
        int f = data.flowerType();
        boolean armor = data.useArmorFlowers();
        snapshots.ifPresent("flower_head_no_armor", b -> b.skipRender(f != 1 || armor));
        snapshots.ifPresent("flower_side_no_armor", b -> b.skipRender(f != 2 || armor));
        snapshots.ifPresent("flower_lower_back_no_armor", b -> b.skipRender(f != 3 || armor));
        snapshots.ifPresent("flower_head_with_armor", b -> b.skipRender(f != 1 || !armor));
        snapshots.ifPresent("flower_side_with_armor", b -> b.skipRender(f != 2 || !armor));
        snapshots.ifPresent("flower_lower_back_with_armor", b -> b.skipRender(f != 3 || !armor));

        // --- Cheek Pouches ---
        snapshots.ifPresent("left_cheek_deflated", b -> b.skipRender(data.leftCheekFull()));
        snapshots.ifPresent("left_cheek_inflated", b -> b.skipRender(!data.leftCheekFull()));
        snapshots.ifPresent("right_cheek_deflated", b -> b.skipRender(data.rightCheekFull()));
        snapshots.ifPresent("right_cheek_inflated", b -> b.skipRender(!data.rightCheekFull()));

        // --- Acorn Hat ---
        snapshots.ifPresent("right_ear", b -> b.skipRender(data.hideRightEar()));
        snapshots.ifPresent("acorn_hat", b -> b.skipRender(!data.showAcornHat()));

        // --- Scaling & Rotation ---
        snapshots.ifPresent("root", b -> {
            b.setScale(data.baseScale(), data.scaleY(), data.baseScale());
            b.setRotX(data.pitch());
            b.setRotY(data.moonwalking() ? (float) Math.PI : 0.0f);
        });
        snapshots.ifPresent("head_parent", b -> b.setScale(data.headScale(), data.headScale(), data.headScale()));
    }

    private static void hideRecursively(GeoBone bone, BoneSnapshots snapshots) {
        BoneSnapshot snapshot = snapshots.get(bone.name()).orElse(null);
        if (snapshot != null) {
            snapshot.skipRender(!PERFORMANCE_MODE_BONES.contains(bone.name()));
        }
        for (GeoBone child : bone.children()) {
            hideRecursively(child, snapshots);
        }
    }
}
