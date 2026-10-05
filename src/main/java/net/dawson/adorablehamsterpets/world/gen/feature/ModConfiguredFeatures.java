package net.dawson.adorablehamsterpets.world.gen.feature;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;

import net.dawson.adorablehamsterpets.AdorableHamsterPets;
import net.dawson.adorablehamsterpets.block.ModBlocks;
import net.dawson.adorablehamsterpets.block.custom.WildCucumberBushBlock;
import net.dawson.adorablehamsterpets.block.custom.WildGreenBeanBushBlock;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class ModConfiguredFeatures {

    public static final ResourceKey<ConfiguredFeature<?, ?>> CUSTOM_SUNFLOWER_PATCH_KEY = registerKey("custom_sunflower_patch");

    // --- Add Keys for Bushes ---
    public static final ResourceKey<ConfiguredFeature<?, ?>> WILD_GREEN_BEAN_BUSH_KEY = registerKey("wild_green_bean_bush_patch");
    public static final ResourceKey<ConfiguredFeature<?, ?>> WILD_CUCUMBER_BUSH_KEY = registerKey("wild_cucumber_bush_patch");
    // --- End Add Keys ---

    // Since 26.1 there is no random_patch feature anymore: the patch spreading (tries, offset, survival check)
    // is done by placement modifiers in ModPlacedFeatures.
    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        // Sunflower (Existing)
        register(context, CUSTOM_SUNFLOWER_PATCH_KEY, Feature.SIMPLE_BLOCK,
                new SimpleBlockConfiguration(BlockStateProvider.simple(ModBlocks.SUNFLOWER_BLOCK.get())));

        // --- Register Green Bean Bush Patch ---
        register(context, WILD_GREEN_BEAN_BUSH_KEY, Feature.SIMPLE_BLOCK,
                new SimpleBlockConfiguration(BlockStateProvider.simple(ModBlocks.WILD_GREEN_BEAN_BUSH.get().defaultBlockState().setValue(WildGreenBeanBushBlock.SEEDED, true))));
        // --- End Register Green Bean ---

        // --- Register Cucumber Bush Patch ---
        register(context, WILD_CUCUMBER_BUSH_KEY, Feature.SIMPLE_BLOCK,
                new SimpleBlockConfiguration(BlockStateProvider.simple(ModBlocks.WILD_CUCUMBER_BUSH.get().defaultBlockState().setValue(WildCucumberBushBlock.SEEDED, true))));
        // --- End Register Cucumber ---
    }

    // Helper methods (Existing)
    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, name));
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(BootstrapContext<ConfiguredFeature<?, ?>> context,
                                                                                   ResourceKey<ConfiguredFeature<?, ?>> key, F feature, FC configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }
}