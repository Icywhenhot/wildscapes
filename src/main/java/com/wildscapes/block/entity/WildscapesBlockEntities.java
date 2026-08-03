package com.wildscapes.block.entity;

import com.wildscapes.Wildscapes;
import com.wildscapes.block.WildscapesBlocks;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class WildscapesBlockEntities {
    private WildscapesBlockEntities() {}

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Wildscapes.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CauldronBlockEntity>> CAULDRON =
            BLOCK_ENTITIES.register("cauldron", () -> BlockEntityType.Builder
                    .of(CauldronBlockEntity::new, WildscapesBlocks.CAULDRON.get())
                    .build(null));

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}
