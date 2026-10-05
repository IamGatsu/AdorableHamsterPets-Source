package net.dawson.adorablehamsterpets.mixin.client;

import net.dawson.adorablehamsterpets.client.render.BlockJiggleManager;
import net.dawson.adorablehamsterpets.client.render.BlockJiggleRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {

    @Inject(method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/PoseStack;Lnet/minecraft/client/render/MultiBufferSource;)V", at = @At("HEAD"))
    private void adorablehamsterpets$pushJiggle(BlockEntity blockEntity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, CallbackInfo ci) {
        if (BlockJiggleManager.INSTANCE.hasJiggle(blockEntity.position().asLong())) {
            matrices.pushPose();
            BlockJiggleRenderer.applyJiggleTransform(matrices, blockEntity.position(), tickDelta, blockEntity.level().getGameTime());
        }
    }

    @Inject(method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/PoseStack;Lnet/minecraft/client/render/MultiBufferSource;)V", at = @At("RETURN"))
    private void adorablehamsterpets$popJiggle(BlockEntity blockEntity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, CallbackInfo ci) {
        if (BlockJiggleManager.INSTANCE.hasJiggle(blockEntity.position().asLong())) {
            matrices.popPose();
        }
    }
}