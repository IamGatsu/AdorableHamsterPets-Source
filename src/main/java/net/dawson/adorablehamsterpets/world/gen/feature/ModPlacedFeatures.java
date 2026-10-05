package net.dawson.adorablehamsterpets.world.gen.feature;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.RandomOffsetPlacement;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.core.Vec3i;
import net.dawson.adorablehamsterpets.block.ModBlocks;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

import net.dawson.adorablehamsterpets.AdorableHamsterPets;
import net.dawson.adorablehamsterpets.config.AhpWorldGenConfig;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;

import java.util.List;

public class ModPlacedFeatures {

    public static final ResourceKey<PlacedFeature> CUSTOM_SUNFLOWER_PLACED_KEY = registerKey("custom_sunflower_placed");

    // --- Add Keys for Placed Bushes ---
    public static final ResourceKey<PlacedFeature> WILD_GREEN_BEAN_BUSH_PLACED_KEY = registerKey("wild_green_bean_bush_placed");
    public static final ResourceKey<PlacedFeature> WILD_CUCUMBER_BUSH_PLACED_KEY = registerKey("wild_cucumber_bush_placed");
    // --- End Add Keys ---

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        var configuredFeatureRegistryEntryLookup = context.lookup(Registries.CONFIGURED_FEATURE);
        final AhpWorldGenConfig config = AdorableHamsterPets.WORLD_GEN_CONFIG; // Access static config

        // Sunflower
        register(context, CUSTOM_SUNFLOWER_PLACED_KEY,
                configuredFeatureRegistryEntryLookup.getOrThrow(ModConfiguredFeatures.CUSTOM_SUNFLOWER_PATCH_KEY),
                RarityFilter.onAverageOnceEvery(3),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP,
                BiomeFilter.biome(),
                // Former random_patch behaviour: 64 tries spread around the origin, only into air where the plant survives
                CountPlacement.of(64),
                RandomOffsetPlacement.ofTriangle(7, 3),
                BlockPredicateFilter.forPredicate(BlockPredicate.allOf(BlockPredicate.ONLY_IN_AIR_PREDICATE, BlockPredicate.wouldSurvive(ModBlocks.SUNFLOWER_BLOCK.get().defaultBlockState(), Vec3i.ZERO)))
        );

        // --- Register Placed Green Bean Bush ---
        register(context, WILD_GREEN_BEAN_BUSH_PLACED_KEY,
                configuredFeatureRegistryEntryLookup.getOrThrow(ModConfiguredFeatures.WILD_GREEN_BEAN_BUSH_KEY),
                // Placement Modifiers:
                RarityFilter.onAverageOnceEvery(config.wildGreenBeanBushRarity.get()),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP,
                BiomeFilter.biome(),
                // Former random_patch behaviour: 18 tries spread around the origin, only into air where the plant survives
                CountPlacement.of(18),
                RandomOffsetPlacement.ofTriangle(7, 3),
                BlockPredicateFilter.forPredicate(BlockPredicate.allOf(BlockPredicate.ONLY_IN_AIR_PREDICATE, BlockPredicate.wouldSurvive(ModBlocks.WILD_GREEN_BEAN_BUSH.get().defaultBlockState(), Vec3i.ZERO)))
        );
        // --- End Register Placed Green Bean ---

        // --- Register Placed Cucumber Bush ---
        register(context, WILD_CUCUMBER_BUSH_PLACED_KEY,
                configuredFeatureRegistryEntryLookup.getOrThrow(ModConfiguredFeatures.WILD_CUCUMBER_BUSH_KEY),
                // Placement Modifiers:
                RarityFilter.onAverageOnceEvery(config.wildCucumberBushRarity.get()),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP,
                BiomeFilter.biome(),
                // Former random_patch behaviour: 18 tries spread around the origin, only into air where the plant survives
                CountPlacement.of(18),
                RandomOffsetPlacement.ofTriangle(7, 3),
                BlockPredicateFilter.forPredicate(BlockPredicate.allOf(BlockPredicate.ONLY_IN_AIR_PREDICATE, BlockPredicate.wouldSurvive(ModBlocks.WILD_CUCUMBER_BUSH.get().defaultBlockState(), Vec3i.ZERO)))
        );
        // --- End Register Placed Cucumber ---
    }

    // Helper methods (Existing)
    public static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, name));
    }

    private static void register(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> configuration,
                                 List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }

    // Overload to accept varargs for modifiers (makes registration cleaner)
    private static void register(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> configuration,
                                 PlacementModifier... modifiers) {
        register(context, key, configuration, List.of(modifiers));
    }
}