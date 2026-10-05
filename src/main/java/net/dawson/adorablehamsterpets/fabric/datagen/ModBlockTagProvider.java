package net.dawson.adorablehamsterpets.fabric.datagen;

import net.dawson.adorablehamsterpets.block.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.Identifier;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends FabricTagsProvider.BlockTagsProvider {

    private static final TagKey<Block> STORAGE_BLOCKS = TagKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath("c", "storage_blocks")
    );

    public ModBlockTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        tag(BlockTags.CROPS)
                .add(key(ModBlocks.CUCUMBER_CROP.get()))
                .add(key(ModBlocks.GREEN_BEANS_CROP.get()));

        tag(BlockTags.MAINTAINS_FARMLAND)
                .add(key(ModBlocks.CUCUMBER_CROP.get()))
                .add(key(ModBlocks.GREEN_BEANS_CROP.get()));

        tag(BlockTags.MINEABLE_WITH_AXE)
                .add(key(ModBlocks.ACORN_CRATE.get()))
                .add(key(ModBlocks.CUCUMBER_CRATE.get()))
                .add(key(ModBlocks.GREEN_BEANS_CRATE.get()))
                .add(key(ModBlocks.HAMSTER_FOOD_MIX_CRATE.get()));

        tag(STORAGE_BLOCKS)
                .add(key(ModBlocks.ACORN_CRATE.get()))
                .add(key(ModBlocks.CUCUMBER_CRATE.get()))
                .add(key(ModBlocks.GREEN_BEANS_CRATE.get()))
                .add(key(ModBlocks.HAMSTER_FOOD_MIX_CRATE.get()));
    }


    private static net.minecraft.resources.ResourceKey<Block> key(Block value) {
        return BuiltInRegistries.BLOCK.getResourceKey(value).orElseThrow();
    }
}
