package com.wildscapes.block;

import java.util.function.Function;
import java.util.function.BiFunction;

import com.wildscapes.Wildscapes;
import com.wildscapes.worldgen.WildscapesTreeGrowers;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityType;
import net.fabricmc.fabric.api.object.builder.v1.block.type.BlockSetTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.type.WoodTypeBuilder;
import net.fabricmc.fabric.api.registry.CompostableRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PlaceOnWaterBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.TintedParticleLeavesBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public final class WildscapesBlocks {
    private WildscapesBlocks() {}

    public static final TagKey<Block> SOIL = TagKey.create(Registries.BLOCK, Wildscapes.id("soil"));

    public static final BlockSetType CYPRESS_SET = BlockSetTypeBuilder.copyOf(BlockSetType.OAK).register(Wildscapes.id("cypress"));
    public static final WoodType CYPRESS_TYPE = WoodTypeBuilder.copyOf(WoodType.OAK).register(Wildscapes.id("cypress"), CYPRESS_SET);

    public static final RotatedPillarBlock STRIPPED_CYPRESS_LOG = register("stripped_cypress_log",
            properties -> new RotatedPillarBlock(logProps(properties)));
    public static final RotatedPillarBlock CYPRESS_LOG = register("cypress_log",
            properties -> new RotatedPillarBlock(logProps(properties)));

    public static final RotatedPillarBlock STRIPPED_CYPRESS_WOOD = register("stripped_cypress_wood",
            properties -> new RotatedPillarBlock(logProps(properties)));
    public static final RotatedPillarBlock CYPRESS_WOOD = register("cypress_wood",
            properties -> new RotatedPillarBlock(logProps(properties)));

    public static final Block CYPRESS_PLANKS = register("cypress_planks",
            properties -> new Block(properties
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava()));

    public static final LeavesBlock CYPRESS_LEAVES = register("cypress_leaves",
            properties -> new TintedParticleLeavesBlock(0.01F, properties
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

    public static final CypressSaplingBlock CYPRESS_SAPLING = register("cypress_sapling",
            properties -> new CypressSaplingBlock(WildscapesTreeGrowers.CYPRESS, properties
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY)));

    public static final DoorBlock CYPRESS_DOOR = register("cypress_door",
            properties -> new DoorBlock(BlockSetType.OAK, properties
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(3.0F)
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY)
                    .ignitedByLava()));

    public static final TrapDoorBlock CYPRESS_TRAPDOOR = register("cypress_trapdoor",
            properties -> new TrapDoorBlock(BlockSetType.OAK, properties
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(3.0F)
                    .noOcclusion()
                    .isValidSpawn((state, level, pos, type) -> false)
                    .ignitedByLava()));

    public static final FenceBlock CYPRESS_FENCE = register("cypress_fence",
            properties -> new FenceBlock(plankProps(properties)));

    public static final FenceGateBlock CYPRESS_FENCE_GATE = register("cypress_fence_gate",
            properties -> new FenceGateBlock(WoodType.OAK, plankProps(properties)));

    public static final StairBlock CYPRESS_STAIRS = register("cypress_stairs",
            properties -> new StairBlock(CYPRESS_PLANKS.defaultBlockState(), plankProps(properties)));

    public static final SlabBlock CYPRESS_SLAB = register("cypress_slab",
            properties -> new SlabBlock(plankProps(properties)));

    public static final ButtonBlock CYPRESS_BUTTON = register("cypress_button",
            properties -> new ButtonBlock(CYPRESS_SET, 30, properties
                    .noCollision()
                    .strength(0.5F)
                    .pushReaction(PushReaction.DESTROY)));

    public static final PressurePlateBlock CYPRESS_PRESSURE_PLATE = register("cypress_pressure_plate",
            properties -> new PressurePlateBlock(CYPRESS_SET, properties
                    .mapColor(MapColor.COLOR_BROWN)
                    .forceSolidOn()
                    .instrument(NoteBlockInstrument.BASS)
                    .noCollision()
                    .strength(0.5F)
                    .ignitedByLava()
                    .pushReaction(PushReaction.DESTROY)));

    public static final StandingSignBlock CYPRESS_SIGN = registerBlock("cypress_sign",
            properties -> new StandingSignBlock(CYPRESS_TYPE, signProps(properties)));

    public static final WallSignBlock CYPRESS_WALL_SIGN = registerBlock("cypress_wall_sign",
            properties -> new WallSignBlock(CYPRESS_TYPE, signProps(properties)));

    public static final CeilingHangingSignBlock CYPRESS_HANGING_SIGN = registerBlock(
            "cypress_hanging_sign", properties -> new CeilingHangingSignBlock(CYPRESS_TYPE, signProps(properties)));

    public static final WallHangingSignBlock CYPRESS_WALL_HANGING_SIGN = registerBlock(
            "cypress_wall_hanging_sign", properties -> new WallHangingSignBlock(CYPRESS_TYPE, signProps(properties)));

    public static final DuckweedBlock DUCKWEED = register("duckweed",
            properties -> new DuckweedBlock(properties
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY)),

            PlaceOnWaterBlockItem::new);

    public static final RushesBlock RUSHES = register("rushes",
            properties -> new RushesBlock(properties
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY)));

    public static final ShortRushesBlock SHORT_RUSHES = register("short_rushes",
            properties -> new ShortRushesBlock(properties
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY)));

    public static final ShelfMushroomBlock SHELF_LUMINOUS_MUSHROOM = registerBlock("shelf_luminous_mushroom",
            properties -> new ShelfMushroomBlock(properties
                    .mapColor(MapColor.COLOR_YELLOW)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .lightLevel(state -> 7)
                    .pushReaction(PushReaction.DESTROY)));

    public static final LuminousMushroomBlock LUMINOUS_MUSHROOM = register("luminous_mushroom",
            properties -> new LuminousMushroomBlock(properties
                    .mapColor(MapColor.COLOR_YELLOW)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .lightLevel(state -> 7)
                    .pushReaction(PushReaction.DESTROY)),
            (block, properties) -> new StandingAndWallBlockItem(block, SHELF_LUMINOUS_MUSHROOM, Direction.DOWN, properties));

    public static final DrapingMossCarpetBlock MOSS_CARPET = registerBlock("moss_carpet",
            properties -> new DrapingMossCarpetBlock(properties
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(0.1F)
                    .randomTicks()
                    .sound(SoundType.MOSS_CARPET)
                    .pushReaction(PushReaction.DESTROY)));

    public static final HangingMossBlock HANGING_MOSS = registerBlock("hanging_moss",
            properties -> new HangingMossBlock(properties
                    .mapColor(MapColor.COLOR_GREEN)
                    .noCollision()
                    .instabreak()
                    .randomTicks()
                    .sound(SoundType.MOSS_CARPET)
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY)));

    public static final BonfireBlock BONFIRE = register("bonfire",
            properties -> new BonfireBlock(1.0F, properties
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
                    .lightLevel(state -> 15)
                    .noOcclusion()
                    .ignitedByLava()));

    public static final BonfireBlock SOUL_BONFIRE = register("soul_bonfire",
            properties -> new BonfireBlock(2.0F, properties
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
                    .lightLevel(state -> 10)
                    .noOcclusion()
                    .ignitedByLava()));

    public static final CauldronOfSoulsBlock CAULDRON_OF_SOULS = register("cauldron_of_souls",
            properties -> new CauldronOfSoulsBlock(properties
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(-1.0F, 3600000.0F)
                    .sound(SoundType.TRIAL_SPAWNER)
                    .lightLevel(state -> state.getValue(CauldronOfSoulsBlock.PHASE) == CauldronOfSoulsBlock.Phase.INACTIVE ? 3 : 10)
                    .noOcclusion()
                    .noLootTable()));

    public static final SummoningBonfireBlock SUMMONING_BONFIRE = register("summoning_bonfire",
            properties -> new SummoningBonfireBlock(properties
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(-1.0F, 3600000.0F)
                    .sound(SoundType.TRIAL_SPAWNER)
                    .lightLevel(state -> state.getValue(SummoningBonfireBlock.LIT) ? 12 : 0)
                    .noOcclusion()
                    .noLootTable()));

    public static final IncendiaryFireBlock INCENDIARY_FIRE = registerBlock("incendiary_fire",
            properties -> new IncendiaryFireBlock(properties
                    .mapColor(MapColor.COLOR_PURPLE)
                    .replaceable()
                    .noCollision()
                    .instabreak()
                    .lightLevel(state -> 12)
                    .sound(SoundType.WOOL)
                    .pushReaction(PushReaction.DESTROY)
                    .noLootTable()));

    public static final WildscapesCauldronBlock CAULDRON = registerBlock("cauldron",
            properties -> new WildscapesCauldronBlock(properties
                    .mapColor(MapColor.STONE)
                    .strength(2.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(state -> state.getValue(WildscapesCauldronBlock.BOILING) ? 3 : 0)));

    private static BlockBehaviour.Properties plankProps(BlockBehaviour.Properties properties) {
        return properties
                .mapColor(MapColor.COLOR_BROWN)
                .strength(2.0F, 3.0F)
                .sound(SoundType.WOOD)
                .ignitedByLava();
    }

    private static BlockBehaviour.Properties signProps(BlockBehaviour.Properties properties) {
        return properties
                .mapColor(MapColor.COLOR_BROWN)
                .forceSolidOn()
                .instrument(NoteBlockInstrument.BASS)
                .noCollision()
                .strength(1.0F)
                .ignitedByLava();
    }

    private static BlockBehaviour.Properties logProps(BlockBehaviour.Properties properties) {
        return properties
                .mapColor(MapColor.COLOR_BROWN)
                .strength(2.0F)
                .sound(SoundType.WOOD)
                .ignitedByLava();
    }

    static <T extends Block> T register(String name, Function<BlockBehaviour.Properties, T> factory) {
        return register(name, factory, BlockItem::new);
    }

    private static <T extends Block> T register(String name, Function<BlockBehaviour.Properties, T> factory,
            BiFunction<T, Item.Properties, ? extends BlockItem> itemFactory) {
        T block = registerBlock(name, factory);
        var key = ResourceKey.create(Registries.ITEM, Wildscapes.id(name));
        BlockItem item = itemFactory.apply(block, new Item.Properties().setId(key).useBlockDescriptionPrefix());
        Registry.register(BuiltInRegistries.ITEM, key, item);
        item.registerBlocks(Item.BY_BLOCK, item);
        return block;
    }

    private static <T extends Block> T registerBlock(String name, Function<BlockBehaviour.Properties, T> factory) {
        var key = ResourceKey.create(Registries.BLOCK, Wildscapes.id(name));
        return Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(BlockBehaviour.Properties.of().setId(key)));
    }

    public static void register() {
        MudBrickBlocks.init();
        CompostableRegistry compostables = CompostableRegistry.INSTANCE;
        compostables.add(CYPRESS_LEAVES, 0.3F);
        compostables.add(CYPRESS_SAPLING, 0.3F);
        compostables.add(DUCKWEED, 0.3F);
        compostables.add(SHORT_RUSHES, 0.3F);
        compostables.add(RUSHES, 0.5F);
        compostables.add(LUMINOUS_MUSHROOM, 0.65F);
        StrippableBlockRegistry.register(CYPRESS_LOG, STRIPPED_CYPRESS_LOG);
        StrippableBlockRegistry.register(CYPRESS_WOOD, STRIPPED_CYPRESS_WOOD);
        FabricBlockEntityType sign = (FabricBlockEntityType) BlockEntityTypes.SIGN;
        sign.addValidBlock(CYPRESS_SIGN);
        sign.addValidBlock(CYPRESS_WALL_SIGN);
        FabricBlockEntityType hangingSign = (FabricBlockEntityType) BlockEntityTypes.HANGING_SIGN;
        hangingSign.addValidBlock(CYPRESS_HANGING_SIGN);
        hangingSign.addValidBlock(CYPRESS_WALL_HANGING_SIGN);
    }
}
