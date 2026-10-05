package net.dawson.adorablehamsterpets.networking.payload;

import net.dawson.adorablehamsterpets.AdorableHamsterPets;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

/**
 * Stops one keyed distant sound session on the receiving client.
 */
public record StopDistantSoundPayload(String sessionKey) implements CustomPacketPayload {

    /* ──────────────────────────────────────────────────────────────────────────────
     *                                  Constants
     * ────────────────────────────────────────────────────────────────────────────*/

    private static final int SESSION_KEY_MAX_LENGTH = 64;

    public static final CustomPacketPayload.Type<StopDistantSoundPayload> ID =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, "stop_distant_sound"));

    public static final StreamCodec<RegistryFriendlyByteBuf, StopDistantSoundPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(SESSION_KEY_MAX_LENGTH), StopDistantSoundPayload::sessionKey,
            StopDistantSoundPayload::new
    );

    /* ──────────────────────────────────────────────────────────────────────────────
     *                              Constructors
     * ────────────────────────────────────────────────────────────────────────────*/

    public StopDistantSoundPayload(UUID sessionId) {
        this(sessionId.toString());
    }

    public StopDistantSoundPayload {
        if (sessionKey == null || sessionKey.isBlank()) {
            throw new IllegalArgumentException("Distant sound session key cannot be blank");
        }
        if (sessionKey.length() > SESSION_KEY_MAX_LENGTH) {
            throw new IllegalArgumentException("Distant sound session key is too long");
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
