package net.dawson.adorablehamsterpets.mixin.client;

import com.geckolib.renderer.base.GeoRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.dawson.adorablehamsterpets.entity.client.HamsterRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Shifts a rolling hamster's shadow backwards along its body yaw (same math as the original).
 *
 * <p>26.2: renderShadow(...) no longer exists. Shadow pieces are extracted relative to the entity and submitted from
 * EntityRenderDispatcher#submit via SubmitNodeCollector#submitShadow(PoseStack, ...). The PoseStack is translated right
 * before that call and restored right after it, exactly like the original HEAD/RETURN injections around renderShadow.
 * The roll offset is captured in HamsterRenderer#addRenderData (ROLL_SHADOW_OFFSET).
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    private static final String SUBMIT_SHADOW =
            "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitShadow(Lcom/mojang/blaze3d/vertex/PoseStack;FLjava/util/List;)V";

    /**
     * Skips the vanilla render of entities riding a hamster; HamsterPassengerLayer draws them bone-locked on the
     * hamster's back instead (the vanilla physics seat point sits almost at ground level).
     */
    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    private void adorablehamsterpets$skipVanillaHamsterRider(EntityRenderState state, CameraRenderState camera, double x, double y, double z,
                                                            PoseStack matrices, SubmitNodeCollector collector, CallbackInfo ci) {
        if (Boolean.TRUE.equals(((GeoRenderState) state).getGeckolibData(HamsterRenderer.RIDING_HAMSTER))) {
            ci.cancel();
        }
    }

    @Inject(method = "submit", at = @At(value = "INVOKE", target = SUBMIT_SHADOW))
    private void adorablehamsterpets$shadowOffsetStart(EntityRenderState state, CameraRenderState camera, double x, double y, double z,
                                                       PoseStack matrices, SubmitNodeCollector collector, CallbackInfo ci) {
        double[] offset = adorablehamsterpets$shadowOffset(state);
        if (offset != null) {
            matrices.translate(offset[0], 0, offset[1]);
        }
    }

    @Inject(method = "submit", at = @At(value = "INVOKE", target = SUBMIT_SHADOW, shift = At.Shift.AFTER))
    private void adorablehamsterpets$shadowOffsetEnd(EntityRenderState state, CameraRenderState camera, double x, double y, double z,
                                                     PoseStack matrices, SubmitNodeCollector collector, CallbackInfo ci) {
        double[] offset = adorablehamsterpets$shadowOffset(state);
        if (offset != null) {
            // Inverse translation
            matrices.translate(-offset[0], 0, -offset[1]);
        }
    }

    private static double[] adorablehamsterpets$shadowOffset(EntityRenderState state) {
        if (!(state instanceof LivingEntityRenderState living)) return null;
        Double offset = ((GeoRenderState) state).getGeckolibData(HamsterRenderer.ROLL_SHADOW_OFFSET);
        if (offset == null || offset <= 0.0) return null;

        // Local backward direction based on (already interpolated) body yaw
        float yawRadians = (float) Math.toRadians(living.bodyRot);
        return new double[]{Math.sin(yawRadians) * offset, -Math.cos(yawRadians) * offset};
    }
}
