package net.dawson.adorablehamsterpets.entity;

import net.dawson.adorablehamsterpets.AdorableHamsterPets;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityDataRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;

/**
 * Custom entity data serializers. Vanilla removed the CompoundTag serializer, so the mod registers its own
 * (through Fabric API so that the network ids stay in sync between client and server).
 */
public final class ModEntityDataSerializers {
    public static final EntityDataSerializer<CompoundTag> COMPOUND_TAG = EntityDataSerializer.forValueType(ByteBufCodecs.COMPOUND_TAG);

    static {
        FabricEntityDataRegistry.register(Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, "compound_tag"), COMPOUND_TAG);
    }

    private ModEntityDataSerializers() {}

    /** Forces class loading (and thereby registration) during mod initialization. */
    public static void init() {}
}
