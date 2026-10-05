package net.dawson.adorablehamsterpets.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Helpers that replace CompoundTag/ItemStack/ContainerHelper methods removed in Minecraft 1.21.5+.
 * The produced NBT layout matches the old vanilla layout, so existing save data stays readable.
 */
public final class NbtCompat {
    private NbtCompat() {}

    public static void putUUID(CompoundTag tag, String key, UUID uuid) {
        tag.store(key, UUIDUtil.CODEC, uuid);
    }

    public static boolean hasUUID(CompoundTag tag, String key) {
        return tag.read(key, UUIDUtil.CODEC).isPresent();
    }

    @Nullable
    public static UUID getUUID(CompoundTag tag, String key) {
        return tag.read(key, UUIDUtil.CODEC).orElse(null);
    }

    public static Tag encodeStack(ItemStack stack, HolderLookup.Provider registries) {
        return ItemStack.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), stack).getOrThrow();
    }

    public static Optional<ItemStack> decodeStack(HolderLookup.Provider registries, @Nullable Tag tag) {
        if (tag == null) return Optional.empty();
        return ItemStack.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag).result();
    }

    public static Optional<ItemStack> decodeStack(HolderLookup.Provider registries, Optional<? extends Tag> tag) {
        return tag.flatMap(t -> decodeStack(registries, (Tag) t));
    }

    /** Equivalent of the old ContainerHelper.saveAllItems (writes an "Items" list with "Slot" bytes). */
    public static CompoundTag saveItems(CompoundTag tag, NonNullList<ItemStack> items, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()) {
                Tag encoded = encodeStack(stack, registries);
                if (encoded instanceof CompoundTag compound) {
                    CompoundTag entry = compound.copy();
                    entry.putByte("Slot", (byte) i);
                    list.add(entry);
                }
            }
        }
        if (!list.isEmpty()) {
            tag.put("Items", list);
        }
        return tag;
    }

    /** Equivalent of the old ContainerHelper.loadAllItems. */
    public static void loadItems(CompoundTag tag, NonNullList<ItemStack> items, HolderLookup.Provider registries) {
        ListTag list = tag.getListOrEmpty("Items");
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompoundOrEmpty(i);
            int slot = entry.getByteOr("Slot", (byte) 0) & 255;
            if (slot < items.size()) {
                items.set(slot, decodeStack(registries, entry).orElse(ItemStack.EMPTY));
            }
        }
    }

    public static void loadItems(Optional<CompoundTag> tag, NonNullList<ItemStack> items, HolderLookup.Provider registries) {
        tag.ifPresent(t -> loadItems(t, items, registries));
    }

    public static Tag saveEffect(MobEffectInstance effect) {
        return MobEffectInstance.CODEC.encodeStart(NbtOps.INSTANCE, effect).getOrThrow();
    }

    @Nullable
    public static MobEffectInstance loadEffect(Tag tag) {
        return MobEffectInstance.CODEC.parse(NbtOps.INSTANCE, tag).result().orElse(null);
    }

    /** Replacement for the old Entity#writeNbt(CompoundTag): writes the full entity state (without id) into the tag. */
    public static CompoundTag saveEntity(net.minecraft.world.entity.Entity entity, CompoundTag into) {
        net.minecraft.world.level.storage.TagValueOutput output = net.minecraft.world.level.storage.TagValueOutput.createWithContext(
                net.minecraft.util.ProblemReporter.DISCARDING, entity.registryAccess());
        entity.saveWithoutId(output);
        CompoundTag result = output.buildResult();
        for (String key : result.keySet()) {
            Tag value = result.get(key);
            if (value != null) into.put(key, value);
        }
        return into;
    }

    /** Replacement for the old Entity#readNbt(CompoundTag). */
    public static void loadEntity(net.minecraft.world.entity.Entity entity, CompoundTag tag) {
        entity.load(net.minecraft.world.level.storage.TagValueInput.create(
                net.minecraft.util.ProblemReporter.DISCARDING, entity.registryAccess(), tag));
    }

    /** Replacement for calling HamsterEntity#addAdditionalSaveData with a CompoundTag. */
    public static void saveAdditional(net.dawson.adorablehamsterpets.entity.custom.HamsterEntity entity, CompoundTag into) {
        net.minecraft.world.level.storage.TagValueOutput output = net.minecraft.world.level.storage.TagValueOutput.createWithContext(
                net.minecraft.util.ProblemReporter.DISCARDING, entity.registryAccess());
        entity.addAdditionalSaveData(output);
        CompoundTag result = output.buildResult();
        for (String key : result.keySet()) {
            Tag value = result.get(key);
            if (value != null) into.put(key, value);
        }
    }

    /** Replacement for calling HamsterEntity#readAdditionalSaveData with a CompoundTag. */
    public static void loadAdditional(net.dawson.adorablehamsterpets.entity.custom.HamsterEntity entity, CompoundTag tag) {
        entity.readAdditionalSaveData(net.minecraft.world.level.storage.TagValueInput.create(
                net.minecraft.util.ProblemReporter.DISCARDING, entity.registryAccess(), tag));
    }
}
