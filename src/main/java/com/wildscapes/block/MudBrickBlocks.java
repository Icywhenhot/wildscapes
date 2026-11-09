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
import net.minecraftforge.registries.RegistryObject;

public final class MudBrickBlocks {
    private MudBrickBlocks() {}

    public static final Map<DyeColor, RegistryObject<Block>> BRICKS = new EnumMap<>(DyeColor.class);
    public static final Map<DyeColor, RegistryObject<Block>> CHISELED = new EnumMap<>(DyeColor.class);
    public static final Map<DyeColor, RegistryObject<StairBlock>> STAIRS = new EnumMap<>(DyeColor.class);
    public static final Map<DyeColor, RegistryObject<SlabBlock>> SLABS = new EnumMap<>(DyeColor.class);
    public static final Map<DyeColor, RegistryObject<WallBlock>> WALLS = new EnumMap<>(DyeColor.class);

    public static final RegistryObject<Block> CHISELED_MUD_BRICKS = WildscapesBlocks.register(
            "chiseled_mud_bricks", () -> new Block(props(MapColor.TERRACOTTA_LIGHT_GRAY)));

    static {
        for (DyeColor color : DyeColor.values()) {
            String name = color.getName();
            MapColor mapColor = color.getMapColor();

            RegistryObject<Block> bricks = WildscapesBlocks.register(name + "_mud_bricks",
                    () -> new Block(props(mapColor)));

            BRICKS.put(color, bricks);
            CHISELED.put(color, WildscapesBlocks.register("chiseled_" + name + "_mud_bricks",
                    () -> new Block(props(mapColor))));
            STAIRS.put(color, WildscapesBlocks.register(name + "_mud_brick_stairs",
                    () -> new StairBlock(bricks.get().defaultBlockState(), props(mapColor))));
            SLABS.put(color, WildscapesBlocks.register(name + "_mud_brick_slab",
                    () -> new SlabBlock(props(mapColor))));
            WALLS.put(color, WildscapesBlocks.register(name + "_mud_brick_wall",
                    () -> new WallBlock(props(mapColor))));
        }
    }

    private static BlockBehaviour.Properties props(MapColor mapColor) {
        return BlockBehaviour.Properties.of()
                .mapColor(mapColor)
                .requiresCorrectToolForDrops()
                .strength(1.5F, 3.0F)
                .sound(SoundType.MUD_BRICKS);
    }

    static void init() {}
}
