package net.dawson.adorablehamsterpets.mixin.client;

import net.dawson.adorablehamsterpets.item.ModItems;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ItemInHandRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class HeldItemRendererMixin {

    @Inject(
            method = "renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayer;FFLnet/minecraft/util/InteractionHand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/PoseStack;Lnet/minecraft/client/render/MultiBufferSource;I)V",
            at = @At("HEAD"),
            cancellable = true)
    private void adorablehamsterpets$hideAcornRingInOffhand(
            AbstractClientPlayer player,
            float tickDelta,
            float pitch,
            InteractionHand hand,
            float swingProgress,
            ItemStack stack,
            float equipProgress,
            PoseStack matrices,
            MultiBufferSource vertexConsumers,
            int light,
            CallbackInfo ci) {
        if (hand == InteractionHand.OFF_HAND && stack.is(ModItems.ACORN_RING.get())) {
            ci.cancel();
        }
    }
}
