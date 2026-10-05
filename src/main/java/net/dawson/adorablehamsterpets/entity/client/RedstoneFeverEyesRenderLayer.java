package net.dawson.adorablehamsterpets.entity.client;

import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.TextureLayerGeoLayer;
import net.dawson.adorablehamsterpets.entity.custom.HamsterEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** Renders only the Redstone Fever eye mask with vanilla's fullbright eyes render type. */
public final class RedstoneFeverEyesRenderLayer extends TextureLayerGeoLayer<HamsterEntity, Void, LivingEntityRenderState> {

    private static final Identifier FEVER_EYES_TEXTURE = Identifier.fromNamespaceAndPath(
            "adorablehamsterpets", "textures/entity/hamster/appearance/conditions/redstone_fever/eyes.png");

    public RedstoneFeverEyesRenderLayer(GeoRenderer<HamsterEntity, Void, LivingEntityRenderState> renderer) {
        super(renderer, FEVER_EYES_TEXTURE, RenderTypes::eyes);
    }

    @Override
    public void submitRenderTask(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
        if (!Boolean.TRUE.equals(renderPassInfo.getGeckolibData(HamsterRenderer.FEVER_EYES))) {
            return;
        }
        super.submitRenderTask(renderPassInfo, renderTasks);
    }
}
