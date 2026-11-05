package com.wildscapes.block;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class MudBrickBlocks {
    private MudBrickBlocks() {}

    public static final Map<DyeColor, Block> BRICKS = new EnumMap<>(DyeColor.class);
    public static final Map<DyeColor, Block> CHISELED = new EnumMap<>(DyeColor.class);
    public static final Map<DyeColor, StairBlock> STAIRS = new EnumMap<>(DyeColor.class);
    public static final Map<DyeColor, SlabBlock> SLABS = new EnumMap<>(DyeColor.class);
    public static final Map<DyeColor, WallBlock> WALLS = new EnumMap<>(DyeColor.class);

    public static final Block CHISELED_MUD_BRICKS = WildscapesBlocks.register(
            "chiseled_mud_bricks", properties -> new Block(props(properties, MapColor.TERRACOTTA_LIGHT_GRAY)));

    static {
        for (DyeColor color : DyeColor.values()) {
            String name = color.getName();
            MapColor mapColor = color.getMapColor();

            Block bricks = WildscapesBlocks.register(name + "_mud_bricks",
                    properties -> new Block(props(properties, mapColor)));

            BRICKS.put(color, bricks);
            CHISELED.put(color, WildscapesBlocks.register("chiseled_" + name + "_mud_bricks",
                    properties -> new Block(props(properties, mapColor))));
            STAIRS.put(color, WildscapesBlocks.register(name + "_mud_brick_stairs",
                    properties -> new StairBlock(bricks.defaultBlockState(), props(properties, mapColor))));
            SLABS.put(color, WildscapesBlocks.register(name + "_mud_brick_slab",
                    properties -> new SlabBlock(props(properties, mapColor))));
            WALLS.put(color, WildscapesBlocks.register(name + "_mud_brick_wall",
                    properties -> new WallBlock(props(properties, mapColor))));
        }
    }

    private static BlockBehaviour.Properties props(BlockBehaviour.Properties properties, MapColor mapColor) {
        return properties
                .mapColor(mapColor)
                .requiresCorrectToolForDrops()
                .strength(1.5F, 3.0F)
                .sound(SoundType.MUD_BRICKS);
    }

    static void init() {}
}
