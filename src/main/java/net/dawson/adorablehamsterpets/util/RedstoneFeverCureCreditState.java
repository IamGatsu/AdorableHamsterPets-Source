package net.dawson.adorablehamsterpets.util;

import net.dawson.adorablehamsterpets.advancement.criterion.ModCriteria;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Server-wide pending advancement credit for players offline when sunlight curing completes.
 */
public final class RedstoneFeverCureCreditState extends SavedData {

    /* ─────────────────────────────────────────────────────────────────────────────
     *        Constants
     * ─────────────────────────────────────────────────────────────────────────────*/

    private static final String STORAGE_KEY = "adorablehamsterpets_redstone_fever_cure_credit";
    // Since 1.21.5 saved data is codec based. The data still lives in "Players" (list of UUID strings),
    // and the default namespace keeps the old file name in the world's data folder.
    private static final com.mojang.serialization.Codec<RedstoneFeverCureCreditState> CODEC =
            com.mojang.serialization.codecs.RecordCodecBuilder.create(instance -> instance.group(
                    net.minecraft.core.UUIDUtil.STRING_CODEC.listOf().optionalFieldOf("Players", java.util.List.of())
                            .forGetter(state -> java.util.List.copyOf(state.pendingPlayers))
            ).apply(instance, players -> {
                RedstoneFeverCureCreditState state = new RedstoneFeverCureCreditState();
                state.pendingPlayers.addAll(players);
                return state;
            }));
    private static final net.minecraft.world.level.saveddata.SavedDataType<RedstoneFeverCureCreditState> TYPE =
            new net.minecraft.world.level.saveddata.SavedDataType<>(
                    net.minecraft.resources.Identifier.withDefaultNamespace(STORAGE_KEY),
                    RedstoneFeverCureCreditState::new,
                    CODEC,
                    DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    /* ──────────────────────────────────────────────────────────────────────────────
     *        Instance Fields
     * ─────────────────────────────────────────────────────────────────────────────*/

    private final Set<UUID> pendingPlayers = new HashSet<>();

    /* ─────────────────────────────────────────────────────────────────────────────
     *        Static Utilities
     * ─────────────────────────────────────────────────────────────────────────────*/

    public static void awardOrQueue(ServerLevel world, UUID playerUuid) {
        ServerPlayer player = world.getServer().getPlayerList().getPlayer(playerUuid);
        if (player != null) {
            // Online credit resolves immediately and never enters persistent queue
            ModCriteria.SUNSHINE_CURING.get().trigger(player);
            return;
        }
        RedstoneFeverCureCreditState state = get(world);
        if (state.pendingPlayers.add(playerUuid)) state.setDirty();
    }

    public static void consume(ServerPlayer player) {
        // Set removal makes reconnect consumption idempotent
        RedstoneFeverCureCreditState state = get(player.level());
        if (state.pendingPlayers.remove(player.getUUID())) {
            ModCriteria.SUNSHINE_CURING.get().trigger(player);
            state.setDirty();
        }
    }

    /* ────────────────────────────────────────────────────────────────────────────
     *        Overrides
     * ───────────────────────────────────────────────────────────────────────────────*/

    /* ─────────────────────────────────────────────────────────────────────────────
     *        Private Helpers
     * ────────────────────────────────────────────────────────────────────────────────*/

    private static RedstoneFeverCureCreditState get(ServerLevel world) {
        // Overworld manager shares one queue across every cure dimension
        return world.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
    }


}
