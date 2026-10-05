package net.dawson.adorablehamsterpets.mixin.client;

import com.geckolib.renderer.base.GeoRenderState;
import net.dawson.adorablehamsterpets.entity.client.HamsterRenderer;
import net.dawson.adorablehamsterpets.entity.custom.HamsterEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Tags the render state of any entity riding a hamster, so EntityRenderDispatcherMixin can skip its vanilla render
 * (the rider is drawn bone-locked by HamsterPassengerLayer instead).
 * 26.2 replacement for the original LivingEntityRendererMixin (which cancelled LivingEntityRenderer#render).
 */
@Mixin(EntityRenderer.class)
public abstract class EntityRendererRidingTagMixin {

    @Inject(method = "createRenderState(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;",
            at = @At("RETURN"))
    private void adorablehamsterpets$tagHamsterRider(Entity entity, float partialTick, CallbackInfoReturnable<EntityRenderState> cir) {
        if (entity.getVehicle() instanceof HamsterEntity) {
            ((GeoRenderState) cir.getReturnValue()).addGeckolibData(HamsterRenderer.RIDING_HAMSTER, true);
        }
    }
}
