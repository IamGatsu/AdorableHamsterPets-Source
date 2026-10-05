package net.dawson.adorablehamsterpets.block;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.dawson.adorablehamsterpets.AdorableHamsterPets;
import net.dawson.adorablehamsterpets.block.entity.HamsterBedBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.registries.Registries;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(AdorableHamsterPets.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<HamsterBedBlockEntity>> HAMSTER_BED_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("hamster_bed_be", () ->
                    net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.create(HamsterBedBlockEntity::new, ModBlocks.HAMSTER_BED.get()).build());

    public static void register() {
        BLOCK_ENTITIES.register();
    }
}