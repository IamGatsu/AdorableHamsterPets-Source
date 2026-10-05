package net.dawson.adorablehamsterpets.networking.payload;

import net.dawson.adorablehamsterpets.AdorableHamsterPets;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RequestGuidebookWarningPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RequestGuidebookWarningPayload> ID =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, "request_guidebook_warning"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestGuidebookWarningPayload> CODEC =
            StreamCodec.unit(new RequestGuidebookWarningPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
