package net.dawson.adorablehamsterpets.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class BlockJiggleRenderer {

    /* ──────────────────────────────────────────────────────────────────────────────
     *        Constants
     * ────────────────────────────────────────────────────────────────────────────*/

    private static final float AMPLITUDE = 0.05f;            // Translation distance in blocks
    private static final float ROTATION_AMPLITUDE = 4.0f;    // Rotation in degrees
    private static final float OSCILLATION_CYCLES = 6.0f;    // How many wiggles in 20 ticks

    /* ──────────────────────────────────────────────────────────────────────────────
     *        Public API Methods
     * ────────────────────────────────────────────────────────────────────────────*/

    /*
     * 26.2 port of the original world-space jiggle (WorldRenderEvents.AFTER_ENTITIES + ModelRenderer.render).
     * Rendering is now split into two phases, following vanilla's FallingBlockRenderer:
     *   1. extract(): runs in LevelExtractionEvents.END_EXTRACTION, reads the level and captures a
     *      MovingBlockRenderState (block state, biome tint, light) for every active jiggle.
     *   2. submit():  runs in LevelRenderEvents.COLLECT_SUBMITS, applies the jiggle transform and hands the
     *      block model to the SubmitNodeCollector via submitMovingBlock (same path vanilla uses for falling sand).
     * As in the original, a moving copy is drawn over the real block; block entities are handled by
     * BlockEntityRenderDispatcherMixin.
     */

    private record PendingJiggle(BlockPos pos, MovingBlockRenderState state) {}

    private static final List<PendingJiggle> PENDING = new ArrayList<>();
    private static long extractedGameTime;
    private static float extractedPartialTick;

    /** Extraction phase: capture everything needed from the level for this frame. */
    public static void extract(LevelExtractionContext context) {
        PENDING.clear();
        ClientLevel level = context.level();
        if (level == null || !BlockJiggleManager.INSTANCE.getActiveJiggles().iterator().hasNext()) return;

        extractedGameTime = level.getGameTime();
        extractedPartialTick = context.deltaTracker().getGameTimeDeltaPartialTick(false);

        for (var entry : BlockJiggleManager.INSTANCE.getActiveJiggles()) {
            BlockPos pos = BlockPos.of(entry.getLongKey());

            // Prevent rendering ghost blocks if the chunk is gone
            if (!level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) continue;

            BlockState state = level.getBlockState(pos);
            // Block entities are deformed by BlockEntityRenderDispatcherMixin instead
            if (state.getRenderShape() != RenderShape.MODEL) continue;

            MovingBlockRenderState renderState = new MovingBlockRenderState();
            renderState.blockPos = pos;
            renderState.randomSeedPos = pos;
            renderState.blockState = state;
            renderState.biome = level.getBiome(pos);            // biome tint (leaves, grass, ...)
            renderState.cardinalLighting = level.cardinalLighting();
            renderState.lightEngine = level.getLightEngine();   // matches the real block's lighting
            PENDING.add(new PendingJiggle(pos, renderState));
        }
    }

    /** Drawing phase: submit the jiggling block models. */
    public static void submit(LevelRenderContext context) {
        if (PENDING.isEmpty()) return;

        Vec3 cameraPos = context.levelState().cameraRenderState.pos;
        PoseStack matrices = context.poseStack();
        SubmitNodeCollector collector = context.submitNodeCollector();

        for (PendingJiggle pending : PENDING) {
            BlockPos pos = pending.pos();
            matrices.pushPose();

            // Translate to block position relative to camera
            matrices.translate(pos.getX() - cameraPos.x, pos.getY() - cameraPos.y, pos.getZ() - cameraPos.z);

            // Apply jiggle math
            applyJiggleTransform(matrices, pos, extractedPartialTick, extractedGameTime);

            collector.submitMovingBlock(matrices, pending.state(), 0);

            matrices.popPose();
        }
        PENDING.clear();
    }

    /**
     * Standalone jiggle transformation logic; can be shared with BlockEntityRenderers.
     * Assumes the PoseStack is currently translated to the block's local origin (0, 0, 0).
     */
    public static void applyJiggleTransform(PoseStack matrices, BlockPos pos, float tickDelta, long worldTime) {
        BlockJiggleManager.Jiggle jiggle = BlockJiggleManager.INSTANCE.getJiggle(pos.asLong());
        if (jiggle == null) return;

        BlockJiggleManager.JiggleConfig config = jiggle.config();

        // Calculate age including partial ticks for smoothness
        float age = (worldTime - jiggle.startTick()) + tickDelta;

        if (age < 0 || age > config.duration()) return;

        // --- Physics Math ---
        // Envelope goes from 0.0 to 1.0
        float p = age / config.duration();
        float envelope = 0.5f - 0.5f * Mth.cos((float)(Math.PI * 2.0 * p));

        // Oscillation frequency
        float w = (float)(Math.PI * 2.0 * (config.oscillationCycles() / config.duration()));

        // Randomize phases based on seed so every block jiggles differently
        RandomSource r = RandomSource.create(jiggle.seed());
        float phaseX = r.nextFloat() * (float)(Math.PI * 2.0);
        float phaseZ = r.nextFloat() * (float)(Math.PI * 2.0);
        float phaseRotX = r.nextFloat() * (float)(Math.PI * 2.0);
        float phaseRotY = r.nextFloat() * (float)(Math.PI * 2.0);
        float phaseRotZ = r.nextFloat() * (float)(Math.PI * 2.0);

        // Calculate offsets using custom values for each feature
        float dx = envelope * config.amplitude() * Mth.cos(w * age + phaseX);
        float dy = 0f;
        float dz = envelope * config.amplitude() * Mth.sin(w * age + phaseZ);

        float rotX = envelope * config.rotationAmplitude() * Mth.sin(w * age + phaseRotX);
        float rotY = envelope * config.rotationAmplitude() * Mth.cos(w * age + phaseRotY);
        float rotZ = envelope * config.rotationAmplitude() * Mth.sin(w * age + phaseRotZ);

        // --- Transformation Application ---
        // Center pivot, apply transforms, un-center
        matrices.translate(0.5 + dx, 0.5 + dy, 0.5 + dz);
        matrices.mulPose(Axis.XP.rotationDegrees(rotX));
        matrices.mulPose(Axis.YP.rotationDegrees(rotY));
        matrices.mulPose(Axis.ZP.rotationDegrees(rotZ));
        matrices.translate(-0.5, -0.5, -0.5);
    }
}