package net.dawson.adorablehamsterpets.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.stream.Stream;

/**
 * Bridges the 26.x {@link ValueInput}/{@link ValueOutput} save system with the mod's existing
 * CompoundTag-based save logic. Keys are written flat into the entity's data, so the on-disk format
 * stays identical to older versions of the mod (existing worlds keep their hamsters).
 */
public final class NbtBridge {
    private NbtBridge() {}

    /** A MapCodec that reads/writes all keys of a CompoundTag directly into the surrounding map. */
    public static final MapCodec<CompoundTag> FLAT_COMPOUND = new MapCodec<>() {
        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.empty();
        }

        @Override
        public <T> DataResult<CompoundTag> decode(DynamicOps<T> ops, MapLike<T> input) {
            CompoundTag tag = new CompoundTag();
            input.entries().forEach((Pair<T, T> entry) -> ops.getStringValue(entry.getFirst()).result()
                    .ifPresent(key -> tag.put(key, ops.convertTo(NbtOps.INSTANCE, entry.getSecond()))));
            return DataResult.success(tag);
        }

        @Override
        public <T> RecordBuilder<T> encode(CompoundTag input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            for (String key : input.keySet()) {
                Tag value = input.get(key);
                if (value != null) {
                    prefix.add(key, NbtOps.INSTANCE.convertTo(ops, value));
                }
            }
            return prefix;
        }
    };

    /** Reads the full data of this input as a CompoundTag. */
    public static CompoundTag read(ValueInput input) {
        return input.read(FLAT_COMPOUND).orElseGet(CompoundTag::new);
    }

    /** Writes all keys of the given CompoundTag into the output. */
    public static void write(ValueOutput output, CompoundTag tag) {
        output.store(FLAT_COMPOUND, tag);
    }
}
