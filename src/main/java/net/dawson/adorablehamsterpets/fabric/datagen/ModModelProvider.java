package net.dawson.adorablehamsterpets.fabric.datagen;

import com.geckolib.renderer.internal.GeckolibItemSpecialRenderer;
import dev.architectury.registry.registries.RegistrySupplier;
import net.dawson.adorablehamsterpets.AdorableHamsterPets;
import net.dawson.adorablehamsterpets.block.ModBlocks;
import net.dawson.adorablehamsterpets.block.custom.CucumberCropBlock;
import net.dawson.adorablehamsterpets.block.custom.GreenBeansCropBlock;
import net.dawson.adorablehamsterpets.block.custom.WildCucumberBushBlock;
import net.dawson.adorablehamsterpets.block.custom.WildGreenBeanBushBlock;
import net.dawson.adorablehamsterpets.item.ModItems;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Optional;

public class ModModelProvider extends FabricModelProvider {

    public ModModelProvider(FabricPackOutput output) {
        super(output);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, path);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators generator) {
        // --- 1. Crops (max age 3) ---
        generator.createCropBlock(ModBlocks.GREEN_BEANS_CROP.get(), GreenBeansCropBlock.AGE, 0, 1, 2, 3);
        generator.createCropBlock(ModBlocks.CUCUMBER_CROP.get(), CucumberCropBlock.AGE, 0, 1, 2, 3);

        // --- 2. Wild Bushes ---
        createSeededBush(generator, ModBlocks.WILD_GREEN_BEAN_BUSH.get(), WildGreenBeanBushBlock.SEEDED,
                id("block/wild_green_bean_bush_seeded"), id("block/wild_green_bean_bush_seedless"));
        createSeededBush(generator, ModBlocks.WILD_CUCUMBER_BUSH.get(), WildCucumberBushBlock.SEEDED,
                id("block/wild_cucumber_bush_seeded"), id("block/wild_cucumber_bush_seedless"));

        // --- 3. Hamster Beds (block models per wood type; the blockstate file is a static resource) ---
        generateHamsterBedVariantModels(generator);

        // --- 4. Crates (models are static resources) ---
        createSimple(generator, ModBlocks.ACORN_CRATE.get(), id("block/acorn_crate"));
        createSimple(generator, ModBlocks.CUCUMBER_CRATE.get(), id("block/cucumber_crate"));
        createSimple(generator, ModBlocks.GREEN_BEANS_CRATE.get(), id("block/green_beans_crate"));
        createSimple(generator, ModBlocks.HAMSTER_FOOD_MIX_CRATE.get(), id("block/hamster_food_mix_crate"));
    }

    private static void createSimple(BlockModelGenerators generator, Block block, Identifier model) {
        generator.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, BlockModelGenerators.plainVariant(model)));
    }

    private static void createSeededBush(BlockModelGenerators generator, Block block,
                                         net.minecraft.world.level.block.state.properties.BooleanProperty seeded,
                                         Identifier seededTexture, Identifier seedlessTexture) {
        Identifier seededModel = ModelTemplates.CROSS.createWithSuffix(block, "_seeded", TextureMapping.cross(new Material(seededTexture)), generator.modelOutput);
        Identifier seedlessModel = ModelTemplates.CROSS.createWithSuffix(block, "_seedless", TextureMapping.cross(new Material(seedlessTexture)), generator.modelOutput);
        generator.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(BlockModelGenerators.createBooleanModelDispatch(seeded,
                        BlockModelGenerators.plainVariant(seededModel),
                        BlockModelGenerators.plainVariant(seedlessModel))));
    }

    @Override
    public void generateItemModels(ItemModelGenerators generator) {
        // --- 1. Flat items (model + item definition) ---
        List<Item> flatItems = List.of(
                ModItems.ANNOUNCEMENT_BELL_ICON.get(), ModItems.HAMSTER_SPAWN_EGG.get(), ModItems.HAMSTER_BEDDING.get(),
                ModItems.MUSIC_DISC_CHEESE.get(), ModItems.MUSIC_DISC_BLUE_CHEESE.get(),
                ModItems.MUSIC_DISC_PARMESAN.get(), ModItems.MUSIC_DISC_ACORN.get(),
                ModItems.CHEESE.get(), ModItems.HAMSTER_FOOD_MIX.get(), ModItems.CUCUMBER.get(),
                ModItems.SLICED_CUCUMBER.get(), ModItems.GREEN_BEANS.get(), ModItems.STEAMED_GREEN_BEANS.get(),
                ModItems.SUNFLOWER_SEEDS.get(),
                // green_bean_seeds / cucumber_seeds: generated by createCropBlock (the crops' seed items)
                ModBlocks.WILD_GREEN_BEAN_BUSH.get().asItem(), ModBlocks.WILD_CUCUMBER_BUSH.get().asItem(),
                ModItems.ACORN.get(), ModItems.ACORN_SHARD.get(), ModItems.ACORN_HAT.get(), ModItems.ACORN_RING.get(),
                ModItems.ACORN_FLUTE_LUSH.get(), ModItems.ACORN_FLUTE_EMBER.get(), ModItems.ACORN_FLUTE_HARMONY.get(),
                ModItems.HAMSTER_ARMOR_ACORN.get(), ModItems.HAMSTER_ARMOR_IRON.get(), ModItems.HAMSTER_ARMOR_GOLD.get(),
                ModItems.HAMSTER_ARMOR_DIAMOND.get(), ModItems.HAMSTER_ARMOR_NETHERITE.get(),
                ModItems.HAMSTER_ARMOR_TRIM_SMITHING_TEMPLATE_IRON.get(), ModItems.HAMSTER_ARMOR_TRIM_SMITHING_TEMPLATE_GOLD.get(),
                ModItems.HAMSTER_ARMOR_TRIM_SMITHING_TEMPLATE_DIAMOND.get(), ModItems.HAMSTER_ARMOR_TRIM_SMITHING_TEMPLATE_NETHERITE.get());
        for (Item item : flatItems) {
            generator.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
        }

        // --- 2. Items with hand-made (static) models: only the item definition is generated ---
        generator.itemModelOutput.accept(ModItems.HAMSTER_GUIDE_BOOK.get(), ItemModelUtils.plainModel(id("item/hamster_guide_book")));
        generator.itemModelOutput.accept(ModItems.SUNFLOWER_BLOCK_ITEM.get(), ItemModelUtils.plainModel(id("item/sunflower_block")));
        generator.itemModelOutput.accept(ModItems.UPSIDE_DOWN_HAMSTER_BED_ICON.get(), ItemModelUtils.plainModel(id("item/upside_down_hamster_bed_icon")));

        // --- 3. Crates use their block model ---
        generator.itemModelOutput.accept(ModItems.ACORN_CRATE.get(), ItemModelUtils.plainModel(id("block/acorn_crate")));
        generator.itemModelOutput.accept(ModItems.CUCUMBER_CRATE.get(), ItemModelUtils.plainModel(id("block/cucumber_crate")));
        generator.itemModelOutput.accept(ModItems.GREEN_BEANS_CRATE.get(), ItemModelUtils.plainModel(id("block/green_beans_crate")));
        generator.itemModelOutput.accept(ModItems.HAMSTER_FOOD_MIX_CRATE.get(), ItemModelUtils.plainModel(id("block/hamster_food_mix_crate")));

        // --- 4. Hamster Beds: rendered by GeckoLib through its special item renderer ---
        for (RegistrySupplier<Item> bedItemSupplier : ModItems.HAMSTER_BED_ITEMS.values()) {
            generator.itemModelOutput.accept(bedItemSupplier.get(),
                    ItemModelUtils.specialModel(id("item/hamster_bed"), new GeckolibItemSpecialRenderer.Unbaked<>()));
        }
    }

    private void generateHamsterBedVariantModels(BlockModelGenerators generator) {
        List<String> woodTypes = List.of(
                "oak", "spruce", "birch", "jungle", "acacia",
                "dark_oak", "mangrove", "cherry", "bamboo", "pale_oak"
        );
        Identifier baseModelId = id("block/hamster_bed");
        ModelTemplate variantModel = new ModelTemplate(Optional.of(baseModelId), Optional.empty(), TextureSlot.PARTICLE);
        for (String wood : woodTypes) {
            TextureMapping textureMap = new TextureMapping().put(TextureSlot.PARTICLE, new Material(id("block/hamster_bed_" + wood)));
            variantModel.create(id("block/hamster_bed_" + wood), textureMap, generator.modelOutput);
        }
    }
}
