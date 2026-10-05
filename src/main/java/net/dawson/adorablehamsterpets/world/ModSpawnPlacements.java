package net.dawson.adorablehamsterpets.world;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.function.Supplier;

/**
 * Common class for registering entity spawn restrictions using an @ExpectPlatform bridge.
 * The actual implementation is provided by each loader.
 */
public final class ModSpawnPlacements {
    public static <T extends Mob> void register(Supplier<? extends EntityType<T>> entityType, SpawnPlacementType location, Heightmap.Types heightmapType, SpawnPlacements.SpawnPredicate<T> predicate) {
        net.dawson.adorablehamsterpets.world.fabric.ModSpawnPlacementsImpl.register(entityType, location, heightmapType, predicate);
    }
}