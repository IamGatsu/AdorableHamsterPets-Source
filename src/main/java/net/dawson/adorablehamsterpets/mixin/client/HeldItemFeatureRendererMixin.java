package net.dawson.adorablehamsterpets.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dawson.adorablehamsterpets.item.ModItems;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hides the Acorn Ring held in a player's off hand (third person / other players).
 * 26.2: renderItem(LivingEntity, ...) became submitArmWithItem(ArmedEntityRenderState, ...). Players use
 * PlayerItemInHandLayer, which delegates to this method for everything except items held to the eye (spyglass).
 */
@Mixin(ItemInHandLayer.class)
public abstract class HeldItemFeatureRendererMixin {

    @Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
    private void adorablehamsterpets$hideAcornRingInOffhand(
            ArmedEntityRenderState state,
            ItemStackRenderState itemState,
            ItemStack stack,
            HumanoidArm arm,
            PoseStack matrices,
            SubmitNodeCollector collector,
            int light,
            CallbackInfo ci) {
        if (state instanceof AvatarRenderState
                && arm != state.mainArm
                && stack.is(ModItems.ACORN_RING.get())) {
            ci.cancel();
        }
    }
}
