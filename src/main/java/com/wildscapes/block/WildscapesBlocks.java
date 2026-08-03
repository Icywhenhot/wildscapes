package com.wildscapes.block;

import java.util.function.Function;
import java.util.function.Supplier;

import com.wildscapes.Wildscapes;
import com.wildscapes.item.WildscapesItems;
import com.wildscapes.worldgen.WildscapesTreeGrowers;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PlaceOnWaterBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class WildscapesBlocks {
    private WildscapesBlocks() {}

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Wildscapes.MODID);

    // ---- Cypress wood set ----
    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_CYPRESS_LOG = register("stripped_cypress_log",
            () -> new RotatedPillarBlock(logProps()));
    public static final DeferredBlock<RotatedPillarBlock> CYPRESS_LOG = register("cypress_log",
            () -> new StrippableLogBlock(STRIPPED_CYPRESS_LOG, logProps()));

    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_CYPRESS_WOOD = register("stripped_cypress_wood",
            () -> new RotatedPillarBlock(logProps()));
    public static final DeferredBlock<RotatedPillarBlock> CYPRESS_WOOD = register("cypress_wood",
            () -> new StrippableLogBlock(STRIPPED_CYPRESS_WOOD, logProps()));

    public static final DeferredBlock<Block> CYPRESS_PLANKS = register("cypress_planks",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava()));

    public static final DeferredBlock<LeavesBlock> CYPRESS_LEAVES = register("cypress_leaves",
            () -> new LeavesBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .strength(0.2F)
                    .randomTicks()
                    .sound(SoundType.GRASS)
                    .noOcclusion()
                    .isValidSpawn((state, level, pos, type) -> false)
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)
                    .ignitedByLava()
                    .pushReaction(PushReaction.DESTROY)
                    .isRedstoneConductor((state, level, pos) -> false)));

    public static final DeferredBlock<CypressSaplingBlock> CYPRESS_SAPLING = register("cypress_sapling",
            () -> new CypressSaplingBlock(WildscapesTreeGrowers.CYPRESS, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<DoorBlock> CYPRESS_DOOR = register("cypress_door",
            () -> new DoorBlock(BlockSetType.OAK, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(3.0F)
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY)
                    .ignitedByLava()));

    public static final DeferredBlock<TrapDoorBlock> CYPRESS_TRAPDOOR = register("cypress_trapdoor",
            () -> new TrapDoorBlock(BlockSetType.OAK, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(3.0F)
                    .noOcclusion()
                    .isValidSpawn((state, level, pos, type) -> false)
                    .ignitedByLava()));

    // ---- Wetland plants ----
    public static final DeferredBlock<DuckweedBlock> DUCKWEED = register("duckweed",
            () -> new DuckweedBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY)),
            // Same item as the lily pad: a plain BlockItem needs a solid face to click,
            // so it ray-traces against the water surface instead and places on top of it.
            block -> new PlaceOnWaterBlockItem(block, new Item.Properties()));

    public static final DeferredBlock<RushesBlock> RUSHES = register("rushes",
            () -> new RushesBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY)));

    // ---- Swamp decoration ----
    public static final DeferredBlock<BonfireBlock> BONFIRE = register("bonfire",
            () -> new BonfireBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
                    .lightLevel(state -> 15)
                    .noOcclusion()
                    .ignitedByLava()));

    public static final DeferredBlock<Block> WITCH_CAULDRON = register("witch_cauldron",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0F)
                    .sound(SoundType.METAL)
                    .lightLevel(state -> 3)
                    .noOcclusion()));

    // Internal block: players use the vanilla cauldron, which CauldronSwap swaps for this while it
    // is heated or holds dyed water / a potion / a brewing soup. It has no BlockItem and drops a
    // vanilla cauldron (see its loot table).
    public static final DeferredBlock<WildscapesCauldronBlock> CAULDRON = BLOCKS.register("cauldron",
            () -> new WildscapesCauldronBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(state -> state.getValue(WildscapesCauldronBlock.BOILING) ? 3 : 0)));

    private static BlockBehaviour.Properties logProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BROWN)
                .strength(2.0F)
                .sound(SoundType.WOOD)
                .ignitedByLava();
    }

    /** Registers a block and a matching simple {@code BlockItem} under the same name. */
    static <T extends Block> DeferredBlock<T> register(String name, Supplier<T> supplier) {
        DeferredBlock<T> block = BLOCKS.register(name, supplier);
        WildscapesItems.ITEMS.registerSimpleBlockItem(name, block);
        return block;
    }

    /** As {@link #register(String, Supplier)}, for blocks needing a non-default item. */
    private static <T extends Block> DeferredBlock<T> register(String name, Supplier<T> supplier,
            Function<T, ? extends BlockItem> itemFactory) {
        DeferredBlock<T> block = BLOCKS.register(name, supplier);
        WildscapesItems.ITEMS.register(name, () -> itemFactory.apply(block.get()));
        return block;
    }

    public static void register(IEventBus bus) {
        // Loads MudBrickBlocks so its static initialiser adds the dyed set to BLOCKS
        // before the registry event fires.
        MudBrickBlocks.init();
        BLOCKS.register(bus);
    }
}
