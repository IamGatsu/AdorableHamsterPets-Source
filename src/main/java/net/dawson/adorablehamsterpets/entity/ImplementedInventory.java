package net.dawson.adorablehamsterpets.entity;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.core.NonNullList;

public interface ImplementedInventory extends Container {

    NonNullList<ItemStack> getItems();

    static NonNullList<ItemStack> create(int size) {
        return NonNullList.withSize(size, ItemStack.EMPTY);
    }

    @Override
    default int getContainerSize() {
        return getItems().size();
    }

    @Override
    default boolean isEmpty() {
        for (int i = 0; i < getContainerSize(); i++) {
            ItemStack stack = getItem(i);
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    default ItemStack getItem(int slot) {
        return getItems().get(slot);
    }

    @Override
    default ItemStack removeItem(int slot, int amount) {
        // Use helper method that respects the 'amount' parameter.
        ItemStack result = ContainerHelper.removeItem(getItems(), slot, amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    default ItemStack removeItemNoUpdate(int slot) {
        setChanged();
        return ContainerHelper.takeItem(getItems(), slot);
    }

    @Override
    default void setItem(int slot, ItemStack stack) {
        getItems().set(slot, stack);
        setChanged();
    }

    @Override
    default void clearContent() {
        getItems().clear();
        setChanged();
    }

    @Override
    default void setChanged() {
        // Client-side inventories do not need to be saved.
    }

    @Override
    default int getMaxStackSize() {
        return 64;
    }

    @Override
    default void startOpen(net.minecraft.world.entity.ContainerUser user) {
    }

    @Override
    default void stopOpen(net.minecraft.world.entity.ContainerUser user) {
    }

    @Override
    default boolean stillValid(Player player) {
        return true;
    }

    @Override
    default boolean canPlaceItem(int slot, ItemStack stack) {
        return true;
    }

    default ContainerData getPropertyDelegate() {
        return new SimpleContainerData(0);
    }
}