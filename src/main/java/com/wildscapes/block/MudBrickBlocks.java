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
import net.neoforged.neoforge.registries.DeferredBlock;

/**
 * The dyed mud brick set: a full block, a chiseled block, stairs, a slab and a wall for
 * each of the 16 dye colours, plus an undyed {@code chiseled_mud_bricks} to serve as the
 * base of the chiseled family — vanilla has no chiseled mud bricks of its own.
 *
 * <p>Every colour is its own block, the way vanilla handles wool and terracotta, rather
 * than one block with a colour property. A block state cannot give each colour a separate
 * item, and separate items are the whole point of a dyed set.
 *
 * <p>The 600-odd model, blockstate, loot table and recipe files these need are generated
 * by {@code tools/gen_mud_bricks.py}.
 */
public final class MudBrickBlocks {
    private MudBrickBlocks() {}

    public static final Map<DyeColor, DeferredBlock<Block>> BRICKS = new EnumMap<>(DyeColor.class);
    public static final Map<DyeColor, DeferredBlock<Block>> CHISELED = new EnumMap<>(DyeColor.class);
    public static final Map<DyeColor, DeferredBlock<StairBlock>> STAIRS = new EnumMap<>(DyeColor.class);
    public static final Map<DyeColor, DeferredBlock<SlabBlock>> SLABS = new EnumMap<>(DyeColor.class);
    public static final Map<DyeColor, DeferredBlock<WallBlock>> WALLS = new EnumMap<>(DyeColor.class);

    public static final DeferredBlock<Block> CHISELED_MUD_BRICKS = WildscapesBlocks.register(
            "chiseled_mud_bricks", () -> new Block(props(MapColor.TERRACOTTA_LIGHT_GRAY)));

    static {
        for (DyeColor color : DyeColor.values()) {
            String name = color.getName();
            MapColor mapColor = color.getMapColor();

            // Held in a local because the stairs need the brick block's default state,
            // and DeferredRegister hands out entries in the order they were added.
            DeferredBlock<Block> bricks = WildscapesBlocks.register(name + "_mud_bricks",
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

    /** Vanilla's mud brick properties, with the map colour swapped for the dye's. */
    private static BlockBehaviour.Properties props(MapColor mapColor) {
        return BlockBehaviour.Properties.of()
                .mapColor(mapColor)
                .requiresCorrectToolForDrops()
                .strength(1.5F, 3.0F)
                .sound(SoundType.MUD_BRICKS);
    }

    /**
     * Does nothing on its own — it exists so {@link WildscapesBlocks} can force this class
     * to load. The blocks are created by the static initialiser above, which must have run
     * before the registry event fires.
     */
    static void init() {}
}
