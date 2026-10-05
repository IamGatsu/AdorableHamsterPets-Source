package net.dawson.adorablehamsterpets.flute;

import net.dawson.adorablehamsterpets.accessor.PlayerEntityAccessor;
import net.dawson.adorablehamsterpets.client.particle.AcornFluteNoteParticleEffect;
import net.dawson.adorablehamsterpets.config.Configs;
import net.dawson.adorablehamsterpets.entity.AI.HamsterLookAtEntityGoal;
import net.dawson.adorablehamsterpets.entity.ShoulderLocation;
import net.dawson.adorablehamsterpets.entity.custom.HamsterEntity;
import net.dawson.adorablehamsterpets.item.custom.AcornFluteItem;
import net.dawson.adorablehamsterpets.sound.ModSounds;
import net.dawson.adorablehamsterpets.sound.ModSounds.TimedSound;
import net.dawson.adorablehamsterpets.util.DistantSoundUtil;
import net.dawson.adorablehamsterpets.util.EntityTargetingUtil;
import net.dawson.adorablehamsterpets.util.HamsterInteractionUtil;
import net.dawson.adorablehamsterpets.util.HamsterMovementUtil;
import net.dawson.adorablehamsterpets.util.HamsterNbtUtil;
import net.dawson.adorablehamsterpets.util.HamsterState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Owns server-authoritative Acorn Flute performances and shoulder-call flights.
 * Performances intentionally belong to the exact mainhand stack object used to start them.
 */
public final class FlutePerformanceManager {

    /* ──────────────────────────────────────────────────────────────────────────────
     *        Constants and Static State
     * ────────────────────────────────────────────────────────────────────────────*/

    private static final int FLIGHT_DURATION_TICKS = 15;
    private static final int ARRIVAL_PRESENTATION_TICKS = 5;
    private static final int HIGH_JUMP_PREP_TICKS = (int) Math.round(0.42D * 20.0D); // 8.4 ticks rounded to 8
    public static final double NOTES_PER_TICK = 1.5D; // Maximum particle emission rate during note swells
    private static final double FLIGHT_ARC_HEIGHT = 2.55D;
    private static final double SHOULDER_OFFSET = 0.36D;
    private static final double HEAD_OFFSET_Y = 0.15D;
    private static final double MOUNT_FEEDBACK_SEARCH_MULTIPLIER = 2.0D; // One extra configured range for feedback
    private static final Set<UUID> RESPONDING_HAMSTERS = new HashSet<>();
    private static final Map<UUID, List<Performance>> ACTIVE_PERFORMANCES = new HashMap<>();

    /* ──────────────────────────────────────────────────────────────────────────────
     *        Public Performance API
     * ────────────────────────────────────────────────────────────────────────────*/

    /**
     * Starts a normal riff or the selected hamster's shoulder call for one player.
     */
    public static boolean startPerformance(
            ServerPlayer player, ItemStack initiatingStack, AcornFluteVariant variant) {
        boolean validInitiatingStack = !initiatingStack.isEmpty()
                && initiatingStack.getItem() instanceof AcornFluteItem flute
                && flute.variant() == variant;
        ServerLevel world = player.level();
        long startTick = world.getGameTime();
        List<Performance> performances = ACTIVE_PERFORMANCES.computeIfAbsent(
                player.getUUID(), ignored -> new ArrayList<>());
        Performance activePerformance = performances.isEmpty()
                ? null
                : performances.getLast();
        int antiSpamCooldownTicks = Math.max(0, Configs.AHP_MAIN.acornFluteAntiSpamCooldownTicks.get());
        boolean antiSpamCooldownActive = antiSpamCooldownTicks > 0
                && player.getCooldowns().isOnCooldown(initiatingStack);
        if (!FlutePerformancePolicy.canUse(
                activePerformance != null,
                validInitiatingStack,
                activePerformance != null && activePerformance.mode == Mode.NORMAL,
                antiSpamCooldownActive)) {
            if (performances.isEmpty()) {
                ACTIVE_PERFORMANCES.remove(player.getUUID());
            }
            return false;
        }

        if (activePerformance != null) {
            FlutePerformancePolicy.RepeatAction repeatAction =
                    !isBoundAndAlive(player, activePerformance)
                            ? FlutePerformancePolicy.RepeatAction.REPLACE
                            : FlutePerformancePolicy.repeatAction(
                                    startTick - activePerformance.startTick,
                                    activePerformance.soundEndTick - activePerformance.startTick,
                                    Configs.AHP_MAIN.acornFluteRiffLayeringThresholdPercent.get());
            if (repeatAction == FlutePerformancePolicy.RepeatAction.REPLACE) {
                for (Performance performance : performances) {
                    terminate(player.level().getServer(), player, performance);
                }
                performances.clear();
            }
        }

        double mountRadius = Math.max(0.0D, Configs.AHP_MAIN.acornFluteMountTargetingRadius.get());
        HamsterEntity target = findMountTarget(player, mountRadius);
        if (target == null) {
            HamsterEntity distantTarget = findMountTarget(
                    player, mountFeedbackSearchRadius(mountRadius));
            if (distantTarget != null
                    && distantTarget.distanceToSqr(player) > mountRadius * mountRadius) {
                player.sendSystemMessage(
                        Component.translatable("message.adorablehamsterpets.flute_move_closer"), true);
                return true;
            }
        }

        ShoulderLocation slot = HamsterInteractionUtil.getNextAvailableSlot(player);
        if (target != null && slot == null) {
            player.sendSystemMessage(Component.translatable("message.adorablehamsterpets.shoulder_occupied"), true);
            return true;
        }

        // Set after the bail-outs so feedback-only attempts don't eat the cooldown
        if (antiSpamCooldownTicks > 0) {
            player.getCooldowns().addCooldown(initiatingStack, antiSpamCooldownTicks);
        }

        FlutePerformancePolicy.Mode selectedMode = FlutePerformancePolicy.selectMode(
                target != null,
                slot != null,
                target != null && RESPONDING_HAMSTERS.contains(target.getUUID()));
        if (selectedMode == FlutePerformancePolicy.Mode.SHOULDER_CALL) {
            TimedSound sound = ModSounds.ACORN_FLUTE_CHIFF_TIMED;
            AcornFluteNoteProfile noteProfile = AcornFluteNoteProfiles.chiff();
            boolean wasLookingAtEntity = target.isLookAtEntityGoalActive
                    || HamsterLookAtEntityGoal.class.getSimpleName().equals(target.getActiveCustomGoalName());
            String previousGoal = FlutePerformancePolicy.sanitizePreviousGoalName(
                    target.getActiveCustomGoalName());
            Performance performance = Performance.shoulderCall(
                    player,
                    player.getUUID(),
                    initiatingStack,
                    variant,
                    player.level().dimension(),
                    player.position(),
                    startTick,
                    sound,
                    noteProfile,
                    target,
                    slot,
                    FluteTrainingPolicy.responseDelayTicks(
                            target.getFluteProgressState().getSuccessfulMounts(),
                            Configs.AHP_MAIN.acornFluteMountsToMastery.get()),
                    target.isFrozenMovement(),
                    target.isNoGravity(),
                    target.getHamsterFlag(HamsterEntity.SITTING_FLAG),
                    previousGoal,
                    wasLookingAtEntity);
            performances.add(performance);
            RESPONDING_HAMSTERS.add(target.getUUID());
            prepareShoulderResponse(target, player);
            playSound(world, performance);
            return true;
        }

        int riffIndex = player.getRandom().nextInt(ModSounds.ACORN_FLUTE_RIFFS.size());
        TimedSound sound = ModSounds.ACORN_FLUTE_RIFFS.get(riffIndex);
        AcornFluteNoteProfile noteProfile = AcornFluteNoteProfiles.forRiffIndex(riffIndex);
        Performance performance = Performance.normal(
                player,
                player.getUUID(),
                initiatingStack,
                variant,
                player.level().dimension(),
                player.position(),
                startTick,
                sound,
                noteProfile);
        performances.add(performance);
        playSound(world, performance);
        return true;
    }

    /**
     * Cancels the player's current performance and sends an explicit stop packet.
     */
    public static void cancel(ServerPlayer player) {
        List<Performance> performances = ACTIVE_PERFORMANCES.remove(player.getUUID());
        if (performances != null) {
            for (Performance performance : performances) {
                terminate(player.level().getServer(), player, performance);
            }
        }
    }

    /**
     * Ticks all sessions once on the server thread.
     */
    public static void onServerTick(MinecraftServer server) {
        Iterator<Map.Entry<UUID, List<Performance>>> iterator = ACTIVE_PERFORMANCES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, List<Performance>> entry = iterator.next();
            List<Performance> performances = entry.getValue();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());

            if (player == null
                    || performances.stream().anyMatch(performance -> !isBoundAndAlive(player, performance))) {
                iterator.remove();
                for (Performance performance : performances) {
                    terminate(server, player, performance);
                }
                continue;
            }

            long currentTick = player.level().getGameTime();
            Iterator<Performance> performanceIterator = performances.iterator();
            while (performanceIterator.hasNext()) {
                Performance performance = performanceIterator.next();
                tickPresentationAndProgress(player, performance, currentTick);
                if (!performance.soundStopped && currentTick >= performance.soundEndTick) {
                    stopSoundEverywhere(server, performance.sessionId);
                    performance.soundStopped = true;
                }

                if (performance.mode == Mode.NORMAL) {
                    if (currentTick >= performance.soundEndTick) {
                        performanceIterator.remove();
                    }
                    continue;
                }

                if (!tickShoulderCall(server, player, performance, currentTick)) {
                    performanceIterator.remove();
                    terminate(server, player, performance);
                }
            }

            if (performances.isEmpty()) {
                iterator.remove();
            }
        }
    }

    /**
     * Returns true when any active normal riff affects the supplied entity.
     */
    public static boolean isAffectedByNormalRiff(Entity entity) {
        return !getNormalRiffPerformersAffecting(entity).isEmpty();
    }

    /**
     * Returns immutable UUIDs for all active normal-riff performers affecting a hamster.
     * Dimension and configured effect-radius checks are applied at query time.
     */
    public static Set<UUID> getNormalRiffPerformersAffecting(Entity entity) {
        if (entity == null || entity.level().isClientSide()) {
            return Set.of();
        }

        double radius = Math.max(0.0D, Configs.AHP_MAIN.acornFluteEffectRadius.get());
        double radiusSquared = radius * radius;
        Set<UUID> performers = new HashSet<>();
        for (List<Performance> performances : ACTIVE_PERFORMANCES.values()) {
            for (Performance performance : performances) {
                if (performance.mode == Mode.NORMAL
                        && isBoundAndAlive(performance.player, performance)
                        && performance.dimension.equals(entity.level().dimension())
                        && performance.sourcePosition.distanceToSqr(entity.position()) <= radiusSquared) {
                    performers.add(performance.playerUuid);
                }
            }
        }
        return Set.copyOf(performers);
    }

    /**
     * Returns true when any active normal riff affects the supplied hamster.
     */
    public static boolean isAffectedByAnyActiveNormalRiff(HamsterEntity hamster) {
        return isAffectedByNormalRiff(hamster);
    }

    /**
     * Compatibility alias for integrations that prefer an explicit active-session name.
     */
    public static Set<UUID> getActiveNormalRiffPerformers(HamsterEntity hamster) {
        return getNormalRiffPerformersAffecting(hamster);
    }

    /**
     * Returns whether a specific player currently supplies a qualifying normal riff.
     */
    public static boolean isNormalRiffActive(UUID performerUuid) {
        List<Performance> performances = ACTIVE_PERFORMANCES.get(performerUuid);
        return performances != null && performances.stream().anyMatch(performance ->
                performance.mode == Mode.NORMAL && isBoundAndAlive(performance.player, performance));
    }

    /* ──────────────────────────────────────────────────────────────────────────────
     *        Shoulder Response Lifecycle
     * ────────────────────────────────────────────────────────────────────────────*/

    private static boolean tickShoulderCall(
            MinecraftServer server,
            ServerPlayer player,
            Performance performance,
            long currentTick) {
        HamsterEntity hamster = performance.target;
        if (!isValidMountTarget(hamster, player)
                || HamsterInteractionUtil.getNextAvailableSlot(player) != performance.slot) {
            return false;
        }

        if (!performance.flightStarted) {
            HamsterMovementUtil.faceEntity(hamster, player);
            hamster.getNavigation().stop();
            hamster.setDeltaMovement(Vec3.ZERO);
            hamster.setActiveCustomGoalName("FluteMountResponse");

            long elapsedTicks = currentTick - performance.startTick;

            if (elapsedTicks >= 10 && !performance.headCockTriggered) {
                String headCockAnim;
                if (performance.wasLookingAtEntity) {
                    headCockAnim = hamster.getRandom().nextBoolean()
                            ? "anim_hamster_head_cock_up_right"
                            : "anim_hamster_head_cock_up_left";
                } else {
                    headCockAnim = hamster.getRandom().nextBoolean()
                            ? "anim_hamster_head_cock_right"
                            : "anim_hamster_head_cock_left";
                }
                hamster.triggerAnimOnServer("headController", headCockAnim);
                performance.headCockTriggered = true;
                performance.triggeredHeadCockAnim = headCockAnim;
            }

            int prepStartTick = Math.max(0, performance.responseDelayTicks - HIGH_JUMP_PREP_TICKS);
            if (elapsedTicks >= prepStartTick && !performance.jumpAnimTriggered) {
                if (performance.headCockTriggered && performance.triggeredHeadCockAnim != null) {
                    hamster.stopTriggeredAnim("headController", performance.triggeredHeadCockAnim);
                }
                hamster.triggerAnimOnServer("mainController", "anim_hamster_high_jump");
                performance.jumpAnimTriggered = true;
            }

            if (elapsedTicks < performance.responseDelayTicks) {
                return true;
            }

            hamster.setNoGravity(true);
            hamster.setFluteMountFlight(true);
            hamster.setFluteMountPitch(0.0F);
            performance.flightStarted = true;
            performance.flightStartTick = currentTick;
            performance.flightStartPosition = hamster.position();
            faceAlongFlight(
                    hamster,
                    FluteFlightMath.directionAlongArc(
                            performance.flightStartPosition,
                            shoulderAnchor(player, performance.slot),
                            0.0D,
                            1.0D / FLIGHT_DURATION_TICKS,
                            FLIGHT_ARC_HEIGHT));
            hamster.setDeltaMovement(Vec3.ZERO);
            return true;
        }

        double progress = Math.min(
                1.0D,
                (double) (currentTick - performance.flightStartTick) / FLIGHT_DURATION_TICKS);
        Vec3 previousPosition = hamster.position();
        Vec3 destination = shoulderAnchor(player, performance.slot);
        Vec3 nextPosition = FluteFlightMath.positionAlongArc(
                performance.flightStartPosition, destination, progress, FLIGHT_ARC_HEIGHT);
        Vec3 flightDirection = FluteFlightMath.directionAlongArc(
                performance.flightStartPosition,
                destination,
                progress,
                1.0D / FLIGHT_DURATION_TICKS,
                FLIGHT_ARC_HEIGHT);

        hamster.setFluteMountPitch(FluteFlightMath.pitchFromVelocity(
                flightDirection.x, flightDirection.y, flightDirection.z));
        float flightYaw = faceAlongFlight(hamster, flightDirection);
        hamster.setDeltaMovement(Vec3.ZERO);
        hamster.snapTo(
                nextPosition.x,
                nextPosition.y,
                nextPosition.z,
                flightYaw,
                hamster.getXRot());
        if (!hamster.level().noCollision(hamster)) {
            hamster.snapTo(
                    previousPosition.x,
                    previousPosition.y,
                    previousPosition.z,
                    hamster.getYRot(),
                    hamster.getXRot());
            return false;
        }
        hamster.hurtMarked = true;

        if (progress < 1.0D
                || performance.arrivalPresentationTicks++ < ARRIVAL_PRESENTATION_TICKS) {
            return true;
        }

        return finishShoulderMount(server, player, performance);
    }

    private static void tickPresentationAndProgress(
            ServerPlayer player, Performance performance, long currentTick) {
        ServerLevel world = player.level();
        if (currentTick < performance.soundEndTick) {
            long elapsedTick = currentTick - performance.startTick;
            if (elapsedTick >= 0L && performance.noteProfile != null) {
                double normalizedIntensity = AcornFluteNoteProfiles.applyParticleNoiseFloor(
                        performance.noteProfile.normalizedIntensityAt(elapsedTick));
                performance.noteSpawnAccumulator += NOTES_PER_TICK * normalizedIntensity;
            }
        }
        int toSpawn = (int) performance.noteSpawnAccumulator;
        if (toSpawn > 0) {
            performance.noteSpawnAccumulator -= toSpawn;
            Vec3 hand = mainHandPosition(player);
            world.sendParticles(
                    new AcornFluteNoteParticleEffect(
                            performance.variant,
                            playerLookYaw(player),
                            playerLookPitch(player)),
                    hand.x,
                    hand.y,
                    hand.z,
                    toSpawn,
                    0.12D,
                    0.05D,
                    0.12D,
                    0.025D);
        }

        if (performance.mode != Mode.NORMAL) {
            return;
        }

        double radius = Math.max(0.0D, Configs.AHP_MAIN.acornFluteEffectRadius.get());
        if (radius <= 0.0D) {
            return;
        }

        AABB affectedArea = new AABB(performance.sourcePosition, performance.sourcePosition).inflate(radius);
        double radiusSquared = radius * radius;
        for (HamsterEntity hamster : world.getEntitiesOfClass(
                HamsterEntity.class,
                affectedArea,
                candidate -> candidate.hasRedstoneFever()
                        && candidate.distanceToSqr(performance.sourcePosition) <= radiusSquared)) {
            hamster.getFluteProgressState().recordFeverPerformer(performance.playerUuid);
        }
    }

    private static boolean finishShoulderMount(
            MinecraftServer server, ServerPlayer player, Performance performance) {
        HamsterEntity hamster = performance.target;
        PlayerEntityAccessor accessor = (PlayerEntityAccessor) player;
        if (HamsterInteractionUtil.getNextAvailableSlot(player) != performance.slot) {
            return false;
        }

        if (performance.headCockTriggered && performance.triggeredHeadCockAnim != null) {
            hamster.stopTriggeredAnim("headController", performance.triggeredHeadCockAnim);
        }
        if (performance.jumpAnimTriggered) {
            hamster.stopTriggeredAnim("mainController", "anim_hamster_high_jump");
        }
        hamster.setFluteMountFlight(false);
        hamster.setFluteMountPitch(0.0F);
        hamster.setFluteMountResponseActive(false);
        hamster.setNoGravity(performance.previousNoGravity);
        hamster.setFrozenMovement(performance.previousFrozenMovement);
        hamster.setActiveCustomGoalName(performance.previousGoalName);
        hamster.setDeltaMovement(Vec3.ZERO);

        HamsterInteractionUtil.executeShoulderMount(
                hamster,
                player,
                performance.initiatingStack,
                HamsterInteractionUtil.ShoulderMountSoundTiming.IMMEDIATE);
        if (!hamster.isRemoved() || accessor.getShoulderHamster(performance.slot).isEmpty()) {
            return false;
        }

        hamster.getFluteProgressState().recordSuccessfulMount();
        HamsterState updatedState = HamsterNbtUtil.saveToHamsterState(hamster);
        accessor.setShoulderHamster(performance.slot, updatedState.toNbt());
        RESPONDING_HAMSTERS.remove(hamster.getUUID());
        return true;
    }

    private static void prepareShoulderResponse(HamsterEntity hamster, ServerPlayer player) {
        hamster.setOrderedToSit(false, true);
        hamster.setFluteMountResponseActive(true);
        hamster.setActiveCustomGoalName("FluteMountResponse");
        hamster.getEntityData().set(
                HamsterEntity.CURRENT_LOOK_UP_ANIM_ID,
                Math.floorMod(hamster.getUUID().hashCode(), 3) + 1);
        hamster.getNavigation().stop();
        HamsterMovementUtil.faceEntity(hamster, player);
    }

    /* ──────────────────────────────────────────────────────────────────────────────
     *        Validation and Audio Helpers
     * ────────────────────────────────────────────────────────────────────────────*/

    static double mountFeedbackSearchRadius(double mountRadius) {
        return mountRadius * MOUNT_FEEDBACK_SEARCH_MULTIPLIER;
    }

    @Nullable
    private static HamsterEntity findMountTarget(ServerPlayer player, double radius) {
        if (radius <= 0.0D) {
            return null;
        }

        AABB searchBox = player.getBoundingBox().inflate(radius);
        HamsterEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (HamsterEntity hamster : player.level().getEntitiesOfClass(
                HamsterEntity.class, searchBox, entity -> isValidMountTarget(entity, player))) {
            if (RESPONDING_HAMSTERS.contains(hamster.getUUID())
                    || hamster.level().isClientSide()
                    || !EntityTargetingUtil.isLookingAt(player, hamster, radius, 0.0D)
                    || !player.hasLineOfSight(hamster)) {
                continue;
            }

            double distance = hamster.distanceToSqr(player);
            if (distance < nearestDistance) {
                nearest = hamster;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private static boolean isValidMountTarget(HamsterEntity hamster, Player player) {
        return hamster != null
                && hamster.isAlive()
                && !hamster.isRemoved()
                && hamster.isTame()
                && net.dawson.adorablehamsterpets.util.PetOwnershipUtil.isOwnedBy(hamster, player.getUUID())
                && hamster.level() == player.level()
                && !hamster.isSleeping()
                && !hamster.isKnockedOut()
                && !hamster.isSulking()
                && !hamster.isShoulderPet()
                && !hamster.isWanderModeActive()
                && !hamster.isPassenger()
                && !hamster.isVehicle()
                && !hamster.isLeashed()
                && !hamster.isNoAi();
    }

    private static boolean isBoundAndAlive(ServerPlayer player, Performance performance) {
        ItemStack mainhand = player.getMainHandItem();
        boolean matchingVariant = mainhand.getItem() instanceof AcornFluteItem flute
                && flute.variant() == performance.variant;
        return FlutePerformancePolicy.remainsBound(
                player.isAlive(),
                player.level().dimension().equals(performance.dimension),
                mainhand == performance.initiatingStack,
                matchingVariant);
    }

    private static Vec3 shoulderAnchor(Player player, ShoulderLocation slot) {
        double yaw = Math.toRadians(player.yBodyRot);
        Vec3 side = new Vec3(Math.cos(yaw), 0.0D, Math.sin(yaw));
        double sideOffset = switch (slot) {
            case RIGHT_SHOULDER -> -SHOULDER_OFFSET;
            case LEFT_SHOULDER -> SHOULDER_OFFSET;
            case HEAD -> 0.0D;
        };
        double y = player.getY() + player.getBbHeight() * 0.8D
                + (slot == ShoulderLocation.HEAD ? HEAD_OFFSET_Y : 0.0D);
        Vec3 horizontal = player.position().add(side.scale(sideOffset));
        return new Vec3(horizontal.x, y, horizontal.z);
    }

    private static float faceAlongFlight(HamsterEntity hamster, Vec3 direction) {
        float yaw = FluteFlightMath.yawFromDirection(direction.x, direction.z);
        hamster.setYRot(yaw);
        hamster.yBodyRot = yaw;
        hamster.yHeadRot = yaw;
        hamster.yRotO = yaw;
        hamster.yBodyRotO = yaw;
        hamster.yHeadRotO = yaw;
        return yaw;
    }

    private static Vec3 mainHandPosition(Player player) {
        double yaw = Math.toRadians(player.getYRot());
        Vec3 right = new Vec3(Math.cos(yaw), 0.0D, Math.sin(yaw));
        double side = player.getMainArm() == HumanoidArm.RIGHT ? -0.24D : 0.24D;
        return player.getEyePosition()
                .add(player.getViewVector(1.0F).scale(0.42D))
                .add(right.scale(side))
                .add(0.0D, -0.32D, 0.0D);
    }

    private static float playerLookYaw(Player player) {
        return (float) Math.toRadians(90.0D + player.getYRot());
    }

    private static float playerLookPitch(Player player) {
        float upwardAngleDegrees = Math.max(0.0F, Math.min(90.0F, -player.getXRot()));
        return (float) Math.toRadians(upwardAngleDegrees);
    }

    private static void playSound(ServerLevel world, Performance performance) {
        DistantSoundUtil.startSession(
                world,
                performance.sessionId,
                performance.sourcePosition,
                performance.player.getId(),
                performance.sound.sound().get(),
                1.0F,
                1.0F,
                Math.max(1.0D, Configs.AHP_MAIN.acornFluteAudioRange.get()),
                SoundSource.RECORDS);
    }

    private static void terminate(
            @Nullable MinecraftServer server,
            @Nullable ServerPlayer player,
            Performance performance) {
        if (server != null) {
            stopSoundEverywhere(server, performance.sessionId);
        } else if (player != null && player.level() instanceof ServerLevel world) {
            DistantSoundUtil.stopSession(world, performance.sessionId);
        }

        HamsterEntity hamster = performance.target;
        if (hamster != null) {
            RESPONDING_HAMSTERS.remove(hamster.getUUID());
            if (!hamster.isRemoved()) {
                if (performance.headCockTriggered && performance.triggeredHeadCockAnim != null) {
                    hamster.stopTriggeredAnim("headController", performance.triggeredHeadCockAnim);
                }
                if (performance.jumpAnimTriggered) {
                    hamster.stopTriggeredAnim("mainController", "anim_hamster_high_jump");
                }
                hamster.setFluteMountFlight(false);
                hamster.setFluteMountPitch(0.0F);
                hamster.setFluteMountResponseActive(false);
                hamster.setNoGravity(performance.previousNoGravity);
                hamster.setFrozenMovement(performance.previousFrozenMovement);
                hamster.setActiveCustomGoalName(performance.previousGoalName);
                hamster.setDeltaMovement(Vec3.ZERO);
                if (performance.previousSitting) {
                    hamster.setOrderedToSit(true, true);
                }
            }
        }
    }

    private static void stopSoundEverywhere(MinecraftServer server, UUID sessionId) {
        for (ServerLevel world : server.getAllLevels()) {
            DistantSoundUtil.stopSession(world, sessionId);
        }
    }

    /* ──────────────────────────────────────────────────────────────────────────────
     *        Constructor and Nested Types
     * ────────────────────────────────────────────────────────────────────────────*/

    private FlutePerformanceManager() {}

    private enum Mode {
        NORMAL,
        SHOULDER_CALL
    }

    private static final class Performance {
        private final UUID playerUuid;
        private final ServerPlayer player;
        private final UUID sessionId = UUID.randomUUID();
        private final ItemStack initiatingStack;
        private final AcornFluteVariant variant;
        private final ResourceKey<Level> dimension;
        private final Vec3 sourcePosition;
        private final long startTick;
        private final TimedSound sound;
        @Nullable private final AcornFluteNoteProfile noteProfile;
        private final long soundEndTick;
        private final Mode mode;
        @Nullable private final HamsterEntity target;
        @Nullable private final ShoulderLocation slot;
        private final int responseDelayTicks;
        private final boolean previousFrozenMovement;
        private final boolean previousNoGravity;
        private final boolean previousSitting;
        private final String previousGoalName;
        private final boolean wasLookingAtEntity;
        private boolean soundStopped;
        private boolean headCockTriggered;
        @Nullable private String triggeredHeadCockAnim;
        private boolean jumpAnimTriggered;
        private boolean flightStarted;
        private long flightStartTick;
        private Vec3 flightStartPosition = Vec3.ZERO;
        private int arrivalPresentationTicks;
        private double noteSpawnAccumulator;

        private Performance(
                ServerPlayer player,
                UUID playerUuid,
                ItemStack initiatingStack,
                AcornFluteVariant variant,
                ResourceKey<Level> dimension,
                Vec3 sourcePosition,
                long startTick,
                TimedSound sound,
                @Nullable AcornFluteNoteProfile noteProfile,
                Mode mode,
                @Nullable HamsterEntity target,
                @Nullable ShoulderLocation slot,
                int responseDelayTicks,
                boolean previousFrozenMovement,
                boolean previousNoGravity,
                boolean previousSitting,
                String previousGoalName,
                boolean wasLookingAtEntity) {
            this.player = player;
            this.playerUuid = playerUuid;
            this.initiatingStack = initiatingStack;
            this.variant = variant;
            this.dimension = dimension;
            this.sourcePosition = sourcePosition;
            this.startTick = startTick;
            this.sound = sound;
            this.noteProfile = noteProfile;
            this.soundEndTick = startTick + Math.max(1L, (long) Math.ceil(sound.durationSeconds() * 20.0D));
            this.mode = mode;
            this.target = target;
            this.slot = slot;
            this.responseDelayTicks = responseDelayTicks;
            this.previousFrozenMovement = previousFrozenMovement;
            this.previousNoGravity = previousNoGravity;
            this.previousSitting = previousSitting;
            this.previousGoalName = previousGoalName;
            this.wasLookingAtEntity = wasLookingAtEntity;
        }

        private static Performance normal(
                ServerPlayer player,
                UUID playerUuid,
                ItemStack initiatingStack,
                AcornFluteVariant variant,
                ResourceKey<Level> dimension,
                Vec3 sourcePosition,
                long startTick,
                TimedSound sound,
                @Nullable AcornFluteNoteProfile noteProfile) {
            return new Performance(
                    player,
                    playerUuid,
                    initiatingStack,
                    variant,
                    dimension,
                    sourcePosition,
                    startTick,
                    sound,
                    noteProfile,
                    Mode.NORMAL,
                    null,
                    null,
                    0,
                    false,
                    false,
                    false,
                    "None",
                    false);
        }

        private static Performance shoulderCall(
                ServerPlayer player,
                UUID playerUuid,
                ItemStack initiatingStack,
                AcornFluteVariant variant,
                ResourceKey<Level> dimension,
                Vec3 sourcePosition,
                long startTick,
                TimedSound sound,
                @Nullable AcornFluteNoteProfile noteProfile,
                HamsterEntity target,
                ShoulderLocation slot,
                int responseDelayTicks,
                boolean previousFrozenMovement,
                boolean previousNoGravity,
                boolean previousSitting,
                String previousGoalName,
                boolean wasLookingAtEntity) {
            return new Performance(
                    player,
                    playerUuid,
                    initiatingStack,
                    variant,
                    dimension,
                    sourcePosition,
                    startTick,
                    sound,
                    noteProfile,
                    Mode.SHOULDER_CALL,
                    target,
                    slot,
                    responseDelayTicks,
                    previousFrozenMovement,
                    previousNoGravity,
                    previousSitting,
                    previousGoalName,
                    wasLookingAtEntity);
        }

    }
}
