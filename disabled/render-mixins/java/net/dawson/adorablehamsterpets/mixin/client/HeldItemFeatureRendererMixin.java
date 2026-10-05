package net.dawson.adorablehamsterpets.mixin.client;

import net.dawson.adorablehamsterpets.item.ModItems;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.item.ItemDisplayContext;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public abstract class HeldItemFeatureRendererMixin {

    @Inject(
            method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ItemDisplayContext;Lnet/minecraft/util/HumanoidArm;Lnet/minecraft/client/util/math/PoseStack;Lnet/minecraft/client/render/MultiBufferSource;I)V",
            at = @At("HEAD"),
            cancellable = true)
    private void adorablehamsterpets$hideAcornRingInOffhand(
            LivingEntity entity,
            ItemStack stack,
            ItemDisplayContext transformationMode,
            HumanoidArm arm,
            PoseStack matrices,
            MultiBufferSource vertexConsumers,
            int light,
            CallbackInfo ci) {
        if (entity instanceof Player
                && arm != entity.getMainArm()
                && stack.is(ModItems.ACORN_RING.get())) {
            ci.cancel();
        }
    }
}
