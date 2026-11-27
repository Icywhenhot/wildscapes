package com.wildscapes.block.entity;

import com.wildscapes.Wildscapes;

import net.minecraft.core.Registry;
import com.wildscapes.block.WildscapesBlocks;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class WildscapesBlockEntities {
    private WildscapesBlockEntities() {}

    public static final BlockEntityType<CauldronBlockEntity> CAULDRON =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Wildscapes.id("cauldron"), new BlockEntityType<>(CauldronBlockEntity::new, WildscapesBlocks.CAULDRON));

    public static final BlockEntityType<CauldronOfSoulsBlockEntity> CAULDRON_OF_SOULS =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Wildscapes.id("cauldron_of_souls"), new BlockEntityType<>(CauldronOfSoulsBlockEntity::new, WildscapesBlocks.CAULDRON_OF_SOULS));

    public static void register() {}
}
