package com.wildscapes.block.entity;

import com.wildscapes.Wildscapes;
import com.wildscapes.block.WildscapesBlocks;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import com.wildscapes.registry.RegistryObject;
import com.wildscapes.registry.DeferredRegister;

public final class WildscapesBlockEntities {
    private WildscapesBlockEntities() {}

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Wildscapes.MODID);

    public static final RegistryObject<BlockEntityType<CauldronBlockEntity>> CAULDRON =
            BLOCK_ENTITIES.register("cauldron", () -> BlockEntityType.Builder
                    .of(CauldronBlockEntity::new, WildscapesBlocks.CAULDRON.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<CauldronOfSoulsBlockEntity>> CAULDRON_OF_SOULS =
            BLOCK_ENTITIES.register("cauldron_of_souls", () -> BlockEntityType.Builder
                    .of(CauldronOfSoulsBlockEntity::new, WildscapesBlocks.CAULDRON_OF_SOULS.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<CypressShelfBlockEntity>> CYPRESS_SHELF =
            BLOCK_ENTITIES.register("cypress_shelf", () -> BlockEntityType.Builder
                    .of(CypressShelfBlockEntity::new, WildscapesBlocks.CYPRESS_SHELF.get())
                    .build(null));

    public static void register() {
        BLOCK_ENTITIES.register();
    }
}
