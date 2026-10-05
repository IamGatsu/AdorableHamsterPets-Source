package net.dawson.adorablehamsterpets.util;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

/** The item tag "minecraft:flowers" was removed; flowers are detected through their block tag instead. */
public final class FlowerUtil {
    private FlowerUtil() {}

    public static boolean isFlower(ItemStack stack) {
        return stack.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock().defaultBlockState().is(BlockTags.FLOWERS);
    }
}
