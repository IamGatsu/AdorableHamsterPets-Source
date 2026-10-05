package net.dawson.adorablehamsterpets.entity.client.renderer;

import net.dawson.adorablehamsterpets.AdorableHamsterPets;
import net.dawson.adorablehamsterpets.entity.ModEntities;
import net.dawson.adorablehamsterpets.entity.client.HamsterRenderer;
import net.dawson.adorablehamsterpets.entity.custom.HamsterEntity;
import net.dawson.adorablehamsterpets.entity.custom.HamsterProjectileEntity;
import net.dawson.adorablehamsterpets.entity.custom.genetics.HamsterGenome;
import net.dawson.adorablehamsterpets.util.HamsterInventoryUtil;
import net.dawson.adorablehamsterpets.util.HamsterState;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.ContainerHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.jetbrains.annotations.Nullable;

/**
 * A specialized renderer for the HamsterProjectileEntity.
 * Unpacks NBT to create a dummy hamster that renders exactly where the projectile is.
 */
public class HamsterProjectileRenderer extends EntityRenderer<HamsterProjectileEntity, HamsterProjectileRenderer.State> {

    /** Render state that carries the extracted render state of the client-side dummy hamster. */
    public static class State extends EntityRenderState {
        @Nullable
        public LivingEntityRenderState hamsterState;
    }

    private final HamsterRenderer hamsterRenderer;

    public HamsterProjectileRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.hamsterRenderer = new HamsterRenderer(ctx);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(HamsterProjectileEntity entity, State state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        state.hamsterState = null;

        // Ensure dummy exists
        if (entity.clientDummyHamster == null) {
            entity.clientDummyHamster = ModEntities.HAMSTER.get().create(entity.level(), net.minecraft.world.entity.EntitySpawnReason.LOAD);
            if (entity.clientDummyHamster != null) {
                entity.clientDummyHamster.setId(net.dawson.adorablehamsterpets.client.state.ClientShoulderHamsterData.nextDummyId());
                entity.clientDummyHamster.setNoGravity(true);
                entity.clientDummyHamster.setNoAi(true); // Disable AI ticking
                entity.clientDummyHamster.isProjectileDummy = true;

                // Decode NBT for visuals
                CompoundTag nbt = entity.getHamsterData();
                if (nbt != null && !nbt.isEmpty()) {
                    HamsterState.fromNbt(nbt).ifPresent(hs -> {
                        entity.clientDummyHamster.setGenome(HamsterGenome.readFromNbt(hs.genomeNbt()));
                        entity.clientDummyHamster.setBaby(hs.breedingAge() < 0);
                        entity.clientDummyHamster.getEntityData().set(HamsterEntity.FLOWER_POS, hs.flowerPosition());
                        entity.clientDummyHamster.setArmorVisible(hs.armorVisible());

                        if (!hs.inventoryNbt().isEmpty()) {
                            entity.clientDummyHamster.getItems().clear();
                            net.dawson.adorablehamsterpets.util.NbtCompat.loadItems(hs.inventoryNbt(), entity.clientDummyHamster.getItems(), entity.level().registryAccess());
                            HamsterInventoryUtil.syncEquipmentTrackers(entity.clientDummyHamster);
                        }
                    });
                }
            }
        }

        if (entity.clientDummyHamster != null) {
            // Sync physics states
            entity.clientDummyHamster.setDeltaMovement(entity.getDeltaMovement());
            entity.clientDummyHamster.setPos(entity.getX(), entity.getY(), entity.getZ());
            entity.clientDummyHamster.tickCount = entity.tickCount;

            // Force yaw sync
            Vec3 vel = entity.getDeltaMovement();
            float livingYaw = (float)(Mth.atan2(-vel.x, vel.z) * Mth.RAD_TO_DEG);

            entity.clientDummyHamster.setYRot(livingYaw);
            entity.clientDummyHamster.yBodyRot = livingYaw;
            entity.clientDummyHamster.yBodyRotO = livingYaw;
            entity.clientDummyHamster.yHeadRot = livingYaw;
            entity.clientDummyHamster.yHeadRotO = livingYaw;

            state.hamsterState = this.hamsterRenderer.createRenderState(entity.clientDummyHamster, tickDelta);
        }
    }

    @Override
    public void submit(State state, PoseStack matrices, SubmitNodeCollector collector, CameraRenderState cameraState) {
        if (state.hamsterState != null) {
            this.hamsterRenderer.submit(state.hamsterState, matrices, collector, cameraState);
        }
        super.submit(state, matrices, collector, cameraState);
    }
}
