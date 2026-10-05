package net.dawson.adorablehamsterpets.entity.client.renderer;

import net.dawson.adorablehamsterpets.entity.custom.HamsterTreeSearcherEntity;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Renderer for an invisible helper entity. It never renders anything.
 */
public class HamsterTreeSearcherRenderer extends EntityRenderer<HamsterTreeSearcherEntity, EntityRenderState> {
    public HamsterTreeSearcherRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public boolean shouldRender(HamsterTreeSearcherEntity entity, Frustum frustum, double x, double y, double z) {
        // Always return false to skip rendering entirely
        return false;
    }
}
