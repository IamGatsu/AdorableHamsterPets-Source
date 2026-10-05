package net.dawson.adorablehamsterpets.entity.client.feature;

import net.dawson.adorablehamsterpets.accessor.PlayerEntityAccessor;
import net.dawson.adorablehamsterpets.client.state.ClientShoulderHamsterData;
import net.dawson.adorablehamsterpets.entity.custom.genetics.HamsterGenome;
import net.dawson.adorablehamsterpets.util.HamsterState;
import net.dawson.adorablehamsterpets.entity.ModEntities;
import net.dawson.adorablehamsterpets.entity.ShoulderLocation;
import net.dawson.adorablehamsterpets.entity.client.renderer.ShoulderHamsterRenderer;
import net.dawson.adorablehamsterpets.entity.custom.HamsterEntity;
import net.dawson.adorablehamsterpets.util.HamsterInventoryUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.world.entity.player.PlayerSkin;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import com.mojang.math.Axis;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;

import java.util.EnumMap;
import java.util.Map;

/**
 * Renders a Hamster model on the player's shoulder if shoulder data is present.
 * Handles scaling for baby/adult hamsters and texture variations.
 */

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.player.PlayerModelType;

public class HamsterShoulderFeatureRenderer
        extends RenderLayer<AvatarRenderState, PlayerModel> {

    // --- 1. Constants ---
    private static final float HAMSTER_SHOULDER_SCALE = 0.8f;

    // --- 2. Fields (Lazy Initialization) ---
    private final Map<ShoulderLocation, ShoulderHamsterRenderer> hamsterRenderers = new EnumMap<>(ShoulderLocation.class);

    private final EntityRendererProvider.Context rendererContext;

    // --- 3. Constructor ---
    public HamsterShoulderFeatureRenderer(
            RenderLayerParent<AvatarRenderState, PlayerModel> context,
            EntityRendererProvider.Context rendererContext
    ) {
        super(context);
        this.rendererContext = rendererContext;
    }

    // --- 4. Public Methods (Overrides from RenderLayer) ---
    @Override
    public void submit(PoseStack matrices, SubmitNodeCollector vertexConsumers, int light,
                       AvatarRenderState avatarState, float yRot, float xRot) {
        // Render states don't carry the entity, so look the player up by id (render thread == client thread)
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !(mc.level.getEntity(avatarState.id) instanceof AbstractClientPlayer player)) {
            return;
        }
        float tickDelta = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);

        PlayerEntityAccessor playerAccessor = (PlayerEntityAccessor) player;

        // --- Defensive Check ---
        // Some shader mods (Oculus/Iris) create "shadow" player entities that
        // do not have fully initialized DataTrackers. Attempting to access my custom data
        // on them can crash the game (NPE or IllegalArgumentException).
        // Catch these exceptions to safely skip rendering for these specific entities.
        if (!hasHamsterStateSafe(playerAccessor)) {
            return;
        }

        // --- Lazy Initialization ---
        if (this.hamsterRenderers.isEmpty()) {
            initializeRenderers();
        }

        // --- Get the per-player data holder ---
        ClientShoulderHamsterData clientData = playerAccessor.adorablehamsterpets$getClientHamsterState();
        if (clientData == null) return; // Safety check

        // --- Render Hamster for Each Occupied Slot ---
        for (ShoulderLocation location : ShoulderLocation.values()) {
            CompoundTag shoulderNbt = playerAccessor.getShoulderHamster(location);
            if (!shoulderNbt.isEmpty()) {
                HamsterState.fromNbt(shoulderNbt).ifPresent(hamsterState ->
                        renderShoulderHamster(matrices, vertexConsumers, light, player, avatarState, hamsterState, tickDelta, clientData, location)
                );
            }
        }
    }

    // --- 5. Private Helper Methods ---
    /**
     * Safely checks if the player has any shoulder hamster data.
     * Wraps the DataTracker access in a try-catch block to prevent crashes when rendering
     * malformed entities (e.g., shader shadows or uninitialized fake players).
     */
    private boolean hasHamsterStateSafe(PlayerEntityAccessor playerAccessor) {
        try {
            return playerAccessor.hasAnyShoulderHamster();
        } catch (RuntimeException e) {
            // Swallowing NPE/IllegalArgumentException here is intentional.
            // It indicates the entity is not in a valid state to have its data read.
            return false;
        }
    }

    /**
     * Applies visual data from the stored shoulder NBT to a specific dummy entity.
     * This ensures the rendered model has the correct appearance (variant, age, cheeks, etc.).
     */
    private void applyHamsterState(HamsterEntity dummyHamster, HamsterState data, Player owner) {
        // --- Apply Visual Data ---
        dummyHamster.setGenome(HamsterGenome.readFromNbt(data.genomeNbt()));
        dummyHamster.setLeftCheekFull((data.hamsterFlags() & HamsterEntity.LEFT_CHEEK_FULL_FLAG) != 0);
        dummyHamster.setRightCheekFull((data.hamsterFlags() & HamsterEntity.RIGHT_CHEEK_FULL_FLAG) != 0);
        dummyHamster.getEntityData().set(HamsterEntity.FLOWER_POS, data.flowerPosition());
        dummyHamster.getEntityData().set(HamsterEntity.ANIMATION_PERSONALITY_ID, data.animationPersonalityId());
        dummyHamster.setArmorVisible(data.armorVisible());
        dummyHamster.setAge(data.breedingAge());
        dummyHamster.getEntityData().set(HamsterEntity.EXACT_AGE, data.breedingAge());
        dummyHamster.setBaby(data.breedingAge() < 0);

        // --- Apply Core Flags ---
        // Restore all states from when the hamster was picked up
        dummyHamster.getEntityData().set(HamsterEntity.HAMSTER_FLAGS, data.hamsterFlags());

        // --- Mark as shoulder hamster for animation controller ---
        dummyHamster.setShoulderPet(true);

        // --- Set Custom Name for Easter Eggs ---
        dummyHamster.setCustomName(null);
        data.customName().ifPresent(name -> {
            if (!name.isEmpty()) {
                dummyHamster.setCustomName(Component.literal(name));
            }
        });

        // --- Apply Inventory for Armor/Accessories ---
        // Clear the inventory first to avoid ghost items if the data is empty/changed
        dummyHamster.getItems().clear();

        if (!data.inventoryNbt().isEmpty()) {
            // Use the owner's registry manager since we are on the client
            HolderLookup.Provider registries = owner.registryAccess();

            // Populate the dummy's inventory from NBT
            net.dawson.adorablehamsterpets.util.NbtCompat.loadItems(data.inventoryNbt(), dummyHamster.getItems(), registries);

            // Force update the tracked data fields so the RenderLayers can see the items
            HamsterInventoryUtil.syncEquipmentTrackers(dummyHamster);
        }

        // --- Set Ownership for Animation Logic ---
        dummyHamster.setOwnerReference(net.minecraft.world.entity.EntityReference.of(owner.getUUID()));
        dummyHamster.setTame(true, false); // No attribute update needed
    }

    /**
     * Renders the GeckoLib model on the player's shoulder with appropriate transformations and animations.
     */
    private void renderShoulderHamster(
            PoseStack matrices, SubmitNodeCollector vertexConsumers, int light,
            AbstractClientPlayer player, AvatarRenderState avatarState, HamsterState hamsterState, float tickDelta,
            ClientShoulderHamsterData clientData, ShoulderLocation location
    ) {
        // --- 1. Get the player's personal dummy entity to prevent animation bleed across different players ---
        HamsterEntity dummyHamster = clientData.getOrCreateDummy(location, player.level());
        ShoulderHamsterRenderer hamsterRenderer = this.hamsterRenderers.get(location);
        if (dummyHamster == null || hamsterRenderer == null) return;

        // --- 2. Update Dummy Entity State from Pre-Ticked Data ---
        updateDummyState(dummyHamster, hamsterState, clientData, location, player);


        // --- 3. Physics Simulation ---
        float renderOffsetY = clientData.getRenderOffsetY(location, tickDelta);
        float renderScaleY = clientData.getRenderScaleY(location, tickDelta);
        dummyHamster.dynamicScaleY = renderScaleY; // Set the scale on the dummy entity

        matrices.pushPose();

        // --- 4. Apply Transformations Based on Location ---
        ItemStack chestStack = avatarState.chestEquipment;
        boolean isWearingChestplate = !chestStack.isEmpty() && !chestStack.is(Items.ELYTRA);
        boolean isSlim = avatarState.skin.model() == PlayerModelType.SLIM;

        switch (location) {
            case RIGHT_SHOULDER -> {
                this.getParentModel().rightArm.translateAndRotate(matrices);
                float xOffset, yOffset;
                if (isWearingChestplate) {
                    // Universal offsets for when armor is worn
                    xOffset = -0.18F;
                    yOffset = -0.18F;
                } else {
                    // Original offsets based on player model
                    xOffset = isSlim ? -0.08F : -0.12F;
                    yOffset = -0.12F;
                }
                matrices.translate(xOffset, yOffset, -0.016F);
                matrices.mulPose(Axis.YP.rotationDegrees(15.0F));
            }
            case LEFT_SHOULDER -> {
                this.getParentModel().leftArm.translateAndRotate(matrices);
                float xOffset, yOffset;
                if (isWearingChestplate) {
                    // Universal offsets for when armor is worn
                    xOffset = 0.18F;
                    yOffset = -0.18F;
                } else {
                    // Original offsets based on player model
                    xOffset = isSlim ? 0.08F : 0.12F;
                    yOffset = -0.12F;
                }
                matrices.translate(xOffset, yOffset, -0.016F);
                matrices.mulPose(Axis.YP.rotationDegrees(-15.0F));
            }
            case HEAD -> {
                this.getParentModel().head.translateAndRotate(matrices);
                matrices.translate(0.0F, -0.5F, -0.05F);
            }
        }

        // --- 5. Apply Physics Offset ---
        // The negative sign is crucial to convert our simulation's "up is positive"
        // into the model's "up is negative" local coordinate space.
        matrices.translate(0.0F, -renderOffsetY, 0.0F);

        matrices.mulPose(Axis.XP.rotationDegrees(180.0F));
        matrices.scale(HAMSTER_SHOULDER_SCALE, HAMSTER_SHOULDER_SCALE, HAMSTER_SHOULDER_SCALE);
        // --- 6. Render the Dummy Entity ---
        LivingEntityRenderState hamsterRenderState = hamsterRenderer.createRenderState(dummyHamster, tickDelta);
        hamsterRenderState.bodyRot = 0.0F; // Already in the player's model space
        hamsterRenderState.lightCoords = light;
        hamsterRenderState.shadowRadius = 0.0F;
        hamsterRenderer.submit(hamsterRenderState, matrices, vertexConsumers, new CameraRenderState());

        matrices.popPose();
    }

    /**
     * Calculates the absolute world-space Y-coordinate of a player model's bone.
     *
     * @param matrices   The current PoseStack from the render method.
     * @param anchorBone The ModelPart (e.g., head, rightArm) to measure.
     * @return The world-space Y-coordinate of the bone's pivot point.
     */
    private double getAnchorBoneWorldY(PoseStack matrices, ModelPart anchorBone) {
        PoseStack tempMatrices = new PoseStack();
        tempMatrices.mulPose(matrices.last().pose());
        anchorBone.translateAndRotate(tempMatrices);
        Matrix4f finalMatrix = tempMatrices.last().pose();
        // The Y translation component is at index m31 in a Matrix4f
        return finalMatrix.m31();
    }

    /**
     * Applies all pre-calculated state to the dummy entity right before rendering.
     * This is the final step that bridges the client-thread logic with the render-thread object.
     */
    private void updateDummyState(HamsterEntity dummyHamster, HamsterState nbtData, ClientShoulderHamsterData clientData, ShoulderLocation location, Player owner) {
        // --- 1. Apply visual data from NBT ---
        applyHamsterState(dummyHamster, nbtData, owner);

        // --- 2. Apply animation clock from client data ---
        dummyHamster.tickCount = clientData.getAnimationAge(location);

        // Sync position so audio/particle keyframes play at the player's location instead of world origin
        dummyHamster.setPos(owner.getX(), owner.getY(), owner.getZ());

        // --- 3. Apply animation state from client data ---
        ShoulderHamsterState state = clientData.getHamsterState(location);
        if (state != null) {
            ShoulderAnimationState currentState = state.getCurrentState();
            dummyHamster.getEntityData().set(HamsterEntity.SHOULDER_ANIMATION_STATE, currentState.ordinal());
            dummyHamster.setOrderedToSit(currentState == ShoulderAnimationState.SITTING, true);
        }

        // --- 4. Inform dummy of its location for animation controller ---
        dummyHamster.shoulderLocation = location;
    }

    /**
     * Initializes specialized shoulder hamster renderers, one for each shoulder location.
     */
    private void initializeRenderers() {
        Minecraft client = Minecraft.getInstance();
        EntityRendererProvider.Context context = this.rendererContext;

        for (ShoulderLocation location : ShoulderLocation.values()) {
            this.hamsterRenderers.put(location, new ShoulderHamsterRenderer(context));
        }
    }
}
