package net.dawson.adorablehamsterpets.client.particle;

import net.dawson.adorablehamsterpets.flute.AcornFluteVariant;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * A colored Acorn Flute note following a player-directed three-dimensional cone.
 */
public final class AcornFluteNoteParticle extends SingleQuadParticle {

    /* ──────────────────────────────────────────────────────────────────────────────
     *        Constants
     * ────────────────────────────────────────────────────────────────────────────*/

    private static final int MIN_LIFETIME = 40;                     // Higher = notes live longer
    private static final int LIFETIME_VARIANCE = 20;                // Higher = more lifespan variation
    private static final float NOTE_SCALE = 1.0F;                   // Higher = bigger
    private static final float CONE_HALF_ANGLE_DEGREES = 20.0F;     // Higher = wider cone
    private static final float CONE_VERTICAL_MIN_DEGREES = 25.0F;   // 0 = parallel to horizon
    private static final float CONE_VERTICAL_MAX_DEGREES = 90.0F;   // Steepest upward launch pitch
    private static final float INITIAL_SPEED = 0.02F;               // Higher = faster launch
    private static final float INITIAL_SPEED_VARIANCE = 0.07F;      // Higher = wider launch speeds
    private static final float DIRECTIONAL_ACCELERATION = 0.01F;    // Higher = stronger push each tick
    private static final float VELOCITY_DAMPING = 0.96F;            // Closer to 1.0 = glides farther
    private static final float ROTATION_SPEED_MIN = 0.02F;          // Higher = faster
    private static final float ROTATION_SPEED_MAX = 0.06F;          // Higher = faster

    // --- Deterministic Tuning Probes ---
    static float noteScale() {
        return NOTE_SCALE;
    }

    static float initialSpeed(float randomSample) {
        return INITIAL_SPEED
                + Mth.clamp(randomSample, 0.0F, 1.0F) * INITIAL_SPEED_VARIANCE;
    }

    static float opacity(int age, int lifetime) {
        if (lifetime <= 0) {
            return 0.0F;
        }

        float fadeStart = lifetime * 0.9F;
        float fadeOut = age < fadeStart
                ? 1.0F
                : Mth.clamp(
                        (lifetime - age) / (lifetime - fadeStart),
                        0.0F,
                        1.0F);
        return ParticleAnimationUtil.fadeInOpacity(age) * fadeOut;
    }

    static float rotationSpeed(float signedRandomSample) {
        float sample = Mth.clamp(signedRandomSample, -1.0F, 1.0F);
        float magnitude = ROTATION_SPEED_MIN
                + Math.abs(sample) * (ROTATION_SPEED_MAX - ROTATION_SPEED_MIN);
        return sample < 0.0F ? -magnitude : magnitude;
    }

    /* ──────────────────────────────────────────────────────────────────────────────
     *        State
     * ────────────────────────────────────────────────────────────────────────────*/

    private final float baseScale;
    private final double directionX;
    private final double directionY;
    private final double directionZ;
    private float previousScale;
    private float rotationVelocity;

    private AcornFluteNoteParticle(ClientLevel world,
                                   double x, double y, double z,
                                   double xd, double yd, double zd,
                                   SpriteSet sprites, AcornFluteVariant variant,
                                   float lookYaw, float lookPitch) {
        super(world, x, y, z, 0.0D, 0.0D, 0.0D, sprites.first());

        this.setSprite(sprites.get(this.random));
        this.lifetime = MIN_LIFETIME + this.random.nextInt(LIFETIME_VARIANCE);
        this.quadSize *= NOTE_SCALE;
        this.baseScale = this.quadSize;
        Vec3 direction = createConeDirection(lookYaw, lookPitch);
        this.directionX = direction.x;
        this.directionY = direction.y;
        this.directionZ = direction.z;
        this.quadSize = 0.0F;
        this.previousScale = 0.0F;
        this.alpha = 0.0F;
        this.gravity = 0.0F;
        this.friction = VELOCITY_DAMPING;
        this.hasPhysics = true;

        this.rotationVelocity = rotationSpeed(this.random.nextFloat() * 2.0F - 1.0F);
        this.roll = this.random.nextFloat() * Mth.TWO_PI;
        this.oRoll = this.roll;

        double launchSpeed = initialSpeed(this.random.nextFloat());
        this.xd = this.directionX * launchSpeed;
        this.yd = this.directionY * launchSpeed;
        this.zd = this.directionZ * launchSpeed;

        this.setPaletteColor(variant);
    }

    private Vec3 createConeDirection(float lookYaw, float lookPitch) {
        float minimumPitch = (float) Math.toRadians(CONE_VERTICAL_MIN_DEGREES);
        float maximumPitch = (float) Math.toRadians(CONE_VERTICAL_MAX_DEGREES);
        float clampedPitch = Mth.clamp(lookPitch, minimumPitch, maximumPitch);
        float cosPitch = Mth.cos(clampedPitch);
        Vec3 centerDirection = new Vec3(
                Mth.cos(lookYaw) * cosPitch,
                Mth.sin(clampedPitch),
                Mth.sin(lookYaw) * cosPitch).normalize();
        Vec3 reference = Math.abs(centerDirection.y) > 0.99D
                ? new Vec3(1.0D, 0.0D, 0.0D)
                : new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 tangent = centerDirection.cross(reference).normalize();
        Vec3 bitangent = tangent.cross(centerDirection).normalize();
        double maximumConeAngle = Math.min(
                Math.toRadians(CONE_HALF_ANGLE_DEGREES),
                clampedPitch);
        double coneAngle = maximumConeAngle * this.random.nextFloat();
        double azimuth = Mth.TWO_PI * this.random.nextFloat();
        double sinConeAngle = Math.sin(coneAngle);

        return centerDirection.scale(Math.cos(coneAngle))
                .add(tangent.scale(sinConeAngle * Math.cos(azimuth)))
                .add(bitangent.scale(sinConeAngle * Math.sin(azimuth)))
                .normalize();
    }

    // --- Palette ---
    private void setPaletteColor(AcornFluteVariant variant) {
        // Runtime sprites retain each source mask's alpha; white RGB lets particle shader apply this tint
        int[] palette = variant.palette();
        int color = palette[this.random.nextInt(palette.length)];
        this.setColor(
                (color >> 16 & 0xFF) / 255.0F,
                (color >> 8 & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F);
    }

    /* ──────────────────────────────────────────────────────────────────────────────
     *        Lifecycle
     * ────────────────────────────────────────────────────────────────────────────*/

    @Override
    public void tick() {
        // --- Global Drift ---
        this.xd += this.directionX * DIRECTIONAL_ACCELERATION;
        this.yd += this.directionY * DIRECTIONAL_ACCELERATION;
        this.zd += this.directionZ * DIRECTIONAL_ACCELERATION;

        // --- Continuous Rotation ---
        this.oRoll = this.roll;
        this.roll += this.rotationVelocity;
        this.previousScale = this.quadSize;
        this.quadSize = this.baseScale * ParticleAnimationUtil.growInScale(this.age);
        this.alpha = opacity(this.age, this.lifetime);

        super.tick();
    }

    /* ──────────────────────────────────────────────────────────────────────────────
     *        Overrides
     * ────────────────────────────────────────────────────────────────────────────*/

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    @Override
    public float getQuadSize(float tickDelta) {
        return ParticleAnimationUtil.interpolateScale(
                this.previousScale,
                this.quadSize,
                tickDelta);
    }

    /* ──────────────────────────────────────────────────────────────────────────────
     *        Factory
     * ────────────────────────────────────────────────────────────────────────────*/

    public static final class Factory implements ParticleProvider<AcornFluteNoteParticleEffect> {
        private final SpriteSet sprites;

        public Factory(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(AcornFluteNoteParticleEffect effect,
                                       ClientLevel world,
                                       double x, double y, double z,
                                       double xd, double yd, double zd, net.minecraft.util.RandomSource random) {
            return new AcornFluteNoteParticle(
                    world,
                    x,
                    y,
                    z,
                    xd,
                    yd,
                    zd,
                    this.sprites,
                    effect.variant(),
                    effect.lookYaw(),
                    effect.lookPitch());
        }
    }
}
