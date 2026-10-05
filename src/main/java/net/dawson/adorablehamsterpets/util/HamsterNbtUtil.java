package net.dawson.adorablehamsterpets.util;

import net.dawson.adorablehamsterpets.AdorableHamsterPets;
import net.dawson.adorablehamsterpets.entity.ModEntities;
import net.dawson.adorablehamsterpets.entity.custom.HamsterEntity;
import net.dawson.adorablehamsterpets.entity.custom.genetics.HamsterGenome;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Isolates NBT serialization and deserialization logic for Hamsters.
 */
public final class HamsterNbtUtil {

    private HamsterNbtUtil() {}

    /* ──────────────────────────────────────────────────────────────────────────────
     *                        Core Data Serialization
     * ────────────────────────────────────────────────────────────────────────────*/

    public static void addAdditionalSaveData(HamsterEntity hamster, CompoundTag nbt) {
        // --- 1. Core Data & Flags ---
        nbt.put("HamsterGenome", hamster.getGenome().saveToNbt());
        nbt.putLong("TotalAgeTicks", hamster.totalAgeTicks);
        nbt.putInt("TimesBred", hamster.timesBred);
        // Write flags as individual booleans for backwards compat
        if (hamster.isTame()) {
            nbt.putBoolean("Sitting", hamster.getHamsterFlag(HamsterEntity.SITTING_FLAG));
            nbt.putBoolean("IsSleeping", hamster.getHamsterFlag(HamsterEntity.SLEEPING_FLAG));
        } else {
            nbt.putBoolean("IsSleeping", false);
        }
        nbt.putBoolean("KnockedOut", hamster.getHamsterFlag(HamsterEntity.KNOCKED_OUT_FLAG));
        nbt.putInt("KnockedOutTimer", hamster.knockedOutTimer);
        nbt.putBoolean("CheekPouchUnlocked", hamster.getHamsterFlag(HamsterEntity.CHEEK_POUCH_UNLOCKED_FLAG));
        nbt.putLong("ThrowCooldownEnd", hamster.throwCooldownEndTick);
        nbt.putLong("GreenBeanBuffDuration", hamster.getEntityData().get(HamsterEntity.GREEN_BEAN_BUFF_DURATION));
        nbt.putInt("AutoEatCooldown", hamster.getAutoEatCooldownTicks());
        nbt.putInt("EjectionCheckCooldown", hamster.getEjectionCheckCooldown());
        nbt.putInt("FlowerPosition", hamster.getEntityData().get(HamsterEntity.FLOWER_POS));
        nbt.putInt("AnimationPersonalityId", hamster.getEntityData().get(HamsterEntity.ANIMATION_PERSONALITY_ID));
        nbt.putBoolean("ArmorVisible", hamster.isArmorVisible());
        nbt.putBoolean("isGeneticsVisualizerMember", hamster.isGeneticsVisualizerMember());
        nbt.putInt("AggressionState", hamster.getAggressionState().ordinal());
        hamster.getRedstoneFeverState().saveWithoutMetadata(nbt);
        hamster.getFluteProgressState().saveWithoutMetadata(nbt);

        // --- 2. Parent Following ---
        if (hamster.getParentUuid() != null) {
            net.dawson.adorablehamsterpets.util.NbtCompat.putUUID(nbt, "ParentUuid", hamster.getParentUuid());
        }

        // --- 3. Sleep State ---
        nbt.putInt("DozingPhase", hamster.getDozingPhase().ordinal());
        nbt.putString("CurrentDeepSleepAnimId", hamster.getEntityData().get(HamsterEntity.CURRENT_DEEP_SLEEP_ANIM_ID));
        nbt.putInt("QuiescentSitTimer", hamster.getQuiescentSitTimer());
        nbt.putInt("DriftingOffTimer", hamster.getDriftingOffTimer());
        nbt.putInt("SettleSleepCooldown", hamster.getSettleSleepCooldown());

        // --- 4. Inventory ---
        HolderLookup.Provider registries = hamster.level().registryAccess();
        CompoundTag inventoryWrapperNbt = new CompoundTag();
        net.dawson.adorablehamsterpets.util.NbtCompat.saveItems(inventoryWrapperNbt, hamster.getItems(), registries);
        nbt.put("Inventory", inventoryWrapperNbt);

        // --- 5. Ore Seeking ---
        nbt.putBoolean("IsPrimedToSeekDiamonds", hamster.isPrimedToSeekDiamonds);
        nbt.putLong("FoundOreCooldownEndTick", hamster.foundOreCooldownEndTick);
        if (hamster.currentOreTarget != null) {
            nbt.putInt("OreTargetX", hamster.currentOreTarget.getX());
            nbt.putInt("OreTargetY", hamster.currentOreTarget.getY());
            nbt.putInt("OreTargetZ", hamster.currentOreTarget.getZ());
        }
        nbt.putBoolean("IsCelebratingDiamond", hamster.getHamsterFlag(HamsterEntity.CELEBRATING_DIAMOND_FLAG));

        // --- 6. Interaction & Mini-Game ---
        nbt.putBoolean("IsSulking", hamster.getHamsterFlag(HamsterEntity.SULKING_FLAG));
        nbt.putInt("SulkTimer", hamster.sulkTimer);
        nbt.putLong("TagGameCooldownEnd", hamster.tagGameCooldownEndTick);
        nbt.putLong("StealingCooldownEnd", hamster.stealingCooldownEndTick);
        nbt.putLong("CropSnackCooldownEnd", hamster.cropSnackCooldownEndTick);
        nbt.putLong("HideAndSeekCooldownEnd", hamster.hideAndSeekCooldownEndTick);
        if (hamster.getGenericInteractionTimer() > 0) {
            nbt.putInt("GenericInteractionTimer", hamster.getGenericInteractionTimer());
        }
        if (hamster.isHoldingMouthItem()) {
            nbt.putBoolean("IsHoldingMouthItem", true);
            if (!hamster.getMouthItemStack().isEmpty()) {
                nbt.put("MouthItemStack", net.dawson.adorablehamsterpets.util.NbtCompat.encodeStack(hamster.getMouthItemStack(), registries));
            }
        }

        // --- 7. Wander Mode ---
        nbt.putBoolean("IsWanderModeActive", hamster.isWanderModeActive());
        hamster.getLinkedBedPos().ifPresent(globalPos ->
                nbt.put("LinkedBedPos", GlobalPos.CODEC.encodeStart(hamster.level().registryAccess().createSerializationContext(NbtOps.INSTANCE), globalPos).getOrThrow()));
        nbt.putBoolean("BypassNextSleepDelay", hamster.shouldBypassNextSleepDelay());
        nbt.putBoolean("StuckSearchingForBed", hamster.isStuckSearchingForBed());
        nbt.putBoolean("IsRescueSleeping", hamster.isRescueSleeping());
    }

    public static void readAdditionalSaveData(HamsterEntity hamster, CompoundTag nbt) {
        // --- 1. Read Core Data ---
        hamster.setLoadingNbt(true); // Suppress sounds
        hamster.totalAgeTicks = nbt.getLongOr("TotalAgeTicks", 0L);
        hamster.timesBred = nbt.getIntOr("TimesBred", 0);
        // Migrate legacy variant IDs to v3.6.0's Genome structure
        if (nbt.contains("HamsterGenome")) {
            hamster.setGenome(HamsterGenome.readFromNbt(nbt.getCompoundOrEmpty("HamsterGenome")));
        } else if (nbt.contains("HamsterVariant")) {
            // Catch old integer IDs from pre 3.6.0
            int legacyId = nbt.getIntOr("HamsterVariant", 0);
            hamster.setGenome(HamsterGeneticsUtil.getGenomeForLegacyId(legacyId));
        } else {
            hamster.setGenome(HamsterGenome.createDefault());
        }
        // Backwards compat: read individual booleans & set flags
        boolean wasSittingNbt = hamster.isTame() && nbt.getBooleanOr("Sitting", false);
        hamster.setOrderedToSit(wasSittingNbt, true); // This will correctly set the SITTING_FLAG
        boolean loadedKnockedOut = nbt.getBooleanOr("KnockedOut", false);
        hamster.setHamsterFlag(HamsterEntity.KNOCKED_OUT_FLAG, loadedKnockedOut);
        hamster.setHamsterFlag(HamsterEntity.CHEEK_POUCH_UNLOCKED_FLAG, nbt.getBooleanOr("CheekPouchUnlocked", false));
        hamster.setHamsterFlag(HamsterEntity.SULKING_FLAG, nbt.getBooleanOr("IsSulking", false));
        hamster.restoreDistressTimers(
                nbt.contains("KnockedOutTimer") ? nbt.getIntOr("KnockedOutTimer", 0) : 0,
                nbt.contains("SulkTimer") ? nbt.getIntOr("SulkTimer", 0) : 0);

        hamster.setHamsterFlag(HamsterEntity.CELEBRATING_DIAMOND_FLAG, nbt.getBooleanOr("IsCelebratingDiamond", false));
        boolean loadedSleeping = nbt.getBooleanOr("IsSleeping", false);
        if (!hamster.isTame()) {
            loadedSleeping = false;
        }
        hamster.setHamsterFlag(HamsterEntity.SLEEPING_FLAG, loadedSleeping);
        hamster.throwCooldownEndTick = nbt.getLongOr("ThrowCooldownEnd", 0L);
        hamster.setHamsterFlag(HamsterEntity.THROW_COOLDOWN_FLAG, hamster.throwCooldownEndTick > hamster.level().getGameTime());
        hamster.getEntityData().set(HamsterEntity.GREEN_BEAN_BUFF_DURATION, nbt.getLongOr("GreenBeanBuffDuration", 0L));
        hamster.setAutoEatCooldownTicks(nbt.getIntOr("AutoEatCooldown", 0));
        hamster.setEjectionCheckCooldown(nbt.contains("EjectionCheckCooldown") ? nbt.getIntOr("EjectionCheckCooldown", 0) : 20);
        // Backwards compat for old Pink Petals
        if (nbt.contains("FlowerPosition")) {
            hamster.getEntityData().set(HamsterEntity.FLOWER_POS, nbt.getIntOr("FlowerPosition", 0));
        } else if (nbt.contains("PinkPetalType")) {
            hamster.getEntityData().set(HamsterEntity.FLOWER_POS, nbt.getIntOr("PinkPetalType", 0));
        }
        // Backwards compat: personality ID verification
        if (!nbt.contains("AnimationPersonalityId")) {
            int personalityId = hamster.getRandom().nextIntBetweenInclusive(1, 3);
            hamster.getEntityData().set(HamsterEntity.ANIMATION_PERSONALITY_ID, personalityId);
            AdorableHamsterPets.LOGGER.debug("[NBT READ] Hamster ID {}: NBT had no personality, assigned new ID {}", hamster.getId(), personalityId);
        } else {
            hamster.getEntityData().set(HamsterEntity.ANIMATION_PERSONALITY_ID, nbt.getIntOr("AnimationPersonalityId", 0));
        }
        hamster.setArmorVisible(!nbt.contains("ArmorVisible") || nbt.getBooleanOr("ArmorVisible", false));
        hamster.setGeneticsVisualizerMember(nbt.getBooleanOr("isGeneticsVisualizerMember", false));
        if (nbt.contains("AggressionState")) {
            int stateOrdinal = nbt.getIntOr("AggressionState", 0);
            if (stateOrdinal >= 0 && stateOrdinal < HamsterEntity.AggressionState.values().length) {
                hamster.setAggressionState(HamsterEntity.AggressionState.values()[stateOrdinal]);
            }
        }
        hamster.getRedstoneFeverState().readNbt(nbt);
        hamster.getFluteProgressState().readNbt(nbt);
        RedstoneFeverUtil.normalizeDisabledState(hamster);
        hamster.synchronizeRedstoneFeverVisualState();

        // --- 2. Parent Following ---
        if (net.dawson.adorablehamsterpets.util.NbtCompat.hasUUID(nbt, "ParentUuid")) {
            hamster.setParentUuid(net.dawson.adorablehamsterpets.util.NbtCompat.getUUID(nbt, "ParentUuid"));
        }

        // --- 3. Sleep State ---
        if (nbt.contains("DozingPhase")) {
            int phaseOrdinal = nbt.getIntOr("DozingPhase", 0);
            if (phaseOrdinal >= 0 && phaseOrdinal < HamsterEntity.DozingPhase.values().length) {
                HamsterEntity.DozingPhase phase = HamsterEntity.DozingPhase.values()[phaseOrdinal];
                hamster.setDozingPhase(phase);
                if (phase == HamsterEntity.DozingPhase.DEEP_SLEEP) {
                    hamster.setHamsterFlag(HamsterEntity.SLEEPING_FLAG, true);
                }
            } else {
                hamster.setDozingPhase(HamsterEntity.DozingPhase.NONE);
            }
        } else {
            hamster.setDozingPhase(HamsterEntity.DozingPhase.NONE);
        }
        hamster.getEntityData().set(HamsterEntity.CURRENT_DEEP_SLEEP_ANIM_ID, nbt.getStringOr("CurrentDeepSleepAnimId", ""));
        hamster.setQuiescentSitTimer(nbt.getIntOr("QuiescentSitTimer", 0));
        hamster.setDriftingOffTimer(nbt.getIntOr("DriftingOffTimer", 0));
        hamster.setSettleSleepCooldown(nbt.getIntOr("SettleSleepCooldown", 0));

        // --- 4. Inventory ---
        HolderLookup.Provider registries = hamster.level().registryAccess();
        if (nbt.contains("Inventory")) {
            hamster.getItems().clear();
            net.dawson.adorablehamsterpets.util.NbtCompat.loadItems(nbt.getCompoundOrEmpty("Inventory"), hamster.getItems(), registries);
            HamsterInventoryUtil.updateCheekStates(hamster);
            HamsterInventoryUtil.syncEquipmentTrackers(hamster);
        } else if (!hamster.level().isClientSide() && !hamster.isTame()) {
            HamsterInventoryUtil.generateWildLoot(hamster, hamster.getRandom());
            HamsterInventoryUtil.updateCheekStates(hamster);
            HamsterInventoryUtil.syncEquipmentTrackers(hamster);
        }

        // --- 5. Ore Seeking ---
        hamster.isPrimedToSeekDiamonds = nbt.getBooleanOr("IsPrimedToSeekDiamonds", false);
        hamster.foundOreCooldownEndTick = nbt.getLongOr("FoundOreCooldownEndTick", 0L);
        if (nbt.contains("OreTargetX") && nbt.contains("OreTargetY") && nbt.contains("OreTargetZ")) {
            hamster.currentOreTarget = new BlockPos(nbt.getIntOr("OreTargetX", 0), nbt.getIntOr("OreTargetY", 0), nbt.getIntOr("OreTargetZ", 0));
        } else {
            hamster.currentOreTarget = null;
        }

        // --- 6. Interaction & Mini-Game ---
        hamster.tagGameCooldownEndTick = nbt.getLongOr("TagGameCooldownEnd", 0L);
        hamster.stealingCooldownEndTick = nbt.getLongOr("StealingCooldownEnd", 0L);
        hamster.cropSnackCooldownEndTick = nbt.getLongOr("CropSnackCooldownEnd", 0L);
        hamster.hideAndSeekCooldownEndTick = nbt.getLongOr("HideAndSeekCooldownEnd", 0L);
        hamster.setGenericInteractionTimer(nbt.getIntOr("GenericInteractionTimer", 0));

        boolean holding = nbt.getBooleanOr("IsHoldingMouthItem", false);
        hamster.setHoldingMouthItem(holding);

        if (holding) {
            if (nbt.contains("MouthItemStack")) {
                net.dawson.adorablehamsterpets.util.NbtCompat.decodeStack(registries, nbt.getCompoundOrEmpty("MouthItemStack")).ifPresent(hamster::setMouthItemStack);
            }
        } else {
            hamster.setMouthItemStack(ItemStack.EMPTY);
        }

        // --- 7. Wander Mode ---
        hamster.setWanderModeActive(nbt.getBooleanOr("IsWanderModeActive", false));
        if (nbt.contains("LinkedBedPos")) {
            hamster.setLinkedBedPos(GlobalPos.CODEC.parse(hamster.level().registryAccess().createSerializationContext(NbtOps.INSTANCE), nbt.get("LinkedBedPos")).result());
        } else {
            hamster.setLinkedBedPos(Optional.empty());
        }
        hamster.setBypassNextSleepDelay(nbt.getBooleanOr("BypassNextSleepDelay", false));
        hamster.setStuckSearchingForBed(nbt.getBooleanOr("StuckSearchingForBed", false));
        hamster.setRescueSleeping(nbt.getBooleanOr("IsRescueSleeping", false));
        if (hamster.isRescueSleeping()) {
            hamster.setHamsterFlag(HamsterEntity.SLEEPING_FLAG, true);
        }

        // --- 8. Reconcile Accessory State ---
        hamster.updateAccessoryState();

        hamster.setLoadingNbt(false);
    }

    /* ──────────────────────────────────────────────────────────────────────────────
     *                       Shoulder Data Handlers
     * ────────────────────────────────────────────────────────────────────────────*/

    /**
     * Takes a hamster's NBT data, deserializes it, sets the knocked-out flag,
     * and re-serializes it to a new CompoundTag.
     */
    public static CompoundTag setKnockedOutInNbt(CompoundTag originalNbt) {
        return HamsterState.fromNbt(originalNbt).map(data -> {
            int newFlags = data.hamsterFlags() | HamsterEntity.KNOCKED_OUT_FLAG;
            return data.withFlags(newFlags).toNbt();
        }).orElse(originalNbt); // Fallback
    }

    /**
     * Captures the current state of this hamster into a {@link HamsterState} record.
     */
    public static HamsterState saveToHamsterState(HamsterEntity hamster) {
        // --- 1. Update Trackers and Prepare NBT ---
        HamsterInventoryUtil.updateCheekStates(hamster);
        CompoundTag inventoryNbt = new CompoundTag();
        if (hamster.level() instanceof ServerLevel serverWorld) {
            net.dawson.adorablehamsterpets.util.NbtCompat.saveItems(inventoryNbt, hamster.getItems(), serverWorld.registryAccess());
        }

        // --- 2. Save Active Status Effects to NBT ---
        CompoundTag effectsNbt = new CompoundTag();
        if (!hamster.getActiveEffects().isEmpty()) {
            ListTag effectsList = new ListTag();
            for (MobEffectInstance effectInstance : hamster.getActiveEffects()) {
                effectsList.add(net.dawson.adorablehamsterpets.util.NbtCompat.saveEffect(effectInstance));
            }
            effectsNbt.put("active_effects", effectsList);
        }

        // --- 3. Get Custom Name ---
        Optional<String> nameOptional = Optional.ofNullable(hamster.getCustomName()).map(Component::getString);

        // --- 4. Create Domain Transfer Records ---
        HamsterState.MiniGameBehaviorData seekingData = new HamsterState.MiniGameBehaviorData(
                hamster.isPrimedToSeekDiamonds,
                hamster.foundOreCooldownEndTick,
                hamster.cropSnackCooldownEndTick,
                hamster.hideAndSeekCooldownEndTick,
                Optional.ofNullable(hamster.currentOreTarget)
        );
        HamsterState.GreenBeanBuffData buffData = new HamsterState.GreenBeanBuffData(
                hamster.getGreenBeanBuffEndTick(),
                hamster.getEntityData().get(HamsterEntity.GREEN_BEAN_BUFF_DURATION),
                effectsNbt
        );
        HamsterState.WanderModeData wanderData = new HamsterState.WanderModeData(
                hamster.getLinkedBedPos(),
                hamster.shouldBypassNextSleepDelay()
        );

        HamsterState.IdentityData identityData = new HamsterState.IdentityData(
                hamster.getUUID(),
                hamster.getGenome().saveToNbt(),
                nameOptional
        );
        HamsterState.LifeHistoryData lifeHistoryData = new HamsterState.LifeHistoryData(
                hamster.getAge(),
                hamster.totalAgeTicks,
                hamster.timesBred
        );
        HamsterState.StatusData statusData = new HamsterState.StatusData(
                hamster.throwCooldownEndTick,
                buffData,
                hamster.getAutoEatCooldownTicks()
        );
        HamsterState.AppearanceData appearanceData = new HamsterState.AppearanceData(
                hamster.getEntityData().get(HamsterEntity.FLOWER_POS),
                hamster.getEntityData().get(HamsterEntity.ANIMATION_PERSONALITY_ID),
                hamster.isArmorVisible()
        );
        HamsterState.BehaviorData behaviorData = new HamsterState.BehaviorData(
                seekingData,
                wanderData,
                hamster.getEntityData().get(HamsterEntity.HAMSTER_FLAGS),
                hamster.knockedOutTimer,
                hamster.sulkTimer
        );
        HamsterState.HamsterConditionData conditionData =
                HamsterState.HamsterConditionData.capture(hamster.getRedstoneFeverState());

        // --- 5. Create and Return Main Data Record ---
        return new HamsterState(
                identityData,
                hamster.getHealth(),
                inventoryNbt,
                lifeHistoryData,
                statusData,
                appearanceData,
                behaviorData,
                conditionData,
                hamster.getFluteProgressState().createTransferData()
        );
    }

    /**
     * Creates a HamsterEntity instance from NBT data, typically from a player's shoulder.
     * Loads the hamster's variant, health, age, inventory, effects, and custom name.
     * Does NOT set the entity's position or spawn it in the world.
     */
    @Nullable
    public static HamsterEntity createFromNbt(ServerLevel world, @Nullable Player player, CompoundTag nbt) {
        Optional<HamsterState> dataOpt = HamsterState.fromNbt(nbt);
        if (dataOpt.isEmpty()) {
            AdorableHamsterPets.LOGGER.error("Failed to deserialize HamsterState from NBT: {}", nbt);
            return null;
        }
        HamsterState data = dataOpt.get();

        HamsterEntity hamster = ModEntities.HAMSTER.get().create(world, net.minecraft.world.entity.EntitySpawnReason.LOAD);

        if (hamster != null) {
            // --- 1. Load Core Data ---
            hamster.setUUID(data.entityUuid());
            hamster.setGenome(HamsterGenome.readFromNbt(data.genomeNbt()));
            hamster.setHealth(data.health());
            if (player != null) {hamster.setOwnerReference(net.minecraft.world.entity.EntityReference.of(player.getUUID()));}
            data.conditionData().applyTo(hamster.getRedstoneFeverState());
            hamster.getFluteProgressState().applyTransferData(data.fluteProgressData());
            RedstoneFeverUtil.normalizeDisabledState(hamster);
            hamster.synchronizeRedstoneFeverVisualState();
            hamster.setTame(true, true);
            hamster.setAge(data.breedingAge());
            hamster.throwCooldownEndTick = data.throwCooldownEndTick();
            hamster.setAutoEatCooldownTicks(data.autoEatCooldownTicks());
            hamster.getEntityData().set(HamsterEntity.FLOWER_POS, data.flowerPosition());
            hamster.getEntityData().set(HamsterEntity.ANIMATION_PERSONALITY_ID, data.animationPersonalityId());
            hamster.getEntityData().set(HamsterEntity.HAMSTER_FLAGS, data.hamsterFlags());
            // Begging is a transient state driven by the tempt goal; never restore it from saved data,
            // otherwise a hamster picked up while begging keeps begging after being set down.
            hamster.setBegging(false);
            hamster.restoreDistressTimers(
                    data.behaviorData().knockedOutTimer(),
                    data.behaviorData().sulkTimer());
            hamster.setArmorVisible(data.armorVisible());
            hamster.totalAgeTicks = data.totalAgeTicks();
            hamster.timesBred = data.timesBred();

            // Sync vanilla sitting pose with restored flag
            hamster.setInSittingPose(
                    hamster.isKnockedOut() || hamster.getHamsterFlag(HamsterEntity.SITTING_FLAG));

            // --- 2. Load Custom Name ---
            data.customName().ifPresent(name -> {
                if (!name.isEmpty()) {
                    hamster.setCustomName(Component.literal(name));
                }
            });

            // --- 3. Load Inventory ---
            HolderLookup.Provider registries = world.registryAccess();
            if (!data.inventoryNbt().isEmpty()) {
                net.dawson.adorablehamsterpets.util.NbtCompat.loadItems(data.inventoryNbt(), hamster.getItems(), registries);
                HamsterInventoryUtil.updateCheekStates(hamster);
                HamsterInventoryUtil.syncEquipmentTrackers(hamster);
            }

            // --- 4. Load Green Bean Buff Data/Status Effects ---
            HamsterState.GreenBeanBuffData buffData = data.greenBeanBuffData();
            hamster.setGreenBeanBuffEndTick(buffData.greenBeanBuffEndTick());
            hamster.getEntityData().set(HamsterEntity.GREEN_BEAN_BUFF_DURATION, buffData.greenBeanBuffDuration());
            CompoundTag effectsNbt = buffData.activeEffectsNbt();
            if (effectsNbt.contains("active_effects")) {
                ListTag effectsList = effectsNbt.getListOrEmpty("active_effects");
                for (Tag effectElement : effectsList) {
                    if (effectElement instanceof CompoundTag effectInstanceNbt) {
                        MobEffectInstance effectInstance = net.dawson.adorablehamsterpets.util.NbtCompat.loadEffect(effectInstanceNbt);
                        if (effectInstance != null) {
                            hamster.addEffect(effectInstance);
                        }
                    }
                }
            }

            // --- 5. Load Diamond Seeking Data ---
            HamsterState.MiniGameBehaviorData seekingData = data.seekingBehaviorData();
            hamster.isPrimedToSeekDiamonds = seekingData.isPrimedToSeekDiamonds();
            hamster.foundOreCooldownEndTick = seekingData.foundOreCooldownEndTick();
            hamster.cropSnackCooldownEndTick = seekingData.cropSnackCooldownEndTick();
            hamster.hideAndSeekCooldownEndTick = seekingData.hideAndSeekCooldownEndTick();
            hamster.currentOreTarget = seekingData.currentOreTarget().orElse(null);

            // --- 6. Load Wander Mode/Bed Data ---
            HamsterState.WanderModeData wanderData = data.wanderModeData();
            hamster.setLinkedBedPos(wanderData.linkedBedPos());
            hamster.setBypassNextSleepDelay(wanderData.bypassNextSleepDelay());

            // --- 7. Reset Transient Action Flags ---
            hamster.setHamsterFlag(HamsterEntity.CLEANING_FLAG, false);
            hamster.setDozingPhase(HamsterEntity.DozingPhase.NONE);

            // --- 8. Reconcile Accessory State ---
            hamster.updateAccessoryState();
        }
        return hamster;
    }

    /* ──────────────────────────────────────────────────────────────────────────────
     *                               Private Helpers
     * ────────────────────────────────────────────────────────────────────────────*/

    private static boolean hasInventoryData(CompoundTag nbt) {
        return nbt.contains("Inventory");
    }
}
