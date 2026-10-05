package net.dawson.adorablehamsterpets.block.client;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.dawson.adorablehamsterpets.AdorableHamsterPets;
import net.dawson.adorablehamsterpets.block.entity.HamsterBedBlockEntity;
import net.dawson.adorablehamsterpets.block.custom.WoodVariant;
import net.minecraft.resources.Identifier;

/** GeckoLib 5 model for the placed hamster bed. The wood variant is passed through the render state. */
public class HamsterBedModel extends GeoModel<HamsterBedBlockEntity> {

    public static final DataTicket<WoodVariant> WOOD_VARIANT = DataTicket.create("adorablehamsterpets_bed_variant", WoodVariant.class);

    private static final Identifier MODEL = Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, "hamster_bed");
    private static final Identifier ANIMATION = Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, "anim_hamster_bed");

    public static Identifier textureFor(WoodVariant variant) {
        return Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, "textures/block/hamster_bed_" + variant.getSerializedName() + ".png");
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        WoodVariant variant = renderState.getGeckolibData(WOOD_VARIANT);
        return textureFor(variant != null ? variant : WoodVariant.OAK);
    }

    @Override
    public Identifier getAnimationResource(HamsterBedBlockEntity animatable) {
        return ANIMATION;
    }
}
