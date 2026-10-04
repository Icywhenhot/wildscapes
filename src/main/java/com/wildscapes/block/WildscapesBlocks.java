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
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class WildscapesBlocks {
    private WildscapesBlocks() {}

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Wildscapes.MODID);

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

    public static final DeferredBlock<FenceBlock> CYPRESS_FENCE = register("cypress_fence",
            () -> new FenceBlock(plankProps()));

    public static final DeferredBlock<FenceGateBlock> CYPRESS_FENCE_GATE = register("cypress_fence_gate",
            () -> new FenceGateBlock(WoodType.OAK, plankProps()));

    public static final DeferredBlock<StairBlock> CYPRESS_STAIRS = register("cypress_stairs",
            () -> new StairBlock(CYPRESS_PLANKS.get().defaultBlockState(), plankProps()));

    public static final DeferredBlock<SlabBlock> CYPRESS_SLAB = register("cypress_slab",
            () -> new SlabBlock(plankProps()));

    public static final DeferredBlock<DuckweedBlock> DUCKWEED = register("duckweed",
            () -> new DuckweedBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY)),

            block -> new PlaceOnWaterBlockItem(block, new Item.Properties()));

    public static final DeferredBlock<RushesBlock> RUSHES = register("rushes",
            () -> new RushesBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<ShortRushesBlock> SHORT_RUSHES = register("short_rushes",
            () -> new ShortRushesBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<DrapingMossCarpetBlock> MOSS_CARPET = BLOCKS.register("moss_carpet",
            () -> new DrapingMossCarpetBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(0.1F)
                    .randomTicks()
                    .sound(SoundType.MOSS_CARPET)
                    .pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<HangingMossBlock> HANGING_MOSS = BLOCKS.register("hanging_moss",
            () -> new HangingMossBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .noCollission()
                    .instabreak()
                    .randomTicks()
                    .sound(SoundType.MOSS_CARPET)
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<BonfireBlock> BONFIRE = register("bonfire",
            () -> new BonfireBlock(1.0F, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
                    .lightLevel(state -> 15)
                    .noOcclusion()
                    .ignitedByLava()));

    public static final DeferredBlock<BonfireBlock> SOUL_BONFIRE = register("soul_bonfire",
            () -> new BonfireBlock(2.0F, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
                    .lightLevel(state -> 10)
                    .noOcclusion()
                    .ignitedByLava()));

    public static final DeferredBlock<CauldronOfSoulsBlock> CAULDRON_OF_SOULS = register("cauldron_of_souls",
            () -> new CauldronOfSoulsBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(50.0F)
                    .sound(SoundType.TRIAL_SPAWNER)
                    .lightLevel(state -> state.getValue(CauldronOfSoulsBlock.PHASE) == CauldronOfSoulsBlock.Phase.INACTIVE ? 3 : 10)
                    .noOcclusion()
                    .noLootTable()));

    public static final DeferredBlock<SummoningBonfireBlock> SUMMONING_BONFIRE = register("summoning_bonfire",
            () -> new SummoningBonfireBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(50.0F)
                    .sound(SoundType.TRIAL_SPAWNER)
                    .lightLevel(state -> state.getValue(SummoningBonfireBlock.LIT) ? 12 : 0)
                    .noOcclusion()
                    .noLootTable()));

    public static final DeferredBlock<IncursionFireBlock> INCURSION_FIRE = BLOCKS.register("incursion_fire",
            () -> new IncursionFireBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .replaceable()
                    .noCollission()
                    .instabreak()
                    .lightLevel(state -> 12)
                    .sound(SoundType.WOOL)
                    .pushReaction(PushReaction.DESTROY)
                    .noLootTable()));

    public static final DeferredBlock<WildscapesCauldronBlock> CAULDRON = BLOCKS.register("cauldron",
            () -> new WildscapesCauldronBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(state -> state.getValue(WildscapesCauldronBlock.BOILING) ? 3 : 0)));

    private static BlockBehaviour.Properties plankProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BROWN)
                .strength(2.0F, 3.0F)
                .sound(SoundType.WOOD)
                .ignitedByLava();
    }

    private static BlockBehaviour.Properties logProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BROWN)
                .strength(2.0F)
                .sound(SoundType.WOOD)
                .ignitedByLava();
    }

    static <T extends Block> DeferredBlock<T> register(String name, Supplier<T> supplier) {
        DeferredBlock<T> block = BLOCKS.register(name, supplier);
        WildscapesItems.ITEMS.registerSimpleBlockItem(name, block);
        return block;
    }

    private static <T extends Block> DeferredBlock<T> register(String name, Supplier<T> supplier,
            Function<T, ? extends BlockItem> itemFactory) {
        DeferredBlock<T> block = BLOCKS.register(name, supplier);
        WildscapesItems.ITEMS.register(name, () -> itemFactory.apply(block.get()));
        return block;
    }

    public static void register(IEventBus bus) {
        MudBrickBlocks.init();
        BLOCKS.register(bus);
    }
}
