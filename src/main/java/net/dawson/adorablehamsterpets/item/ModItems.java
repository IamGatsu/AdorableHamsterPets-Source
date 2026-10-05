package net.dawson.adorablehamsterpets.item;

import net.dawson.adorablehamsterpets.sound.ModSounds;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;

import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.dawson.adorablehamsterpets.AdorableHamsterPets;
import net.dawson.adorablehamsterpets.block.ModBlocks;
import net.dawson.adorablehamsterpets.block.custom.WoodVariant;
import net.dawson.adorablehamsterpets.config.Configs;
import net.dawson.adorablehamsterpets.entity.ModEntities;
import net.dawson.adorablehamsterpets.flute.AcornFluteVariant;
import net.dawson.adorablehamsterpets.item.custom.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Rarity;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ModItems {

    // --- 1. Create a DeferredRegister for Items ---
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(AdorableHamsterPets.MOD_ID, Registries.ITEM);

    // --- 2. Item Registrations ---

    // --- Core Items ---
    public static final RegistrySupplier<Item> HAMSTER_GUIDE_BOOK = registerItem("hamster_guide_book",
            () -> new PatchouliGuideBookItem(props().stacksTo(1)));

    public static final RegistrySupplier<Item> HAMSTER_SPAWN_EGG = registerItem("hamster_spawn_egg",
            () -> new SpawnEggItem(props().spawnEgg(ModEntities.HAMSTER.get())));

    // --- Crops & Food ---
    public static final RegistrySupplier<Item> GREEN_BEAN_SEEDS = registerItem("green_bean_seeds",
            () -> new BlockItem(ModBlocks.GREEN_BEANS_CROP.get(), props().useItemDescriptionPrefix()) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag type) {
                    if (Configs.AHP_UI.enableItemTooltips) {
                        tooltip.accept(Component.translatable("tooltip.adorablehamsterpets.green_bean_seeds.hint1").withStyle(ChatFormatting.GOLD));
                        tooltip.accept(Component.translatable("tooltip.adorablehamsterpets.green_bean_seeds.hint2").withStyle(ChatFormatting.GRAY));
                    } else if (!Platform.isModLoaded("emi")) {
                        tooltip.accept(Component.literal("Adorable Hamster Pets").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
                    }
                    super.appendHoverText(stack, context, tooltipDisplay, tooltip, type);
                }
            });

    public static final RegistrySupplier<Item> CUCUMBER_SEEDS = registerItem("cucumber_seeds",
            () -> new BlockItem(ModBlocks.CUCUMBER_CROP.get(), props().useItemDescriptionPrefix()) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag type) {
                    if (Configs.AHP_UI.enableItemTooltips) {
                        tooltip.accept(Component.translatable("tooltip.adorablehamsterpets.cucumber_seeds.hint1").withStyle(ChatFormatting.GOLD));
                        tooltip.accept(Component.translatable("tooltip.adorablehamsterpets.cucumber_seeds.hint2").withStyle(ChatFormatting.GRAY));
                    } else if (!Platform.isModLoaded("emi")) {
                        tooltip.accept(Component.literal("Adorable Hamster Pets").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
                    }
                    super.appendHoverText(stack, context, tooltipDisplay, tooltip, type);
                }
            });

    public static final RegistrySupplier<Item> SUNFLOWER_SEEDS = registerItem("sunflower_seeds",
            () -> new net.minecraft.world.item.DoubleHighBlockItem(ModBlocks.SUNFLOWER_BLOCK.get(), props().useItemDescriptionPrefix()) {

                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag type) {
                    if (Configs.AHP_UI.enableItemTooltips) {
                        tooltip.accept(Component.translatable("tooltip.adorablehamsterpets.sunflower_seeds.hint1").withStyle(ChatFormatting.GOLD));
                        tooltip.accept(Component.translatable("tooltip.adorablehamsterpets.sunflower_seeds.hint2").withStyle(ChatFormatting.GRAY));
                    } else if (!Platform.isModLoaded("emi")) {
                        tooltip.accept(Component.literal("Adorable Hamster Pets").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
                    }
                    super.appendHoverText(stack, context, tooltipDisplay, tooltip, type);
                }
            });

    public static final RegistrySupplier<Item> CUCUMBER = registerItem("cucumber",
            () -> new ConfigurableFoodItem(props().food(ModFoodComponents.CUCUMBER),
                    Configs.AHP_ITEMS.cucumberNutrition, Configs.AHP_ITEMS.cucumberSaturation,
                    "tooltip.adorablehamsterpets.cucumber"));

    public static final RegistrySupplier<Item> SLICED_CUCUMBER = registerItem("sliced_cucumber",
            () -> new ConfigurableFoodItem(props().food(ModFoodComponents.SLICED_CUCUMBER),
                    Configs.AHP_ITEMS.slicedCucumberNutrition, Configs.AHP_ITEMS.slicedCucumberSaturation,
                    "tooltip.adorablehamsterpets.sliced_cucumber"));

    public static final RegistrySupplier<Item> GREEN_BEANS = registerItem("green_beans",
            () -> new ConfigurableFoodItem(props().food(ModFoodComponents.GREEN_BEANS),
                    Configs.AHP_ITEMS.greenBeansNutrition, Configs.AHP_ITEMS.greenBeansSaturation,
                    "tooltip.adorablehamsterpets.green_beans"));

    public static final RegistrySupplier<Item> STEAMED_GREEN_BEANS = registerItem("steamed_green_beans",
            () -> new ConfigurableFoodItem(props().food(ModFoodComponents.STEAMED_GREEN_BEANS),
                    Configs.AHP_ITEMS.steamedGreenBeansNutrition, Configs.AHP_ITEMS.steamedGreenBeansSaturation,
                    "tooltip.adorablehamsterpets.steamed_green_beans"));

    public static final RegistrySupplier<Item> HAMSTER_FOOD_MIX = registerItem("hamster_food_mix",
            () -> new ConfigurableFoodItem(props().food(ModFoodComponents.HAMSTER_FOOD_MIX).stacksTo(16),
                    Configs.AHP_ITEMS.hamsterFoodMixNutrition, Configs.AHP_ITEMS.hamsterFoodMixSaturation,
                    "tooltip.adorablehamsterpets.hamster_food_mix"));

    public static final RegistrySupplier<Item> CHEESE = registerItem("cheese",
            () -> new CheeseItem(props().food(ModFoodComponents.CHEESE,
                    // 26.x: the chewing sound lives in the consumable component (was Item#getEatSound in 1.21.1)
                    net.minecraft.world.item.component.Consumables.defaultFood()
                            .consumeSeconds(1.0F) // 20 ticks, matches CheeseItem#getUseDuration
                            .sound(net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ModSounds.CHEESE_EAT1.get()))
                            .build())));

    // --- Music Discs ---
    public static final ResourceKey<JukeboxSong> CHEESE_SONG_8_BIT_KEY = ResourceKey.create(Registries.JUKEBOX_SONG, Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, "ahp_theme_song_8_bit"));
    public static final ResourceKey<JukeboxSong> BLUE_CHEESE_SONG_LOW_FI_KEY = ResourceKey.create(Registries.JUKEBOX_SONG, Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, "ahp_theme_song_low_fi"));
    public static final ResourceKey<JukeboxSong> PARMESAN_SONG_ORCHESTRAL_KEY = ResourceKey.create(Registries.JUKEBOX_SONG, Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, "ahp_theme_song_orchestral"));
    public static final ResourceKey<JukeboxSong> ACORN_SONG_ZAMPONA_KEY = ResourceKey.create(Registries.JUKEBOX_SONG, Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, "ahp_theme_song_zampona"));

    public static final RegistrySupplier<Item> MUSIC_DISC_CHEESE = registerItem("music_disc_cheese",
            () -> new Item(props().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(CHEESE_SONG_8_BIT_KEY)) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag type) {
                    if (Configs.AHP_UI.enableItemTooltips) {
                        tooltip.accept(Component.translatable("tooltip.adorablehamsterpets.music_disc_cheese.hint").withStyle(ChatFormatting.GOLD));
                    } else if (!Platform.isModLoaded("emi")) {
                        tooltip.accept(Component.literal("Adorable Hamster Pets").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
                    }
                    super.appendHoverText(stack, context, tooltipDisplay, tooltip, type);
                }
            });

    public static final RegistrySupplier<Item> MUSIC_DISC_BLUE_CHEESE = registerItem("music_disc_blue_cheese",
            () -> new Item(props().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(BLUE_CHEESE_SONG_LOW_FI_KEY)) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag type) {
                    if (Configs.AHP_UI.enableItemTooltips) {
                        tooltip.accept(Component.translatable("tooltip.adorablehamsterpets.music_disc_blue_cheese.hint").withStyle(ChatFormatting.GOLD));
                    } else if (!Platform.isModLoaded("emi")) {
                        tooltip.accept(Component.literal("Adorable Hamster Pets").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
                    }
                    super.appendHoverText(stack, context, tooltipDisplay, tooltip, type);
                }
            });

    public static final RegistrySupplier<Item> MUSIC_DISC_PARMESAN = registerItem("music_disc_parmesan",
            () -> new Item(props().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(PARMESAN_SONG_ORCHESTRAL_KEY)) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag type) {
                    if (Configs.AHP_UI.enableItemTooltips) {
                        tooltip.accept(Component.translatable("tooltip.adorablehamsterpets.music_disc_parmesan.hint").withStyle(ChatFormatting.GOLD));
                    } else if (!Platform.isModLoaded("emi")) {
                        tooltip.accept(Component.literal("Adorable Hamster Pets").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
                    }
                    super.appendHoverText(stack, context, tooltipDisplay, tooltip, type);
                }
            });

    public static final RegistrySupplier<Item> MUSIC_DISC_ACORN = registerItem("music_disc_acorn",
            () -> new Item(props().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(ACORN_SONG_ZAMPONA_KEY)) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag type) {
                    if (Configs.AHP_UI.enableItemTooltips) {
                        tooltip.accept(Component.translatable("tooltip.adorablehamsterpets.music_disc_acorn.hint").withStyle(ChatFormatting.GOLD));
                    } else if (!Platform.isModLoaded("emi")) {
                        tooltip.accept(Component.literal("Adorable Hamster Pets").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
                    }
                    super.appendHoverText(stack, context, tooltipDisplay, tooltip, type);
                }
            });

    // --- Acorn & Resources ---
    public static final RegistrySupplier<Item> ACORN = registerItem("acorn",
            () -> new BlockItem(Blocks.OAK_SAPLING, props().useItemDescriptionPrefix()) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag type) {
                    if (Configs.AHP_UI.enableItemTooltips) {
                        tooltip.accept(Component.translatable("tooltip.adorablehamsterpets.acorn.hint1").withStyle(ChatFormatting.GOLD));
                        tooltip.accept(Component.translatable("tooltip.adorablehamsterpets.acorn.hint2").withStyle(ChatFormatting.GRAY));
                    } else if (!Platform.isModLoaded("emi")) {
                        tooltip.accept(Component.literal("Adorable Hamster Pets").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
                    }
                    super.appendHoverText(stack, context, tooltipDisplay, tooltip, type);
                }
            });

    public static final RegistrySupplier<Item> ACORN_SHARD = registerItem("acorn_shard",
            () -> new Item(props()) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag type) {
                    if (Configs.AHP_UI.enableItemTooltips) {
                        tooltip.accept(Component.translatable("tooltip.adorablehamsterpets.acorn_shard.hint1").withStyle(ChatFormatting.GOLD));
                        tooltip.accept(Component.translatable("tooltip.adorablehamsterpets.acorn_shard.hint2").withStyle(ChatFormatting.GRAY));
                    } else if (!Platform.isModLoaded("emi")) {
                        tooltip.accept(Component.literal("Adorable Hamster Pets").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
                    }
                    super.appendHoverText(stack, context, tooltipDisplay, tooltip, type);
                }
            });

    public static final RegistrySupplier<Item> ACORN_HAT = registerItem("acorn_hat",
            () -> new Item(props()) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag type) {
                    if (Configs.AHP_UI.enableItemTooltips) {
                        tooltip.accept(Component.translatable("tooltip.adorablehamsterpets.acorn_hat.hint1").withStyle(ChatFormatting.GOLD));
                        tooltip.accept(Component.translatable("tooltip.adorablehamsterpets.acorn_hat.hint2").withStyle(ChatFormatting.GRAY));
                    } else if (!Platform.isModLoaded("emi")) {
                        tooltip.accept(Component.literal("Adorable Hamster Pets").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
                    }
                    super.appendHoverText(stack, context, tooltipDisplay, tooltip, type);
                }
            });

    // --- Acorn Items ---
    public static final RegistrySupplier<Item> ACORN_RING = registerItem("acorn_ring",
            () -> new Item(props().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag type) {
                    if (Configs.AHP_UI.enableItemTooltips) {
                        tooltip.accept(Component.translatable("tooltip.adorablehamsterpets.acorn_ring.hint1")
                                .withStyle(ChatFormatting.GOLD));
                        tooltip.accept(Component.translatable("tooltip.adorablehamsterpets.acorn_ring.hint2")
                                .withStyle(ChatFormatting.GRAY));
                    } else if (!Platform.isModLoaded("emi")) {
                        tooltip.accept(Component.literal("Adorable Hamster Pets").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
                    }
                    super.appendHoverText(stack, context, tooltipDisplay, tooltip, type);
                }
            });

    public static final RegistrySupplier<Item> ACORN_FLUTE_LUSH = registerItem("acorn_flute_lush",
            () -> new AcornFluteItem(props().stacksTo(16), AcornFluteVariant.LUSH));

    public static final RegistrySupplier<Item> ACORN_FLUTE_EMBER = registerItem("acorn_flute_ember",
            () -> new AcornFluteItem(props().stacksTo(16), AcornFluteVariant.EMBER));

    public static final RegistrySupplier<Item> ACORN_FLUTE_HARMONY = registerItem("acorn_flute_harmony",
            () -> new AcornFluteItem(props().stacksTo(16), AcornFluteVariant.HARMONY));

    // --- Hamster Armor ---
    public static final RegistrySupplier<Item> HAMSTER_ARMOR_ACORN = registerItem("hamster_armor_acorn",
            () -> new HamsterArmorItem(HamsterArmorItem.HamsterArmorMaterial.ACORN, props()) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag type) {
                    if (Configs.AHP_UI.enableItemTooltips) {
                        tooltip.accept(Component.translatable("tooltip.adorablehamsterpets.hamster_armor_acorn.hint1").withStyle(ChatFormatting.GOLD));
                        tooltip.accept(Component.translatable("tooltip.adorablehamsterpets.hamster_armor_acorn.hint2").withStyle(ChatFormatting.GRAY));
                    } else if (!Platform.isModLoaded("emi")) {
                        tooltip.accept(Component.literal("Adorable Hamster Pets").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
                    }
                    super.appendHoverText(stack, context, tooltipDisplay, tooltip, type);
                }
            });

    public static final RegistrySupplier<Item> HAMSTER_ARMOR_IRON = registerItem("hamster_armor_iron",
            () -> new HamsterArmorItem(HamsterArmorItem.HamsterArmorMaterial.IRON, props()) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag type) {
                    if (Configs.AHP_UI.enableItemTooltips) {
                        tooltip.accept(Component.translatable("tooltip.adorablehamsterpets.hamster_armor_iron.hint1").withStyle(ChatFormatting.GOLD));
                        tooltip.accept(Component.translatable("tooltip.adorablehamsterpets.hamster_armor_iron.hint2").withStyle(ChatFormatting.GRAY));
                    } else if (!Platform.isModLoaded("emi")) {
                        tooltip.accept(Component.literal("Adorable Hamster Pets").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
                    }
                    super.appendHoverText(stack, context, tooltipDisplay, tooltip, type);
                }
            });

    public static final RegistrySupplier<Item> HAMSTER_ARMOR_GOLD = registerItem("hamster_armor_gold",
            () -> new HamsterArmorItem(HamsterArmorItem.HamsterArmorMaterial.GOLD, props()) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag type) {
                    if (Configs.AHP_UI.enableItemTooltips) {
                        tooltip.accept(Component.translatable("tooltip.adorablehamsterpets.hamster_armor_gold.hint1").withStyle(ChatFormatting.GOLD));
                        tooltip.accept(Component.translatable("tooltip.adorablehamsterpets.hamster_armor_gold.hint2").withStyle(ChatFormatting.GRAY));
                    } else if (!Platform.isModLoaded("emi")) {
                        tooltip.accept(Component.literal("Adorable Hamster Pets").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
                    }
                    super.appendHoverText(stack, context, tooltipDisplay, tooltip, type);
                }
            });

    public static final RegistrySupplier<Item> HAMSTER_ARMOR_DIAMOND = registerItem("hamster_armor_diamond",
            () -> new HamsterArmorItem(HamsterArmorItem.HamsterArmorMaterial.DIAMOND, props()) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag type) {
                    if (Configs.AHP_UI.enableItemTooltips) {
                        tooltip.accept(Component.translatable("tooltip.adorablehamsterpets.hamster_armor_diamond.hint1").withStyle(ChatFormatting.GOLD));
                        tooltip.accept(Component.translatable("tooltip.adorablehamsterpets.hamster_armor_diamond.hint2").withStyle(ChatFormatting.GRAY));
                    } else if (!Platform.isModLoaded("emi")) {
                        tooltip.accept(Component.literal("Adorable Hamster Pets").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
                    }
                    super.appendHoverText(stack, context, tooltipDisplay, tooltip, type);
                }
            });

    public static final RegistrySupplier<Item> HAMSTER_ARMOR_NETHERITE = registerItem("hamster_armor_netherite",
            () -> new HamsterArmorItem(HamsterArmorItem.HamsterArmorMaterial.NETHERITE, props()) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag type) {
                    if (Configs.AHP_UI.enableItemTooltips) {
                        tooltip.accept(Component.translatable("tooltip.adorablehamsterpets.hamster_armor_netherite.hint1").withStyle(ChatFormatting.GOLD));
                        tooltip.accept(Component.translatable("tooltip.adorablehamsterpets.hamster_armor_netherite.hint2").withStyle(ChatFormatting.GRAY));
                    } else if (!Platform.isModLoaded("emi")) {
                        tooltip.accept(Component.literal("Adorable Hamster Pets").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
                    }
                    super.appendHoverText(stack, context, tooltipDisplay, tooltip, type);
                }
            });


    // --- Smithing Templates ---
    public static final RegistrySupplier<Item> HAMSTER_ARMOR_TRIM_SMITHING_TEMPLATE_IRON = registerItem("hamster_armor_trim_smithing_template_iron",
            () -> createHamsterArmorTemplate("iron"));

    public static final RegistrySupplier<Item> HAMSTER_ARMOR_TRIM_SMITHING_TEMPLATE_GOLD = registerItem("hamster_armor_trim_smithing_template_gold",
            () -> createHamsterArmorTemplate("gold"));

    public static final RegistrySupplier<Item> HAMSTER_ARMOR_TRIM_SMITHING_TEMPLATE_DIAMOND = registerItem("hamster_armor_trim_smithing_template_diamond",
            () -> createHamsterArmorTemplate("diamond"));

    public static final RegistrySupplier<Item> HAMSTER_ARMOR_TRIM_SMITHING_TEMPLATE_NETHERITE = registerItem("hamster_armor_trim_smithing_template_netherite",
            () -> createHamsterArmorTemplate("netherite"));


    // --- Block Item Registrations ---
    public static final RegistrySupplier<Item> WILD_GREEN_BEAN_BUSH_ITEM = registerBlockItem("wild_green_bean_bush",
            () -> new BlockItem(ModBlocks.WILD_GREEN_BEAN_BUSH.get(), props().useBlockDescriptionPrefix()) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag type) {
                    if (Configs.AHP_UI.enableItemTooltips) {
                        tooltip.accept(Component.translatable("block.adorablehamsterpets.wild_green_bean_bush.hint1").withStyle(ChatFormatting.GOLD));
                        tooltip.accept(Component.translatable("block.adorablehamsterpets.wild_green_bean_bush.hint2").withStyle(ChatFormatting.GRAY));
                    } else if (!Platform.isModLoaded("emi")) {
                        tooltip.accept(Component.literal("Adorable Hamster Pets").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
                    }
                    super.appendHoverText(stack, context, tooltipDisplay, tooltip, type);
                }
            });

    public static final RegistrySupplier<Item> WILD_CUCUMBER_BUSH_ITEM = registerBlockItem("wild_cucumber_bush",
            () -> new BlockItem(ModBlocks.WILD_CUCUMBER_BUSH.get(), props().useBlockDescriptionPrefix()) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag type) {
                    if (Configs.AHP_UI.enableItemTooltips) {
                        tooltip.accept(Component.translatable("block.adorablehamsterpets.wild_cucumber_bush.hint1").withStyle(ChatFormatting.GOLD));
                        tooltip.accept(Component.translatable("block.adorablehamsterpets.wild_cucumber_bush.hint2").withStyle(ChatFormatting.GRAY));
                    } else if (!Platform.isModLoaded("emi")) {
                        tooltip.accept(Component.literal("Adorable Hamster Pets").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
                    }
                    super.appendHoverText(stack, context, tooltipDisplay, tooltip, type);
                }
            });

    public static final RegistrySupplier<Item> SUNFLOWER_BLOCK_ITEM = registerBlockItem("sunflower_block",
            () -> new BlockItem(ModBlocks.SUNFLOWER_BLOCK.get(), props().useBlockDescriptionPrefix()) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag type) {
                    if (Configs.AHP_UI.enableItemTooltips) {
                        tooltip.accept(Component.translatable("block.adorablehamsterpets.sunflower_block.hint1").withStyle(ChatFormatting.GOLD));
                        tooltip.accept(Component.translatable("block.adorablehamsterpets.sunflower_block.hint2").withStyle(ChatFormatting.GRAY));
                    } else if (!Platform.isModLoaded("emi")) {
                        tooltip.accept(Component.literal("Adorable Hamster Pets").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
                    }
                    super.appendHoverText(stack, context, tooltipDisplay, tooltip, type);
                }
            });

    public static final RegistrySupplier<Item> HAMSTER_BEDDING = registerItem("hamster_bedding",
            () -> new HamsterBeddingItem(props()));

    // Hamster Bed
    public static final Map<WoodVariant, RegistrySupplier<Item>> HAMSTER_BED_ITEMS = new EnumMap<>(WoodVariant.class);
    static {
        for (WoodVariant variant : WoodVariant.values()) {
            HAMSTER_BED_ITEMS.put(variant, registerBlockItem("hamster_bed_" + variant.getSerializedName(),
                    () -> new HamsterBedItem(ModBlocks.HAMSTER_BED.get(), variant, props().stacksTo(1).useItemDescriptionPrefix())));
        }
    }

    public static final RegistrySupplier<Item> ACORN_CRATE = registerBlockItem("acorn_crate",
            () -> new BlockItem(ModBlocks.ACORN_CRATE.get(), props().useBlockDescriptionPrefix()));

    public static final RegistrySupplier<Item> CUCUMBER_CRATE = registerBlockItem("cucumber_crate",
            () -> new BlockItem(ModBlocks.CUCUMBER_CRATE.get(), props().useBlockDescriptionPrefix()));

    public static final RegistrySupplier<Item> GREEN_BEANS_CRATE = registerBlockItem("green_beans_crate",
            () -> new BlockItem(ModBlocks.GREEN_BEANS_CRATE.get(), props().useBlockDescriptionPrefix()));

    public static final RegistrySupplier<Item> HAMSTER_FOOD_MIX_CRATE = registerBlockItem("hamster_food_mix_crate",
            () -> new BlockItem(ModBlocks.HAMSTER_FOOD_MIX_CRATE.get(), props().useBlockDescriptionPrefix()));

    public static final RegistrySupplier<Item> UPSIDE_DOWN_HAMSTER_BED_ICON = registerItem("upside_down_hamster_bed_icon",
            () -> new HamsterBedItem(ModBlocks.HAMSTER_BED.get(), WoodVariant.OAK, props().useItemDescriptionPrefix()));

    // So Patchouli can display custom bell icon in its category list
    public static final RegistrySupplier<Item> ANNOUNCEMENT_BELL_ICON = registerItem("announcement_bell_icon",
            () -> new Item(props()));

    // --- 3. Helper methods for registration ---
    private static RegistrySupplier<Item> registerItem(String name, Supplier<Item> itemSupplier) {
        return ITEMS.register(name, withKey(name, itemSupplier));
    }

    private static RegistrySupplier<Item> registerBlockItem(String name, Supplier<Item> itemSupplier) {
        return ITEMS.register(name, withKey(name, itemSupplier));
    }

    // Since 1.21.2 every item needs its registry key in its properties before construction.
    private static final ThreadLocal<net.minecraft.resources.ResourceKey<Item>> CURRENT_KEY = new ThreadLocal<>();

    private static Supplier<Item> withKey(String name, Supplier<Item> itemSupplier) {
        net.minecraft.resources.ResourceKey<Item> key = net.minecraft.resources.ResourceKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, name));
        return () -> {
            CURRENT_KEY.set(key);
            try {
                return itemSupplier.get();
            } finally {
                CURRENT_KEY.remove();
            }
        };
    }

    /** Item properties with the registry id of the item currently being constructed. */
    public static Item.Properties props() {
        net.minecraft.resources.ResourceKey<Item> key = CURRENT_KEY.get();
        if (key == null) {
            throw new IllegalStateException("ModItems.props() called outside of item registration");
        }
        return new Item.Properties().setId(key);
    }

    /**
     * Helper to create standard Hamster Armor Smithing Templates.
     * Uses vanilla assets for the empty slot icons to avoid needing new textures,
     * and anonymous subclass to inject the tooltip hints.
     *
     * @param materialName The name of the material (e.g., "iron").
     * @return A configured SmithingTemplateItem.
     */
    private static Item createHamsterArmorTemplate(String materialName) {
        return new net.minecraft.world.item.SmithingTemplateItem(
                Component.translatable("item.adorablehamsterpets.hamster_armor_trim_smithing_template.applies_to").withStyle(ChatFormatting.BLUE),                       // Applies to
                Component.translatable("item.adorablehamsterpets.hamster_armor_trim_smithing_template." + materialName + ".ingredients").withStyle(ChatFormatting.BLUE), // Ingredients
                Component.translatable("item.adorablehamsterpets.hamster_armor_trim_smithing_template.base_slot_description"),                                       // Base Slot Desc
                Component.translatable("item.adorablehamsterpets.hamster_armor_trim_smithing_template.additions_slot_description"),                                  // Additions Slot Desc
                List.of(Identifier.fromNamespaceAndPath("minecraft", "item/empty_armor_slot_helmet")),                                                                 // Empty Base Slot Icon
                List.of(Identifier.fromNamespaceAndPath("minecraft", "item/empty_slot_ingot")),                                                                        // Empty Additions Slot Icon
                props()
        ) {
            @Override
            public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag type) {
                // Vanilla SmithingTemplateItem adds its own tooltip info first.
                super.appendHoverText(stack, context, tooltipDisplay, tooltip, type);

                if (Configs.AHP_UI.enableItemTooltips) {
                    tooltip.accept(Component.empty()); // Spacer
                    // Use dynamic keys based on the material name (iron, gold, diamond, netherite)
                    tooltip.accept(Component.translatable("tooltip.adorablehamsterpets.smithing_template." + materialName + ".hint1").withStyle(ChatFormatting.GOLD));
                    tooltip.accept(Component.translatable("tooltip.adorablehamsterpets.smithing_template." + materialName + ".hint2").withStyle(ChatFormatting.GRAY));
                } else if (!Platform.isModLoaded("emi")) {
                    tooltip.accept(Component.literal("Adorable Hamster Pets").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
                }
            }
        };
    }

    // --- 4. Main registration call ---
    public static void register() {
        ITEMS.register();
    }
}
