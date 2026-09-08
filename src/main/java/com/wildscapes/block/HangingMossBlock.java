package com.wildscapes.block;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HangingMossBlock extends Block {
    public static final MapCodec<HangingMossBlock> CODEC = simpleCodec(HangingMossBlock::new);

    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");

    private static final Map<Direction, BooleanProperty> BY_DIRECTION = ImmutableMap.of(
            Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH, Direction.WEST, WEST);

    private static final Map<Direction, VoxelShape> SHAPES = ImmutableMap.of(
            Direction.NORTH, box(0.0, 6.0, 0.0, 16.0, 16.0, 1.0),
            Direction.EAST, box(15.0, 6.0, 0.0, 16.0, 16.0, 16.0),
            Direction.SOUTH, box(0.0, 6.0, 15.0, 16.0, 16.0, 16.0),
            Direction.WEST, box(0.0, 6.0, 0.0, 1.0, 16.0, 16.0));

    public HangingMossBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false));
    }

    public static BooleanProperty faceFacing(Direction direction) {
        return BY_DIRECTION.get(direction);
    }

    public static void removeFace(Level level, BlockPos pos, BlockState state, Direction face) {
        BlockState without = state.setValue(faceFacing(face), false);
        if (isBare(without)) {
            level.removeBlock(pos, false);
        } else if (without != state) {
            level.setBlock(pos, without, Block.UPDATE_ALL);
        }
    }

    private static boolean isBare(BlockState state) {
        return BY_DIRECTION.values().stream().noneMatch(state::getValue);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = Shapes.empty();
        for (Map.Entry<Direction, BooleanProperty> side : BY_DIRECTION.entrySet()) {
            if (state.getValue(side.getValue())) {
                shape = Shapes.or(shape, SHAPES.get(side.getKey()));
            }
        }
        return shape.isEmpty() ? Shapes.block() : shape;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockState kept = state;
        for (Map.Entry<Direction, BooleanProperty> side : BY_DIRECTION.entrySet()) {
            if (state.getValue(side.getValue()) && !hasCarpet(level, pos, side.getKey())) {
                kept = kept.setValue(side.getValue(), false);
            }
        }
        if (kept == state) {
            return;
        }
        if (isBare(kept)) {
            level.removeBlock(pos, false);
        } else {
            level.setBlock(pos, kept, Block.UPDATE_ALL);
        }
    }

    private static boolean hasCarpet(LevelReader level, BlockPos pos, Direction face) {
        return isDraping(level, pos.above(2), face)
                || isDraping(level, pos.relative(face).above(2), face.getOpposite());
    }

    private static boolean isDraping(LevelReader level, BlockPos pos, Direction side) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof DrapingMossCarpetBlock
                && state.getValue(DrapingMossCarpetBlock.sideFacing(side)) == DrapingMossCarpetBlock.Drape.LONG;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }
}
