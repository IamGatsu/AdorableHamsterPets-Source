package net.dawson.adorablehamsterpets.fabric.datagen;

import net.dawson.adorablehamsterpets.AdorableHamsterPets;
import net.dawson.adorablehamsterpets.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.Identifier;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends FabricTagsProvider.ItemTagsProvider {

    private static final TagKey<Item> STORAGE_BLOCKS = TagKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath("c", "storage_blocks")
    );

    // --- Custom Tags ---
    public static final TagKey<Item> HAMSTER_ARMOR_ENCHANTABLE = TagKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, "enchantable/hamster_armor")
    );

    // Frost Walker (Vanilla Foot Armor + Hamster Armor)
    public static final TagKey<Item> FROST_WALKER_SUPPORTED = TagKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, "enchantable/frost_walker_supported")
    );

    // Fire Protection (Vanilla Armor + Hamster Armor)
    public static final TagKey<Item> FIRE_PROTECTION_SUPPORTED = TagKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, "enchantable/fire_protection_supported")
    );

    // Soul Speed (Vanilla Foot Armor + Hamster Armor)
    public static final TagKey<Item> SOUL_SPEED_SUPPORTED = TagKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, "enchantable/soul_speed_supported")
    );

    public ModItemTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        // 1. Define Hamster Armor Group
        tag(HAMSTER_ARMOR_ENCHANTABLE)
                .add(key(ModItems.HAMSTER_ARMOR_ACORN.get()))
                .add(key(ModItems.HAMSTER_ARMOR_IRON.get()))
                .add(key(ModItems.HAMSTER_ARMOR_GOLD.get()))
                .add(key(ModItems.HAMSTER_ARMOR_DIAMOND.get()))
                .add(key(ModItems.HAMSTER_ARMOR_NETHERITE.get()));

        // 2. Add to Vanilla Durability (Enables Unbreaking/Mending)
        tag(ItemTags.DURABILITY_ENCHANTABLE)
                .addTag(HAMSTER_ARMOR_ENCHANTABLE);

        // 3. Frost Walker Wrapper
        tag(FROST_WALKER_SUPPORTED)
                .addOptionalTag(ItemTags.FOOT_ARMOR_ENCHANTABLE)
                .addTag(HAMSTER_ARMOR_ENCHANTABLE);

        // 4. Fire Protection Wrapper
        tag(FIRE_PROTECTION_SUPPORTED)
                .addOptionalTag(ItemTags.ARMOR_ENCHANTABLE)
                .addTag(HAMSTER_ARMOR_ENCHANTABLE);

        // 5. Soul Speed Wrapper
        tag(SOUL_SPEED_SUPPORTED)
                .addOptionalTag(ItemTags.FOOT_ARMOR_ENCHANTABLE)
                .addTag(HAMSTER_ARMOR_ENCHANTABLE);

        // 6. Lectern Books Wrapper
        tag(ItemTags.LECTERN_BOOKS)
                .add(key(ModItems.HAMSTER_GUIDE_BOOK.get()));

        // 7. Chiseled Bookshelf Books
        tag(ItemTags.BOOKSHELF_BOOKS)
                .add(key(ModItems.HAMSTER_GUIDE_BOOK.get()));

        // 8. Vanilla Trimmable Armor Wrapper
        tag(ItemTags.TRIMMABLE_ARMOR)
                .addTag(HAMSTER_ARMOR_ENCHANTABLE);

        // 8. Music Discs (only neccessary on 1.20.1)
//        tag(ItemTags.MUSIC_DISCS)
//                .add(key(ModItems.MUSIC_DISC_CHEESE.get()))
//                .add(key(ModItems.MUSIC_DISC_BLUE_CHEESE.get()))
//                .add(key(ModItems.MUSIC_DISC_PARMESAN.get()))
//                .add(key(ModItems.MUSIC_DISC_ACORN.get()));

        // Farmer's Delight and MineColonies use this item tag for storage-crate interoperability
        tag(STORAGE_BLOCKS)
                .add(key(ModItems.ACORN_CRATE.get()))
                .add(key(ModItems.CUCUMBER_CRATE.get()))
                .add(key(ModItems.GREEN_BEANS_CRATE.get()))
                .add(key(ModItems.HAMSTER_FOOD_MIX_CRATE.get()));
    }


    private static net.minecraft.resources.ResourceKey<Item> key(Item value) {
        return BuiltInRegistries.ITEM.getResourceKey(value).orElseThrow();
    }
}
