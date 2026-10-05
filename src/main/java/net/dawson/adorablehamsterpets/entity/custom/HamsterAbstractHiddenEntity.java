package net.dawson.adorablehamsterpets.entity.custom;

import net.dawson.adorablehamsterpets.entity.ModEntities;
import net.dawson.adorablehamsterpets.util.TreeHeistUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An abstract base class for invisible proxy entities that represent a hamster hiding inside a block.
 * Handles global block occupancy registration, NBT storage, and the physics of popping back out.
 */
public abstract class HamsterAbstractHiddenEntity extends Entity {

    /* ──────────────────────────────────────────────────────────────────────────────
     *        Constants and Static Utilities
     * ────────────────────────────────────────────────────────────────────────────*/

    // Maps a specific block anchor to the ID of the entity hiding inside it
    private static final Map<GlobalPos, Integer> OCCUPIED_BLOCKS = new ConcurrentHashMap<>();

    public static boolean isBlockOccupied(Level world, BlockPos anchor) {
        if (anchor == null || world.isClientSide()) return false;
        GlobalPos key = GlobalPos.of(world.dimension(), anchor);
        return OCCUPIED_BLOCKS.containsKey(key);
    }

    @Nullable
    public static HamsterAbstractHiddenEntity getOccupant(Level world, BlockPos anchor) {
        if (anchor == null || world.isClientSide()) return null;
        GlobalPos key = GlobalPos.of(world.dimension(), anchor);
        Integer id = OCCUPIED_BLOCKS.get(key);
        if (id != null) {
            Entity entity = ((ServerLevel) world).getEntity(id);
            if (entity instanceof HamsterAbstractHiddenEntity hidden) {
                return hidden;
            }
        }
        return null;
    }

    /* ──────────────────────────────────────────────────────────────────────────────
     *        Instance Fields
     * ────────────────────────────────────────────────────────────────────────────*/

    protected CompoundTag hamsterNbt = new CompoundTag();
    protected BlockPos anchorPos = null;
    protected BlockPos forcedExitPos = null;
    protected Float forcedExitYaw = null;
    protected boolean isRegistered = false;

    /* ──────────────────────────────────────────────────────────────────────────────
     *        Constructors
     * ────────────────────────────────────────────────────────────────────────────*/

    public HamsterAbstractHiddenEntity(EntityType<?> type, Level world) {
        super(type, world);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setInvisible(true);
    }

    /* ──────────────────────────────────────────────────────────────────────────────
     *        Lifecycle Hooks
     * ────────────────────────────────────────────────────────────────────────────*/

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // No client synced data needed for this logic entity
    }

    @Override
    public void onRemoval(Entity.RemovalReason reason) {
        super.onRemoval(reason);
        if (!this.level().isClientSide()) {
            unregisterOccupancy();
        }
    }

    /* ──────────────────────────────────────────────────────────────────────────────
     *        Public API Methods
     * ────────────────────────────────────────────────────────────────────────────*/

    public BlockPos getAnchorPos() {
        return this.anchorPos;
    }

    public void setForcedExitPos(BlockPos pos) {
        this.forcedExitPos = pos;
    }

    public void setForcedExitYaw(float yaw) {
        this.forcedExitYaw = yaw;
    }

    public boolean isOwnedBy(Player player) {
        if (this.hamsterNbt != null && net.dawson.adorablehamsterpets.util.NbtCompat.hasUUID(hamsterNbt, "Owner")) {
            return net.dawson.adorablehamsterpets.util.NbtCompat.getUUID(hamsterNbt, "Owner").equals(player.getUUID());
        }
        return false;
    }

    /* ──────────────────────────────────────────────────────────────────────────────
     *        Overrides
     * ────────────────────────────────────────────────────────────────────────────*/

    @Override
    protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput __data) {
        CompoundTag nbt = new CompoundTag();
        nbt.put("HamsterNBT", this.hamsterNbt);
        if (this.anchorPos != null) {
            nbt.putLong("AnchorPos", this.anchorPos.asLong());
        }
        if (this.forcedExitPos != null) {
            nbt.putLong("ForcedExitPos", this.forcedExitPos.asLong());
        }
        if (this.forcedExitYaw != null) {
            nbt.putFloat("ForcedExitYaw", this.forcedExitYaw);
        }
        net.dawson.adorablehamsterpets.util.NbtBridge.write(__data, nbt);
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput __data) {
        CompoundTag nbt = net.dawson.adorablehamsterpets.util.NbtBridge.read(__data);
        this.hamsterNbt = nbt.getCompoundOrEmpty("HamsterNBT");

        if (nbt.contains("AnchorPos")) {
            this.anchorPos = BlockPos.of(nbt.getLongOr("AnchorPos", 0L));
        } else if (nbt.contains("TreeAnchor")) {
            // Backwards compatibility for pre v3.6.1 tree heists
            this.anchorPos = BlockPos.of(nbt.getLongOr("TreeAnchor", 0L));
        }

        if (nbt.contains("ForcedExitPos")) {
            this.forcedExitPos = BlockPos.of(nbt.getLongOr("ForcedExitPos", 0L));
        }
        if (nbt.contains("ForcedExitYaw")) {
            this.forcedExitYaw = nbt.getFloatOr("ForcedExitYaw", 0f);
        }
    }

    /* ──────────────────────────────────────────────────────────────────────────────
     *        Protected Helpers
     * ────────────────────────────────────────────────────────────────────────────*/

    protected void registerOccupancy() {
        if (this.level().isClientSide() || this.anchorPos == null || this.isRegistered) return;

        GlobalPos key = GlobalPos.of(this.level().dimension(), this.anchorPos);
        OCCUPIED_BLOCKS.put(key, this.getId());
        this.isRegistered = true;
    }

    protected void unregisterOccupancy() {
        if (this.level().isClientSide() || this.anchorPos == null || !this.isRegistered) return;

        GlobalPos key = GlobalPos.of(this.level().dimension(), this.anchorPos);
        if (OCCUPIED_BLOCKS.remove(key, this.getId())) {
            this.isRegistered = false;
        }
    }

    /**
     * Reconstructs the HamsterEntity from NBT, places it at a safe exit position,
     * and sets its velocity based on whether the action was successful.
     * The subclass is responsible for spawning it into the world.
     *
     * @param success Whether the hamster pops out excitedly (forward velocity) or fails (drops straight down).
     * @return The fully constructed and positioned HamsterEntity, ready to be spawned.
     */
    protected HamsterEntity popOut(boolean success) {
        if (this.level().isClientSide()) return null;
        ServerLevel serverWorld = (ServerLevel) this.level();

        unregisterOccupancy();

        // Calculate exit point
        BlockPos startPoint = this.blockPosition();
        BlockPos exitPos;

        if (this.forcedExitPos != null) {
            exitPos = this.forcedExitPos;
        } else {
            exitPos = TreeHeistUtil.findExitPosition(this.level(), startPoint);
        }

        // Entity reconstruction
        HamsterEntity newHamster = ModEntities.HAMSTER.get().create(serverWorld, net.minecraft.world.entity.EntitySpawnReason.LOAD);
        if (newHamster != null) {
            net.dawson.adorablehamsterpets.util.NbtCompat.loadEntity(newHamster, this.hamsterNbt);
            newHamster.setFallFlyImmunityTicks(0);

            // Calculate exit yaw
            float exitYaw;
            if (this.forcedExitYaw != null) {
                exitYaw = this.forcedExitYaw;
            } else if (this.anchorPos != null && (exitPos.getX() != this.anchorPos.getX() || exitPos.getZ() != this.anchorPos.getZ())) {
                double dx = exitPos.getX() - this.anchorPos.getX();
                double dz = exitPos.getZ() - this.anchorPos.getZ();
                exitYaw = (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
            } else {
                exitYaw = this.random.nextFloat() * 360.0F;
            }

            // Apply position
            newHamster.snapTo(
                    exitPos.getX() + 0.5,
                    exitPos.getY() + 0.1,
                    exitPos.getZ() + 0.5,
                    exitYaw,
                    0
            );

            // Apply velocity
            if (success) {
                Vec3 forward = Vec3.directionFromRotation(0, exitYaw).normalize().scale(0.4);
                newHamster.setDeltaMovement(forward.x, 0.3, forward.z);
            } else {
                newHamster.setDeltaMovement(Vec3.ZERO);
            }

            // Clear stale states
            newHamster.setKnockedOut(false);
            newHamster.setOrderedToSit(false);
            newHamster.setHiding(false);
            newHamster.setActiveCustomGoalName("None");

            newHamster.hurtMarked = true;
        }

        return newHamster;
    }

    @Override
    public boolean hurtServer(ServerLevel level, net.minecraft.world.damagesource.DamageSource source, float amount) {
        return false; // Invisible helper entity, never takes damage
    }
}
