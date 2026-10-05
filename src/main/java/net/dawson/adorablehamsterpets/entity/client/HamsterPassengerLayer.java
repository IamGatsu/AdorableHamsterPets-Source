package net.dawson.adorablehamsterpets.entity.client;

import com.geckolib.cache.model.GeoBone;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.PerBoneRender;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.dawson.adorablehamsterpets.entity.custom.HamsterEntity;
import net.dawson.adorablehamsterpets.util.HamsterRidingUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Renders hamster riders "bone-locked" to the hamster's {@code body_child} bone, so they sit on the hamster's back
 * and follow its jump/walk animation (26.2 port of the original HamsterRenderer#renderPassengersForBone).
 *
 * <p>The vanilla physics seat point is intentionally low (camera/collision), so the vanilla-rendered rider is
 * suppressed in EntityRenderDispatcherMixin and drawn here instead:
 * <ol>
 *   <li>Extraction: build the rider's render state ({@link EntityRenderer#createRenderState(Entity, float)}).</li>
 *   <li>Submit: GeckoLib poses the PoseStack at the bone; keep only translation + rotation (rider must not squash
 *       with the hamster's bone scale or the hamster's own scale), apply the visual seat offset, neutralise the
 *       rider renderer's own body-yaw rotation and submit the rider.</li>
 * </ol>
 */
public class HamsterPassengerLayer extends GeoRenderLayer<HamsterEntity, Void, LivingEntityRenderState> {

    private static final String SEAT_BONE = "body_child";

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final DataTicket<List<Passenger>> PASSENGERS =
            DataTicket.create("adorablehamsterpets_passengers", (Class<List<Passenger>>) (Class) List.class);

    /** A rider render state plus the values needed for the seat transform. */
    public record Passenger(EntityRenderState state, Vec3 seatOffset) {}

    public HamsterPassengerLayer(GeoRenderer<HamsterEntity, Void, LivingEntityRenderState> renderer) {
        super(renderer);
    }

    @Override
    public void addRenderData(HamsterEntity hamster, @Nullable Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
        if (hamster.getPassengers().isEmpty() || HamsterRenderer.IS_RENDERING_IN_GUI.get()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();
        List<Passenger> passengers = new ArrayList<>();

        for (Entity passenger : hamster.getPassengers()) {
            if (!(passenger instanceof LivingEntity living)) {
                continue;
            }
            // Skip the rider in first person so it doesn't obstruct the view (vanilla doesn't draw it either)
            if (passenger == client.getCameraEntity() && client.options.getCameraType().isFirstPerson()) {
                continue;
            }

            EntityRenderer<? super Entity, ?> riderRenderer = dispatcher.getRenderer(passenger);
            EntityRenderState riderState = riderRenderer.createRenderState(passenger, partialTick);
            // No separate rider shadow (the original disabled shadows for the bone-locked rider too)
            riderState.shadowPieces.clear();

            Vec3 seat = HamsterRidingUtil.HamsterSeatOffsets.visualSeatOffset(living, hamster.getScale());
            passengers.add(new Passenger(riderState, seat));
        }

        if (!passengers.isEmpty()) {
            renderState.addGeckolibData(PASSENGERS, passengers);
        }
    }

    @Override
    public void addPerBoneRender(RenderPassInfo<LivingEntityRenderState> renderPassInfo,
                                 BiConsumer<GeoBone, PerBoneRender<LivingEntityRenderState>> consumer) {
        if (!renderPassInfo.willRender()) {
            return;
        }
        List<Passenger> passengers = renderPassInfo.renderState().getGeckolibData(PASSENGERS);
        if (passengers == null || passengers.isEmpty()) {
            return;
        }

        renderPassInfo.model().getBone(SEAT_BONE).ifPresent(bone ->
                consumer.accept(bone, (info, bone2, renderTasks) -> {
                    EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
                    PoseStack poseStack = info.poseStack();

                    for (Passenger passenger : passengers) {
                        poseStack.pushPose();

                        // --- 1. Keep the bone's translation + rotation only (drops bone squash and hamster scale) ---
                        PoseStack.Pose pose = poseStack.last();
                        Matrix4f boneMatrix = pose.pose();
                        Vector3f translation = boneMatrix.getTranslation(new Vector3f());
                        Quaternionf rotation = boneMatrix.getNormalizedRotation(new Quaternionf());
                        pose.setIdentity();
                        pose.translate(translation.x, translation.y, translation.z);
                        pose.rotate(rotation);

                        // --- 2. Seat offset on the hamster's back ---
                        Vec3 seat = passenger.seatOffset();
                        poseStack.translate(seat.x, seat.y, seat.z);

                        // --- 3. Neutralise the rider renderer's own rotY(180 - bodyRot) so it stays locked to the bone ---
                        if (passenger.state() instanceof LivingEntityRenderState living) {
                            poseStack.mulPose(Axis.YP.rotationDegrees(living.bodyRot - 180.0F));
                        }

                        // --- 4. Submit the rider ---
                        submitRider(dispatcher, passenger.state(), poseStack, renderTasks, info);

                        poseStack.popPose();
                    }
                }));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void submitRider(EntityRenderDispatcher dispatcher, EntityRenderState state, PoseStack poseStack,
                                    net.minecraft.client.renderer.SubmitNodeCollector renderTasks,
                                    RenderPassInfo<LivingEntityRenderState> info) {
        EntityRenderer renderer = dispatcher.getRenderer(state);
        renderer.submit(state, poseStack, renderTasks, info.cameraState());
    }
}
