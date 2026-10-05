package net.dawson.adorablehamsterpets.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dawson.adorablehamsterpets.client.render.BlockJiggleManager;
import net.dawson.adorablehamsterpets.client.render.BlockJiggleRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Jiggles block entities (e.g. hide-and-seek hiding spots) the same way BlockJiggleRenderer jiggles plain blocks.
 *
 * <p>26.2: {@code render(BlockEntity, float, PoseStack, MultiBufferSource)} became
 * {@code submit(BlockEntityRenderState, PoseStack, SubmitNodeCollector, CameraRenderState)}. As before, LevelRenderer
 * translates the PoseStack to the block's origin right before calling it, so the original push/transform/pop
 * logic still applies unchanged.
 */
@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {

    @Inject(method = "submit", at = @At("HEAD"))
    private void adorablehamsterpets$pushJiggle(BlockEntityRenderState renderState, PoseStack matrices, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        if (renderState.blockPos != null && BlockJiggleManager.INSTANCE.hasJiggle(renderState.blockPos.asLong())) {
            matrices.pushPose();
            Minecraft client = Minecraft.getInstance();
            long worldTime = client.level != null ? client.level.getGameTime() : 0L;
            float tickDelta = client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            BlockJiggleRenderer.applyJiggleTransform(matrices, renderState.blockPos, tickDelta, worldTime);
        }
    }

    @Inject(method = "submit", at = @At("RETURN"))
    private void adorablehamsterpets$popJiggle(BlockEntityRenderState renderState, PoseStack matrices, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        if (renderState.blockPos != null && BlockJiggleManager.INSTANCE.hasJiggle(renderState.blockPos.asLong())) {
            matrices.popPose();
        }
    }
}
