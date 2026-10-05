package net.dawson.adorablehamsterpets.mixin.server;

import net.dawson.adorablehamsterpets.config.AcornFluteCreeperMode;
import net.dawson.adorablehamsterpets.config.Configs;
import net.dawson.adorablehamsterpets.flute.FlutePerformanceManager;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Keeps a creeper harmlessly attentive while a qualifying Acorn Flute riff is playing nearby.
 * The target and ordinary movement goals remain untouched; only fuse state is suppressed.
 */
@Mixin(Creeper.class)
public abstract class CreeperEntityMixin {

    /* ──────────────────────────────────────────────────────────────────────────────
     *        Shadows and Synchronized State
     * ────────────────────────────────────────────────────────────────────────────*/

    @Shadow @Final private static EntityDataAccessor<Integer> DATA_SWELL_DIR;
    @Shadow @Final private static EntityDataAccessor<Boolean> DATA_IS_IGNITED;
    @Shadow private int oldSwell;
    @Shadow private int swell;

    @Unique
    private static final EntityDataAccessor<Boolean> AHP_FUSE_SUPPRESSED =
            SynchedEntityData.defineId(Creeper.class, EntityDataSerializers.BOOLEAN);

    /* ──────────────────────────────────────────────────────────────────────────────
     *        Tracker Initialization
     * ────────────────────────────────────────────────────────────────────────────*/

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void adorablehamsterpets$addFuseSuppressionTracker(
            SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(AHP_FUSE_SUPPRESSED, false);
    }

    /* ──────────────────────────────────────────────────────────────────────────────
     *        Fuse Lifecycle
     * ────────────────────────────────────────────────────────────────────────────*/

    @Inject(method = "tick", at = @At("HEAD"))
    private void adorablehamsterpets$updateFuseSuppression(CallbackInfo ci) {
        Creeper creeper = (Creeper) (Object) this;
        SynchedEntityData tracker = creeper.getEntityData();

        // Server owns the area query; the tracker carries the result to client rendering.
        if (!creeper.level().isClientSide()) {
            tracker.set(AHP_FUSE_SUPPRESSED, adorablehamsterpets$shouldSuppressFuse(creeper));
        }

        if (tracker.get(AHP_FUSE_SUPPRESSED)) {
            adorablehamsterpets$resetFuse(tracker);
        }
    }

    @Inject(method = "setSwellDir", at = @At("HEAD"), cancellable = true)
    private void adorablehamsterpets$blockSuppressedFuseSpeed(int fuseSpeed, CallbackInfo ci) {
        Creeper creeper = (Creeper) (Object) this;
        if (!creeper.getEntityData().get(AHP_FUSE_SUPPRESSED)) return;

        adorablehamsterpets$resetFuse(creeper.getEntityData());
        ci.cancel();
    }

    @Inject(method = "ignite", at = @At("HEAD"), cancellable = true)
    private void adorablehamsterpets$rejectSuppressedIgnition(CallbackInfo ci) {
        Creeper creeper = (Creeper) (Object) this;
        if (!creeper.getEntityData().get(AHP_FUSE_SUPPRESSED)
                && (creeper.level().isClientSide()
                || !adorablehamsterpets$shouldSuppressFuse(creeper))) {
            return;
        }

        adorablehamsterpets$resetFuse(creeper.getEntityData());
        ci.cancel();
    }

    @Inject(method = "getSwelling", at = @At("HEAD"), cancellable = true)
    private void adorablehamsterpets$hideSuppressedFuse(
            float tickDelta, CallbackInfoReturnable<Float> cir) {
        Creeper creeper = (Creeper) (Object) this;
        if (creeper.getEntityData().get(AHP_FUSE_SUPPRESSED)) cir.setReturnValue(0.0F);
    }

    /* ──────────────────────────────────────────────────────────────────────────────
     *        Private Helpers
     * ────────────────────────────────────────────────────────────────────────────*/

    @Unique
    private static boolean adorablehamsterpets$shouldSuppressFuse(Creeper creeper) {
        AcornFluteCreeperMode mode = Configs.AHP_MAIN.acornFluteCreeperMode.get();
        if (mode == null || !FlutePerformanceManager.isAffectedByNormalRiff(creeper)) return false;
        return mode == AcornFluteCreeperMode.ALL
                || (mode == AcornFluteCreeperMode.CHARGED_ONLY && creeper.isPowered());
    }

    @Unique
    private void adorablehamsterpets$resetFuse(SynchedEntityData tracker) {
        tracker.set(DATA_SWELL_DIR, -1);
        tracker.set(DATA_IS_IGNITED, false);
        this.swell = 0;
        this.oldSwell = 0;
    }
}
