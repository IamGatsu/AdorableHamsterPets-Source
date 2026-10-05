package net.dawson.adorablehamsterpets.util;

import dev.architectury.networking.NetworkManager;
import net.dawson.adorablehamsterpets.networking.payload.PlayDistantSoundPayload;
import net.dawson.adorablehamsterpets.networking.payload.StopDistantSoundPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * Sends server-authoritative distant audio with explicit range and session semantics.
 *
 * <p>Audio range is intentionally an argument to this service. Callers should keep
 * gameplay effect radii separate and calculate those independently.</p>
 */
public final class DistantSoundUtil {

    /* ──────────────────────────────────────────────────────────────────────────────
     *                                  Constants
     * ────────────────────────────────────────────────────────────────────────────*/

    private static final float MINIMUM_VOLUME = 0.0F;
    private static final float MAXIMUM_VOLUME = 1.0F;

    /* ──────────────────────────────────────────────────────────────────────────────
     *                                Static Utilities
     * ────────────────────────────────────────────────────────────────────────────*/

    /**
     * Returns a linear source-to-listener falloff for the supplied audio range.
     * A listener at the edge of the range receives no packet.
     */
    public static float calculateVolumeFalloff(double distance, double audioRange) {
        if (!Double.isFinite(distance)
                || !Double.isFinite(audioRange)
                || distance < 0.0
                || audioRange <= 0.0) {
            return MINIMUM_VOLUME;
        }

        return Mth.clamp((float) (1.0 - distance / audioRange), MINIMUM_VOLUME, MAXIMUM_VOLUME);
    }

    /**
     * Sends one positioned sound to listeners inside the configured range.
     * The default category is neutral.
     */
    public static void playOneShot(
            ServerLevel world,
            Vec3 sourcePosition,
            SoundEvent sound,
            float baseVolume,
            float pitch,
            double audioRange
    ) {
        playOneShot(world, sourcePosition, sound, baseVolume, pitch, audioRange, SoundSource.NEUTRAL);
    }

    /**
     * Sends one positioned sound to listeners inside the configured range.
     */
    public static void playOneShot(
            ServerLevel world,
            Vec3 sourcePosition,
            SoundEvent sound,
            float baseVolume,
            float pitch,
            double audioRange,
            SoundSource category
    ) {
        broadcastStart(
                world,
                sourcePosition,
                0,
                sound,
                baseVolume,
                pitch,
                audioRange,
                category,
                ""
        );
    }

    /**
     * Starts one positioned keyed session. The default category matches vanilla
     * Goat Horn audio and is suitable for Acorn Flute performances.
     */
    public static void startSession(
            ServerLevel world,
            UUID sessionId,
            Vec3 sourcePosition,
            SoundEvent sound,
            float baseVolume,
            float pitch,
            double audioRange
    ) {
        startSession(
                world,
                sessionId,
                sourcePosition,
                sound,
                baseVolume,
                pitch,
                audioRange,
                SoundSource.RECORDS
        );
    }

    /**
     * Starts one positioned keyed session with an explicit category.
     * Reusing a session UUID replaces that identity on each receiving client.
     */
    public static void startSession(
            ServerLevel world,
            UUID sessionId,
            Vec3 sourcePosition,
            SoundEvent sound,
            float baseVolume,
            float pitch,
            double audioRange,
            SoundSource category
    ) {
        startSession(
                world,
                sessionId,
                sourcePosition,
                0,
                sound,
                baseVolume,
                pitch,
                audioRange,
                category);
    }

    /**
     * Starts a positioned keyed session that follows a living source entity on clients.
     * The supplied position remains the fallback if the source is not yet client-tracked.
     */
    public static void startSession(
            ServerLevel world,
            UUID sessionId,
            Vec3 sourcePosition,
            int sourceEntityId,
            SoundEvent sound,
            float baseVolume,
            float pitch,
            double audioRange,
            SoundSource category
    ) {
        broadcastStart(
                world,
                sourcePosition,
                sourceEntityId,
                sound,
                baseVolume,
                pitch,
                audioRange,
                category,
                sessionId.toString()
        );
    }

    /**
     * Explicitly stops a keyed session for every player in this dimension.
     * Stop packets are not range-limited so listeners who moved away still clean up.
     */
    public static void stopSession(ServerLevel world, UUID sessionId) {
        StopDistantSoundPayload payload = new StopDistantSoundPayload(sessionId);
        for (ServerPlayer player : world.players()) {
            NetworkManager.sendToPlayer(player, payload);
        }
    }

    /* ──────────────────────────────────────────────────────────────────────────────
     *                                Private Helpers
     * ────────────────────────────────────────────────────────────────────────────*/

    private static void broadcastStart(
            ServerLevel world,
            Vec3 sourcePosition,
            int sourceEntityId,
            SoundEvent sound,
            float baseVolume,
            float pitch,
            double audioRange,
            SoundSource category,
            String sessionKey
    ) {
        if (!isValidPosition(sourcePosition)
                || !Float.isFinite(baseVolume)
                || baseVolume <= MINIMUM_VOLUME
                || !Double.isFinite(audioRange)
                || audioRange <= 0.0) {
            return;
        }

        for (ServerPlayer player : world.players()) {
            double distance = player.position().distanceTo(sourcePosition);
            float volume = calculateVolumeFalloff(distance, audioRange) * baseVolume;
            if (volume > MINIMUM_VOLUME) {
                volume = Mth.clamp(volume, MINIMUM_VOLUME, MAXIMUM_VOLUME);
                PlayDistantSoundPayload payload = sessionKey.isEmpty()
                        ? PlayDistantSoundPayload.positioned(
                                sound.location(),
                                sourcePosition,
                                baseVolume,
                                pitch,
                                (float) audioRange,
                                category)
                        : PlayDistantSoundPayload.startSession(
                                sound.location(),
                                sourcePosition,
                                sourceEntityId,
                                baseVolume,
                                pitch,
                                (float) audioRange,
                                sessionKey,
                                category);
                NetworkManager.sendToPlayer(player, payload);
            }
        }
    }

    private static boolean isValidPosition(Vec3 position) {
        return position != null
                && Double.isFinite(position.x)
                && Double.isFinite(position.y)
                && Double.isFinite(position.z);
    }

    /* ──────────────────────────────────────────────────────────────────────────────
     *                                Constructor
     * ────────────────────────────────────────────────────────────────────────────*/

    private DistantSoundUtil() {}
}
