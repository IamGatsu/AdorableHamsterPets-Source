package net.dawson.adorablehamsterpets.client.sound;

import net.dawson.adorablehamsterpets.networking.payload.PlayDistantSoundPayload;
import net.dawson.adorablehamsterpets.util.DistantSoundUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.Vec3;

/**
 * Plays one server-positioned distant sound with long-range client-side falloff.
 * A source entity, when supplied, updates the sound origin as it moves.
 */
public final class DistantSoundInstance extends AbstractTickableSoundInstance {

    /* ──────────────────────────────────────────────────────────────────────────────
     *                              Instance State
     * ────────────────────────────────────────────────────────────────────────────*/

    private final Vec3 fallbackSourcePosition;
    private final int sourceEntityId;
    private final float baseVolume;
    private final float audioRange;

    public DistantSoundInstance(PlayDistantSoundPayload payload) {
        super(
                SoundEvent.createVariableRangeEvent(payload.soundId()),
                payload.category(),
                SoundInstance.createUnseededRandom()
        );
        this.looping = false;
        this.attenuation = SoundInstance.Attenuation.NONE;
        this.baseVolume = payload.volume();
        this.audioRange = payload.audioRange();
        this.volume = this.baseVolume;
        this.pitch = payload.pitch();

        this.fallbackSourcePosition = payload.position().orElse(Vec3.ZERO);
        this.sourceEntityId = payload.sourceEntityId();
        this.x = this.fallbackSourcePosition.x;
        this.y = this.fallbackSourcePosition.y;
        this.z = this.fallbackSourcePosition.z;
    }

    /* ──────────────────────────────────────────────────────────────────────────────
     *                                Lifecycle API
     * ────────────────────────────────────────────────────────────────────────────*/

    void markDone() {
        this.stop();
    }

    @Override
    public void tick() {
        if (this.audioRange <= 0.0F) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            this.volume = 0.0F;
            return;
        }

        Vec3 sourcePosition = this.fallbackSourcePosition;
        if (this.sourceEntityId > 0 && client.level != null) {
            Entity sourceEntity = client.level.getEntity(this.sourceEntityId);
            if (sourceEntity != null) {
                sourcePosition = sourceEntity.position();
            }
        }

        this.x = sourcePosition.x;
        this.y = sourcePosition.y;
        this.z = sourcePosition.z;
        double distance = client.player.position().distanceTo(sourcePosition);
        this.volume = this.baseVolume * DistantSoundUtil.calculateVolumeFalloff(distance, this.audioRange);
    }
}
