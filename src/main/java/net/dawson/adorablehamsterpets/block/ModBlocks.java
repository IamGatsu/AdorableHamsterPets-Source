package net.dawson.adorablehamsterpets.block;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.dawson.adorablehamsterpets.AdorableHamsterPets;
import net.dawson.adorablehamsterpets.block.custom.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.SoundType;

import java.util.function.Supplier;

public class ModBlocks {

    // --- 1. Create a DeferredRegister for Blocks ---
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(AdorableHamsterPets.MOD_ID, Registries.BLOCK);

    // --- 2. Change Block fields to RegistrySuppliers ---
    // Note: crops/bushes no longer copy WHEAT/SWEET_BERRY_BUSH properties, because since 26.x those carry
    // state-dependent map colors that read vanilla's AGE property (crash with the custom AGE_3 property).
    public static final RegistrySupplier<Block> GREEN_BEANS_CROP = registerBlock("green_beans_crop",
            () -> new GreenBeansCropBlock(blockProps().mapColor(MapColor.PLANT).noCollision().randomTicks().instabreak().sound(SoundType.CROP).pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY).noOcclusion()));

    public static final RegistrySupplier<Block> CUCUMBER_CROP = registerBlock("cucumber_crop",
            () -> new CucumberCropBlock(blockProps().mapColor(MapColor.PLANT).noCollision().randomTicks().instabreak().sound(SoundType.CROP).pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY).noOcclusion()));

    public static final RegistrySupplier<Block> WILD_GREEN_BEAN_BUSH = registerBlock("wild_green_bean_bush",
            () -> new WildGreenBeanBushBlock(blockProps().mapColor(MapColor.PLANT).pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
                    .noOcclusion()
                    .noCollision()
                    .randomTicks()
                    .sound(SoundType.SWEET_BERRY_BUSH)));

    public static final RegistrySupplier<Block> WILD_CUCUMBER_BUSH = registerBlock("wild_cucumber_bush",
            () -> new WildCucumberBushBlock(blockProps().mapColor(MapColor.PLANT).pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
                    .noOcclusion()
                    .noCollision()
                    .randomTicks()
                    .sound(SoundType.SWEET_BERRY_BUSH)));

    public static final RegistrySupplier<Block> SUNFLOWER_BLOCK = registerBlock("sunflower_block",
            () -> new SunflowerBlock(blockProps(Blocks.SUNFLOWER)
                    .noOcclusion()
                    .lightLevel(state -> state.getValue(SunflowerBlock.LIT) ? 15 : 0)));

    public static final RegistrySupplier<Block> HAMSTER_BED = registerBlock("hamster_bed",
            () -> new HamsterBedBlock(blockProps().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(0.5F).sound(SoundType.WOOD).noOcclusion()));

    // --- Crates ---
    public static final RegistrySupplier<Block> ACORN_CRATE = registerBlock("acorn_crate",
            () -> new Block(blockProps(Blocks.OAK_PLANKS).sound(SoundType.WOOD).strength(2.0F, 3.0F)));

    public static final RegistrySupplier<Block> CUCUMBER_CRATE = registerBlock("cucumber_crate",
            () -> new Block(blockProps(Blocks.OAK_PLANKS).sound(SoundType.WOOD).strength(2.0F, 3.0F)));

    public static final RegistrySupplier<Block> GREEN_BEANS_CRATE = registerBlock("green_beans_crate",
            () -> new Block(blockProps(Blocks.OAK_PLANKS).sound(SoundType.WOOD).strength(2.0F, 3.0F)));

    public static final RegistrySupplier<Block> HAMSTER_FOOD_MIX_CRATE = registerBlock("hamster_food_mix_crate",
            () -> new Block(blockProps(Blocks.OAK_PLANKS).sound(SoundType.WOOD).strength(2.0F, 3.0F)));

    // --- 3. Private Helper Method for Block Registration ---
    private static RegistrySupplier<Block> registerBlock(String name, Supplier<Block> blockSupplier) {
        net.minecraft.resources.ResourceKey<Block> key = net.minecraft.resources.ResourceKey.create(Registries.BLOCK,
                net.minecraft.resources.Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, name));
        return BLOCKS.register(name, () -> {
            CURRENT_KEY.set(key);
            try {
                return blockSupplier.get();
            } finally {
                CURRENT_KEY.remove();
            }
        });
    }

    // Since 1.21.2 every block needs its registry key in its properties before construction.
    private static final ThreadLocal<net.minecraft.resources.ResourceKey<Block>> CURRENT_KEY = new ThreadLocal<>();

    private static BlockBehaviour.Properties blockProps() {
        return BlockBehaviour.Properties.of().setId(CURRENT_KEY.get());
    }

    private static BlockBehaviour.Properties blockProps(Block copyFrom) {
        return BlockBehaviour.Properties.ofFullCopy(copyFrom).setId(CURRENT_KEY.get());
    }

    // --- 4. Main Registration Call ---
    public static void register() {
        BLOCKS.register();
    }
}