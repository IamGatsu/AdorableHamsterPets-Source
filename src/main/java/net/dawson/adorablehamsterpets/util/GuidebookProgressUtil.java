package net.dawson.adorablehamsterpets.util;

import net.dawson.adorablehamsterpets.AdorableHamsterPets;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;

public final class GuidebookProgressUtil {

    private static final Identifier RECEIVED_GUIDEBOOK =
            Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, "technical/has_received_initial_guidebook");
    private static final Identifier SAW_MISSING_GUIDEBOOK_WARNING =
            Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, "technical/has_seen_missing_guidebook_warning");

    private GuidebookProgressUtil() {}

    public static boolean hasReceivedGuidebook(ServerPlayer player) {
        return isComplete(player, RECEIVED_GUIDEBOOK);
    }

    public static boolean hasSeenMissingGuidebookWarning(ServerPlayer player) {
        return isComplete(player, SAW_MISSING_GUIDEBOOK_WARNING);
    }

    public static boolean markGuidebookReceived(ServerPlayer player) {
        return grant(player, RECEIVED_GUIDEBOOK);
    }

    public static boolean markMissingGuidebookWarningSeen(ServerPlayer player) {
        return grant(player, SAW_MISSING_GUIDEBOOK_WARNING);
    }

    private static boolean isComplete(ServerPlayer player, Identifier id) {
        AdvancementHolder advancement = player.level().getServer().getAdvancements().get(id);
        return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
    }

    private static boolean grant(ServerPlayer player, Identifier id) {
        AdvancementHolder advancement = player.level().getServer().getAdvancements().get(id);
        if (advancement == null) {
            AdorableHamsterPets.LOGGER.warn("Could not find technical advancement: {}", id);
            return false;
        }

        for (String criterion : advancement.value().criteria().keySet()) {
            player.getAdvancements().award(advancement, criterion);
        }
        return player.getAdvancements().getOrStartProgress(advancement).isDone();
    }
}
