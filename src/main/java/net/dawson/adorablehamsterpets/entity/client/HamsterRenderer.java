package net.dawson.adorablehamsterpets.entity.client;

import java.lang.Math;

import dev.architectury.networking.NetworkManager;
import net.dawson.adorablehamsterpets.AdorableHamsterPetsClient;
import net.dawson.adorablehamsterpets.config.Configs;
import net.dawson.adorablehamsterpets.entity.custom.HamsterEntity;
import net.dawson.adorablehamsterpets.networking.payload.HamsterAnimationSoundPayload;
import net.dawson.adorablehamsterpets.sound.ModSounds;
import net.dawson.adorablehamsterpets.util.HamsterMouthItemOffsets;
import net.dawson.adorablehamsterpets.util.HamsterRenderUtil;
import net.dawson.adorablehamsterpets.util.HamsterRidingUtil;
import net.dawson.adorablehamsterpets.util.HamsterTextureUtil;
import net.dawson.adorablehamsterpets.util.RedstoneFeverUtil;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import com.mojang.math.Axis;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class HamsterRenderer extends GeoEntityRenderer<HamsterEntity, LivingEntityRenderState> {

    // Kept for compatibility with mixins/other classes that check these flags
    public static final ThreadLocal<Boolean> IS_RENDERING_PASSENGER = ThreadLocal.withInitial(() -> false);
    public static final ThreadLocal<Boolean> IS_RENDERING_IN_GUI = ThreadLocal.withInitial(() -> false);

    /** Per-frame pose offsets (Redstone Fever tremor, ground surface offset). */
    public record PoseExtras(double tremorX, double tremorZ, double tremorRoll, float groundYOffset) {}
    public static final DataTicket<PoseExtras> POSE_EXTRAS = DataTicket.create("adorablehamsterpets_pose_extras", PoseExtras.class);
    public static final DataTicket<Boolean> FEVER_EYES = DataTicket.create("adorablehamsterpets_fever_eyes", Boolean.class);
    /** Backward shadow offset while rolling; read by EntityRenderDispatcherMixin when the shadow is submitted. */
    /** Set on the render state of any entity riding a hamster; its vanilla render is replaced by HamsterPassengerLayer. */
    public static final DataTicket<Boolean> RIDING_HAMSTER = DataTicket.create("adorablehamsterpets_riding_hamster", Boolean.class);
    public static final DataTicket<Double> ROLL_SHADOW_OFFSET = DataTicket.create("adorablehamsterpets_roll_shadow_offset", Double.class);

    private static final float ADULT_SHADOW_RADIUS = 0.2F;

    public HamsterRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new HamsterModel());
        withRenderLayer(new RedstoneFeverEyesRenderLayer(this));
        withRenderLayer(new HamsterMouthItemLayer(ctx, this));
        withRenderLayer(new HamsterPassengerLayer(this));
        this.shadowRadius = ADULT_SHADOW_RADIUS;
    }

    @Override
    public boolean shouldShowName(HamsterEntity entity, double distToCameraSq) {
        if (IS_RENDERING_IN_GUI.get()) {
            return false;
        }
        return super.shouldShowName(entity, distToCameraSq);
    }

    @Override
    protected float getShadowRadius(LivingEntityRenderState state) {
        return state.isBaby ? ADULT_SHADOW_RADIUS * 0.5f : ADULT_SHADOW_RADIUS;
    }

    @Override
    public void addRenderData(HamsterEntity entity, @Nullable Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
        // --- 1. Report to Client-Side Tracker ---
        AdorableHamsterPetsClient.onHamsterRendered(entity.getId());

        // --- 2. Texture & Bone Data ---
        renderState.addGeckolibData(HamsterModel.TEXTURE, HamsterTextureUtil.getHamsterTexture(entity));
        renderState.addGeckolibData(HamsterModel.BONE_DATA, HamsterModel.capture(entity, partialTick));
        renderState.addGeckolibData(FEVER_EYES, entity.hasRedstoneFever() && !entity.isInvisible());
        renderState.addGeckolibData(ROLL_SHADOW_OFFSET, entity.getRollShadowOffset(partialTick));

        // --- 3. Redstone Fever Tremor ---
        double tremorX = 0, tremorZ = 0, roll = 0;
        if (entity.hasRedstoneFever() && !entity.isRedstoneFeverBurstActive() && !IS_RENDERING_IN_GUI.get()) {
            double baseAmplitude = 0.000D;
            double spikeAmplitude = 0.015D;
            double horizontalXFrequency = 4.73D;
            double horizontalZFrequency = 4.39D;
            double severity = entity.getSynchronizedRedstoneFeverSeverity();
            double renderTime = entity.level().getGameTime() + partialTick;
            double spike = RedstoneFeverUtil.getTremorSpike(renderTime, entity.getUUID()) * spikeAmplitude;
            double finalAmplitude = (baseAmplitude + spike) * severity;
            double amplitudePulse = (baseAmplitude + spike) / (baseAmplitude + spikeAmplitude);
            double entityPhase = entity.getUUID().hashCode() * 0.61803398875D;
            tremorX = Math.sin(renderTime * horizontalXFrequency + entityPhase) * finalAmplitude;
            tremorZ = Math.cos(renderTime * horizontalZFrequency + entityPhase) * finalAmplitude;
            roll = Math.toRadians(2.5D) * amplitudePulse * severity * Math.sin(renderTime * 4.17D + entityPhase);
        }

        // --- 4. Smooth Ground Surface Height Adjustment ---
        if (IS_RENDERING_IN_GUI.get() || entity.isShoulderPet() || entity.isProjectileDummy) {
            entity.renderedGroundYOffset = 0.0F;
        } else {
            float targetYOffset = HamsterRenderUtil.getGroundSurfaceOffset(entity);
            entity.renderedGroundYOffset += (targetYOffset - entity.renderedGroundYOffset) * 0.15F;
        }
        renderState.addGeckolibData(POSE_EXTRAS, new PoseExtras(tremorX, tremorZ, roll, entity.renderedGroundYOffset));

        // --- 5. Keyframe Particles & Sounds (handled on the main thread) ---
        if (entity.particleEffectId != null) {
            handleParticleKeyframes(entity);
        }
        if (entity.soundEffectId != null) {
            handleSoundKeyframes(entity);
        }
    }

    @Override
    public void adjustRenderPose(RenderPassInfo<LivingEntityRenderState> renderPassInfo) {
        PoseExtras extras = renderPassInfo.getGeckolibData(POSE_EXTRAS);
        if (extras != null) {
            PoseStack poseStack = renderPassInfo.poseStack();
            poseStack.translate(extras.tremorX(), extras.groundYOffset(), extras.tremorZ());
            if (extras.tremorRoll() != 0) {
                poseStack.mulPose(Axis.ZP.rotation((float) extras.tremorRoll()));
            }
        }
        super.adjustRenderPose(renderPassInfo);
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<LivingEntityRenderState> renderPassInfo, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(renderPassInfo, snapshots);
        HamsterModel.BoneData data = renderPassInfo.getGeckolibData(HamsterModel.BONE_DATA);
        if (data != null) {
            HamsterModel.applyBones(renderPassInfo.model(), data, snapshots);
        }
    }

    /* ──────────────────────────────────────────────────────────────────────────────
     *        Keyframe helpers
     * ────────────────────────────────────────────────────────────────────────────*/

    // GeckoLib 5 no longer exposes world-space bone positions on the main thread, so the
    // particle origins are approximated from the entity's position and facing.
    private static Vec3 footPosition(HamsterEntity hamster) {
        return hamster.position().add(0, 0.05, 0);
    }

    private static Vec3 nosePosition(HamsterEntity hamster) {
        float yaw = hamster.yBodyRot * Mth.DEG_TO_RAD;
        double forward = hamster.getBbWidth() * 0.6;
        return hamster.position().add(-Mth.sin(yaw) * forward, hamster.getBbHeight() * 0.45, Mth.cos(yaw) * forward);
    }

    private void handleParticleKeyframes(HamsterEntity animatable) {
        RandomSource random = animatable.getRandom();
        switch (animatable.particleEffectId) {
            case "attack_poof":
                {
                    Vec3 pos = footPosition(animatable);
                    for (int i = 0; i < 8; ++i) {
                        double d = random.nextGaussian() * 0.1;
                        double e = random.nextGaussian() * 0.2;
                        double f = random.nextGaussian() * 0.1;
                        animatable.level().addParticle(ParticleTypes.WHITE_SMOKE,
                                pos.x + d, pos.y + e, pos.z + f,
                                random.nextGaussian() * 0.05,
                                random.nextGaussian() * 0.05,
                                random.nextGaussian() * 0.05);
                    }
                }
                break;
            case "seeking_dust":
                {
                    Vec3 pos = nosePosition(animatable);
                    BlockPos blockBelow = BlockPos.containing(pos.x, pos.y - 0.1, pos.z).below();
                    BlockState state = animatable.level().getBlockState(blockBelow);
                    if (state.isAir()) state = Blocks.DIRT.defaultBlockState();
                    for (int i = 0; i < 12; ++i) {
                        double d = random.nextGaussian() * 0.2;
                        double e = random.nextGaussian() * 0.03;
                        double f = random.nextGaussian() * 0.2;
                        animatable.level().addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, state),
                                pos.x + d, pos.y + e, pos.z + f,
                                0.0, 0.0, 0.0);
                    }
                }
                break;
            case "hamster_spit_particles":
                // Spawn items and spit them out
                {
                    Vec3 pos = nosePosition(animatable);
                    // 1. Item Particles (if holding item)
                    ItemStack mouthStack = animatable.getMouthItemStack();
                    if (!mouthStack.isEmpty()) {
                        for (int i = 0; i < 5; i++) {
                            animatable.level().addParticle(
                                    new ItemParticleOption(ParticleTypes.ITEM, mouthStack.getItem()),
                                    pos.x, pos.y, pos.z,
                                    (random.nextDouble() - 0.5) * 0.3,
                                    random.nextDouble() * 0.2,
                                    (random.nextDouble() - 0.5) * 0.3
                            );
                        }
                    }
                    // 2. Llama Spit Particles
                    for (int i = 0; i < 8; i++) {
                        animatable.level().addParticle(
                                ParticleTypes.SPIT,
                                pos.x, pos.y, pos.z,
                                (random.nextDouble() - 0.5) * 0.1,
                                random.nextDouble() * 0.1,
                                (random.nextDouble() - 0.5) * 0.1
                        );
                    }
                }
                break;
        }
        animatable.particleEffectId = null;
    }

    private void handleSoundKeyframes(HamsterEntity animatable) {
        Minecraft client = Minecraft.getInstance();
        switch (animatable.soundEffectId) {
            case "dynamic_item_sound":
                ItemStack mouthStack = animatable.getMouthItemStack();
                if (!mouthStack.isEmpty()) {
                    SoundEvent dynamicSound = ModSounds.getDynamicItemSound(mouthStack);
                    float baseVol = ModSounds.getDynamicSoundVolume(dynamicSound);

                    client.getSoundManager().play(new SimpleSoundInstance(
                            dynamicSound, SoundSource.NEUTRAL, baseVol * 0.6f, 1.0f + (animatable.getRandom().nextFloat() - 0.5f) * 0.2f,
                            animatable.getRandom(), animatable.getX(), animatable.getY(), animatable.getZ()
                    ));
                }
                break;
            case "hamster_scratch_sound":
                SoundEvent scratchSound = ModSounds.getRandomSoundFrom(ModSounds.HAMSTER_SCRATCH_SOUNDS, animatable.getRandom());
                if (scratchSound != null) {
                    client.getSoundManager().play(new SimpleSoundInstance(
                            scratchSound, SoundSource.NEUTRAL, 0.2f, 0.8f,
                            animatable.getRandom(), animatable.getX(), animatable.getY(), animatable.getZ()
                    ));
                }
                break;
            case "hamster_roll_back_sound":
                SoundEvent rollBackSound = Configs.AHP_MAIN.enableRollingSlideWhistle
                        ? ModSounds.HAMSTER_ROLL_BACK.get()
                        : ModSounds.HAMSTER_ROLL_BACK_NO_SLIDE_WHISTLE.get();

                client.getSoundManager().play(new SimpleSoundInstance(
                        rollBackSound, SoundSource.NEUTRAL, 0.3f, 1.4f,
                        animatable.getRandom(), animatable.getX(), animatable.getY(), animatable.getZ()
                ));
                break;
            case "hamster_roll_forward_sound":
                client.getSoundManager().play(new SimpleSoundInstance(
                        ModSounds.HAMSTER_ROLL_FORWARD.get(), SoundSource.NEUTRAL, 0.2f, 1.4f,
                        animatable.getRandom(), animatable.getX(), animatable.getY(), animatable.getZ()
                ));
                break;
            case "hamster_step_sound":
                BlockPos pos = animatable.blockPosition();
                BlockState blockState = animatable.level().getBlockState(pos.below());
                if (blockState.isAir()) blockState = animatable.level().getBlockState(pos.below(2));
                if (!blockState.isAir()) {
                    SoundType group = blockState.getSoundType();
                    float volume = blockState.is(Blocks.GRAVEL) ? (0.10F * 0.60F) : 0.10F;
                    client.getSoundManager().play(new SimpleSoundInstance(
                            group.getStepSound(), SoundSource.NEUTRAL, volume,
                            group.getPitch() * 1.5F, animatable.getRandom(),
                            animatable.getX(), animatable.getY(), animatable.getZ()
                    ));
                }
                break;
            case "hamster_bounce_sound":
                if (animatable.isDancing()) break; // Mute bounce sound when dancing to music disc
                SoundEvent bounceSound = ModSounds.getRandomSoundFrom(ModSounds.HAMSTER_BOUNCE_SOUNDS, animatable.getRandom());
                if (bounceSound != null) {
                    float basePitch = animatable.getVoicePitch();
                    float randomPitchAddition = animatable.getRandom().nextFloat() * 0.2f;
                    float finalPitch = (basePitch * 1.2f) + randomPitchAddition;
                    client.getSoundManager().play(new SimpleSoundInstance(
                            bounceSound, SoundSource.NEUTRAL, 0.6f, finalPitch,
                            animatable.getRandom(), animatable.getX(), animatable.getY(), animatable.getZ()
                    ));
                }
                break;
            case "hamster_thump_sound":
                float thumpPitch = 1.0F + animatable.getRandom().nextFloat() * 0.4F;
                client.getSoundManager().play(new SimpleSoundInstance(
                        ModSounds.HAMSTER_THUMP.get(), SoundSource.NEUTRAL, 0.3f, thumpPitch,
                        animatable.getRandom(), animatable.getX(), animatable.getY(), animatable.getZ()
                ));

                // Broadcast via server if >16.0 blocks from player to bypass vanilla distance attenuation
                if (client.player != null) {
                    if (animatable.distanceToSqr(client.player) > 16.0 * 16.0) {
                        NetworkManager.sendToServer(
                                new HamsterAnimationSoundPayload(animatable.getId(), "hamster_thump_sound")
                        );
                    }
                }
                break;
            case "hamster_spit_sound":
                client.getSoundManager().play(new SimpleSoundInstance(
                        SoundEvents.LLAMA_SPIT, SoundSource.NEUTRAL, 0.4f, 2.0f,
                        animatable.getRandom(), animatable.getX(), animatable.getY(), animatable.getZ()
                ));
                break;
            case "hamster_sniff_sound":
                SoundEvent sniffSound = ModSounds.getRandomSoundFrom(ModSounds.HAMSTER_DIAMOND_SNIFF_SOUNDS, animatable.getRandom());
                if (sniffSound != null) {
                    client.getSoundManager().play(new SimpleSoundInstance(
                            sniffSound, SoundSource.NEUTRAL, 1.0f, animatable.getVoicePitch(),
                            animatable.getRandom(), animatable.getX(), animatable.getY(), animatable.getZ()
                    ));
                }
                break;
            case "hamster_head_shake_fast_sound":
                client.getSoundManager().play(new SimpleSoundInstance(
                        ModSounds.HAMSTER_HEAD_SHAKE_FAST.get(), SoundSource.NEUTRAL, 0.35f, 1.0f + (animatable.getRandom().nextFloat() - 0.5f) * 0.2f,
                        animatable.getRandom(), animatable.getX(), animatable.getY(), animatable.getZ()
                ));
                break;
            case "hamster_swish_sound":
                client.getSoundManager().play(new SimpleSoundInstance(
                        ModSounds.HAMSTER_SWISH.get(), SoundSource.NEUTRAL, 0.1f, 1.0f + (animatable.getRandom().nextFloat() * 0.5f),
                        animatable.getRandom(), animatable.getX(), animatable.getY(), animatable.getZ()
                ));
                break;
            case "hamster_water_swish_sound":
                SoundEvent waterSwishSound = ModSounds.getRandomSoundFrom(ModSounds.HAMSTER_WATER_SWISH_SOUNDS, animatable.getRandom());
                if (waterSwishSound != null) {
                    client.getSoundManager().play(new SimpleSoundInstance(
                            waterSwishSound, SoundSource.NEUTRAL, 0.25f, 1.0f,
                            animatable.getRandom(), animatable.getX(), animatable.getY(), animatable.getZ()
                    ));
                }
                break;
            case "hamster_affection_sound":
                SoundEvent affectionSound = ModSounds.getRandomSoundFrom(ModSounds.HAMSTER_AFFECTION_SOUNDS, animatable.getRandom());
                if (affectionSound != null) {
                    client.getSoundManager().play(new SimpleSoundInstance(
                            affectionSound, SoundSource.NEUTRAL, 1.0f, 1.0f,
                            animatable.getRandom(), animatable.getX(), animatable.getY(), animatable.getZ()
                    ));
                }
                break;
            case "hamster_celebrate_sound":
                SoundEvent celebrateSound = ModSounds.getRandomSoundFrom(ModSounds.HAMSTER_CELEBRATE_SOUNDS, animatable.getRandom());
                if (celebrateSound != null) {
                    client.getSoundManager().play(new SimpleSoundInstance(
                            celebrateSound, SoundSource.NEUTRAL, 1.0f, 1.0f,
                            animatable.getRandom(), animatable.getX(), animatable.getY(), animatable.getZ()
                    ));
                }
                break;
            case "hamster_sneeze_sound":
                SoundEvent sneezeSound = ModSounds.getRandomSoundFrom(ModSounds.HAMSTER_SNEEZE_SOUNDS, animatable.getRandom());
                if (sneezeSound != null) {
                    client.getSoundManager().play(new SimpleSoundInstance(
                            sneezeSound, SoundSource.NEUTRAL, 0.6f, 1.0f,
                            animatable.getRandom(), animatable.getX(), animatable.getY(), animatable.getZ()
                    ));
                }
                break;
        }
        animatable.soundEffectId = null;
    }
}
