package net.dawson.adorablehamsterpets.networking.payload;

import net.dawson.adorablehamsterpets.AdorableHamsterPets;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PlayerKnockbackPayload(double xd, double yd, double zd) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PlayerKnockbackPayload> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, "player_knockback"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerKnockbackPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, PlayerKnockbackPayload::xd,
            ByteBufCodecs.DOUBLE, PlayerKnockbackPayload::yd,
            ByteBufCodecs.DOUBLE, PlayerKnockbackPayload::zd,
            PlayerKnockbackPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}