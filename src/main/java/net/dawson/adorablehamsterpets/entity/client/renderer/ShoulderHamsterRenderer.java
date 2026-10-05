package net.dawson.adorablehamsterpets.entity.client.renderer;

import net.dawson.adorablehamsterpets.entity.client.HamsterRenderer;
import net.dawson.adorablehamsterpets.entity.custom.HamsterEntity;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.Nullable;
import com.geckolib.cache.model.BakedGeoModel;

/**
 * A specialized renderer for the shoulder-mounted hamster.
 * It extends the base HamsterRenderer but overrides methods to suppress
 * sounds, particles, and other world-interactive effects that are not
 * needed for a purely cosmetic render.
 */
public class ShoulderHamsterRenderer extends HamsterRenderer {

    public ShoulderHamsterRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    protected float getShadowRadius(net.minecraft.client.renderer.entity.state.LivingEntityRenderState state) {
        return 0.0F; // Shoulder hamsters never cast their own shadow
    }
}