package net.dawson.adorablehamsterpets.mixin.client;

import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import org.spongepowered.asm.mixin.Mixin;

/**
 * No longer used: the shoulder hamster layer is now registered through Fabric API's
 * LivingEntityRenderLayerRegistrationCallback (see AdorableHamsterPetsClient). Kept empty so
 * the mixin config stays stable; it is not listed in the mixin json anymore.
 */
@Mixin(AvatarRenderer.class)
public abstract class PlayerEntityRendererMixin {
}
