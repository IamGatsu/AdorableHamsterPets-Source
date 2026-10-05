package net.dawson.adorablehamsterpets.item.client;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.dawson.adorablehamsterpets.AdorableHamsterPets;
import net.dawson.adorablehamsterpets.block.client.HamsterBedModel;
import net.dawson.adorablehamsterpets.block.custom.WoodVariant;
import net.dawson.adorablehamsterpets.item.custom.HamsterBedItem;
import net.minecraft.resources.Identifier;

public final class HamsterBedItemModel extends GeoModel<HamsterBedItem> {

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, "hamster_bed");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        WoodVariant variant = renderState.getGeckolibData(HamsterBedModel.WOOD_VARIANT);
        return HamsterBedModel.textureFor(variant != null ? variant : WoodVariant.OAK);
    }

    @Override
    public Identifier getAnimationResource(HamsterBedItem animatable) {
        return Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, "anim_hamster_bed");
    }
}
