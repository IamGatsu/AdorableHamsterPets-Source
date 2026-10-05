package net.dawson.adorablehamsterpets.networking.payload;

import net.dawson.adorablehamsterpets.AdorableHamsterPets;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Starts a distant sound as a listener-position one-shot, a positioned one-shot
 * with distance falloff, or a keyed session.
 *
 * <p>An empty session key selects a one-shot, and an absent position plays the
 * sound at the receiving client player's own position. The three-argument
 * constructor builds that form for the Hamster Yeet impact audio.</p>
 */
public record PlayDistantSoundPayload(
        Identifier soundId,
        Attenuation attenuation,
        float pitch,
        Optional<Vec3> position,
        int sourceEntityId,
        String sessionKey,
        SoundSource category
) implements CustomPacketPayload {

    /* ──────────────────────────────────────────────────────────────────────────────
     *                                  Constants
     * ────────────────────────────────────────────────────────────────────────────*/

    private static final int SESSION_KEY_MAX_LENGTH = 64;

    public static final CustomPacketPayload.Type<PlayDistantSoundPayload> ID =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, "play_distant_sound"));

    private static final StreamCodec<RegistryFriendlyByteBuf, Vec3> POSITION_CODEC = StreamCodec.of(
            (buf, position) -> {
                buf.writeDouble(position.x);
                buf.writeDouble(position.y);
                buf.writeDouble(position.z);
            },
            buf -> new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble())
    );

    private static final StreamCodec<RegistryFriendlyByteBuf, Attenuation> ATTENUATION_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, Attenuation::volume,
            ByteBufCodecs.FLOAT, Attenuation::audioRange,
            Attenuation::new
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayDistantSoundPayload> CODEC = StreamCodec.of(
            (buf, payload) -> {
                Identifier.STREAM_CODEC.encode(buf, payload.soundId());
                ATTENUATION_CODEC.encode(buf, payload.attenuation());
                ByteBufCodecs.FLOAT.encode(buf, payload.pitch());
                ByteBufCodecs.optional(POSITION_CODEC).encode(buf, payload.position());
                ByteBufCodecs.VAR_INT.encode(buf, payload.sourceEntityId());
                ByteBufCodecs.stringUtf8(SESSION_KEY_MAX_LENGTH).encode(buf, payload.sessionKey());
                ByteBufCodecs.idMapper(
                        index -> SoundSource.values()[index],
                        SoundSource::ordinal
                ).encode(buf, payload.category());
            },
            buf -> new PlayDistantSoundPayload(
                    Identifier.STREAM_CODEC.decode(buf),
                    ATTENUATION_CODEC.decode(buf),
                    ByteBufCodecs.FLOAT.decode(buf),
                    ByteBufCodecs.optional(POSITION_CODEC).decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.stringUtf8(SESSION_KEY_MAX_LENGTH).decode(buf),
                    ByteBufCodecs.idMapper(
                            index -> SoundSource.values()[index],
                            SoundSource::ordinal
                    ).decode(buf))
    );

    /* ──────────────────────────────────────────────────────────────────────────────
     *                              Constructors
     * ────────────────────────────────────────────────────────────────────────────*/

    /**
     * Creates a listener-position one-shot with neutral audio, used by the
     * Hamster Yeet impact and armor sounds.
     */
    public PlayDistantSoundPayload(Identifier soundId, float volume, float pitch) {
        this(
                soundId,
                new Attenuation(volume, 0.0F),
                pitch,
                Optional.empty(),
                0,
                "",
                SoundSource.NEUTRAL);
    }

    public PlayDistantSoundPayload {
        if (attenuation == null) {
            attenuation = new Attenuation(0.0F, 0.0F);
        }
        if (position == null) {
            position = Optional.empty();
        }
        if (sessionKey == null) {
            sessionKey = "";
        }
        if (sessionKey.length() > SESSION_KEY_MAX_LENGTH) {
            throw new IllegalArgumentException("Distant sound session key is too long");
        }
        if (category == null) {
            category = SoundSource.NEUTRAL;
        }
    }

    /* ──────────────────────────────────────────────────────────────────────────────
     *                              Static Factories
     * ────────────────────────────────────────────────────────────────────────────*/

    /**
     * Creates a positioned one-shot sound with client-updated distance falloff.
     */
    public static PlayDistantSoundPayload positioned(
            Identifier soundId,
            Vec3 position,
            float baseVolume,
            float pitch,
            float audioRange,
            SoundSource category
    ) {
        return new PlayDistantSoundPayload(
                soundId,
                new Attenuation(baseVolume, audioRange),
                pitch,
                Optional.of(position),
                0,
                "",
                category);
    }

    /**
     * Creates a positioned one-shot using neutral audio.
     */
    public static PlayDistantSoundPayload positioned(
            Identifier soundId,
            Vec3 position,
            float baseVolume,
            float pitch,
            float audioRange
    ) {
        return positioned(soundId, position, baseVolume, pitch, audioRange, SoundSource.NEUTRAL);
    }

    /**
     * Creates a positioned session start. Reusing the same key replaces the
     * currently playing session with that identity on each client.
     */
    public static PlayDistantSoundPayload startSession(
            Identifier soundId,
            Vec3 position,
            int sourceEntityId,
            float baseVolume,
            float pitch,
            float audioRange,
            String sessionKey,
            SoundSource category
    ) {
        if (sessionKey == null || sessionKey.isBlank()) {
            throw new IllegalArgumentException("Distant sound session key cannot be blank");
        }

        return new PlayDistantSoundPayload(
                soundId,
                new Attenuation(baseVolume, audioRange),
                pitch,
                Optional.of(position),
                sourceEntityId,
                sessionKey,
                category);
    }

    public static PlayDistantSoundPayload startSession(
            Identifier soundId,
            Vec3 position,
            float baseVolume,
            float pitch,
            float audioRange,
            String sessionKey,
            SoundSource category
    ) {
        return startSession(
                soundId,
                position,
                0,
                baseVolume,
                pitch,
                audioRange,
                sessionKey,
                category);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public float volume() {
        return this.attenuation.volume();
    }

    public float audioRange() {
        return this.attenuation.audioRange();
    }

    public record Attenuation(float volume, float audioRange) {}
}
