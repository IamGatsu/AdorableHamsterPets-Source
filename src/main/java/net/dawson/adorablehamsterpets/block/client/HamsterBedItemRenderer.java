package net.dawson.adorablehamsterpets.block.client;

import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import net.dawson.adorablehamsterpets.item.client.HamsterBedItemModel;
import net.dawson.adorablehamsterpets.item.custom.HamsterBedItem;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/** GeckoLib 5 item renderer for the hamster bed items (texture depends on the item's wood variant). */
public class HamsterBedItemRenderer extends GeoItemRenderer<HamsterBedItem> {

    public HamsterBedItemRenderer() {
        super(new HamsterBedItemModel());
    }

    /** Culled cutout, as in the original: prevents z-fighting of the zero-thickness bedding planes (see HamsterBedRenderer). */
    @Override
    public @Nullable RenderType getRenderType(GeoRenderState renderState, Identifier texture) {
        return RenderTypes.entityCutoutCull(texture);
    }

    @Override
    public void addRenderData(HamsterBedItem animatable, RenderData relatedObject, GeoRenderState renderState, float partialTick) {
        renderState.addGeckolibData(HamsterBedModel.WOOD_VARIANT, animatable.getVariant());
    }
}
