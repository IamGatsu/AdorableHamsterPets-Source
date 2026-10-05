package net.dawson.adorablehamsterpets.networking.payload;

import net.dawson.adorablehamsterpets.AdorableHamsterPets;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record AcknowledgeGuidebookWarningPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AcknowledgeGuidebookWarningPayload> ID =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, "acknowledge_guidebook_warning"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AcknowledgeGuidebookWarningPayload> CODEC =
            StreamCodec.unit(new AcknowledgeGuidebookWarningPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
