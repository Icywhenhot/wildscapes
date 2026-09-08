package com.wildscapes.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.server.level.ServerLevel;

public class DrapingMossCarpetBlock extends CarpetBlock implements BonemealableBlock {
    public static final MapCodec<DrapingMossCarpetBlock> CODEC = simpleCodec(DrapingMossCarpetBlock::new);

    public enum Drape implements StringRepresentable {
        NONE("none"),
        SHORT("short"),
        LONG("long");

        private final String name;

        Drape(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public static final EnumProperty<Drape> NORTH = EnumProperty.create("north", Drape.class);
    public static final EnumProperty<Drape> EAST = EnumProperty.create("east", Drape.class);
    public static final EnumProperty<Drape> SOUTH = EnumProperty.create("south", Drape.class);
    public static final EnumProperty<Drape> WEST = EnumProperty.create("west", Drape.class);

    private static final Map<Direction, EnumProperty<Drape>> BY_DIRECTION = ImmutableMap.of(
            Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH, Direction.WEST, WEST);

    public DrapingMossCarpetBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(NORTH, Drape.NONE)
                .setValue(EAST, Drape.NONE)
                .setValue(SOUTH, Drape.NONE)
                .setValue(WEST, Drape.NONE));
    }

    public static EnumProperty<Drape> sideFacing(Direction direction) {
        return BY_DIRECTION.get(direction);
    }

    @Override
    public MapCodec<? extends CarpetBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        List<Direction> pending = unsettledSides(level, pos, state, false);
        if (pending.isEmpty()) {
            for (Direction side : BY_DIRECTION.keySet()) {
                if (state.getValue(sideFacing(side)) == Drape.LONG) {
                    growTail(level, pos, side);
                }
            }
            return;
        }
        step(level, pos, state, pending.get(random.nextInt(pending.size())));
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return !unsettledSides(level, pos, state, true).isEmpty();
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        List<Direction> pending = unsettledSides(level, pos, state, true);
        if (!pending.isEmpty()) {
            step(level, pos, state, pending.get(random.nextInt(pending.size())));
        }
    }

    private static List<Direction> unsettledSides(LevelReader level, BlockPos pos, BlockState state,
            boolean growingOnly) {
        List<Direction> pending = new ArrayList<>(4);
        for (Direction side : BY_DIRECTION.keySet()) {
            Drape current = state.getValue(sideFacing(side));
            Drape target = allowedDrape(level, pos, side);
            if (current == target || (growingOnly && current.ordinal() > target.ordinal())) {
                continue;
            }
            pending.add(side);
        }
        return pending;
    }

    private static void step(Level level, BlockPos pos, BlockState state, Direction side) {
        EnumProperty<Drape> property = sideFacing(side);
        Drape current = state.getValue(property);
        Drape target = allowedDrape(level, pos, side);
        Drape next = Drape.values()[current.ordinal() + (current.ordinal() < target.ordinal() ? 1 : -1)];
        if (next == Drape.LONG) {
            growTail(level, pos, side);
        } else if (current == Drape.LONG) {
            clearTail(level, pos, side);
        }
        level.setBlock(pos, state.setValue(property, next), Block.UPDATE_ALL);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!newState.is(this)) {
            for (Direction side : BY_DIRECTION.keySet()) {
                if (state.getValue(sideFacing(side)) == Drape.LONG) {
                    clearTail(level, pos, side);
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private static Drape allowedDrape(LevelReader level, BlockPos pos, Direction side) {
        if (!sideIsOpen(level, pos, side)) {
            return Drape.NONE;
        }
        return tailPos(level, pos, side) == null ? Drape.SHORT : Drape.LONG;
    }

    private static boolean sideIsOpen(LevelReader level, BlockPos pos, Direction side) {
        BlockPos beside = pos.below().relative(side);
        return !level.getBlockState(beside).isFaceSturdy(level, beside, side.getOpposite());
    }

    private static BlockPos tailPos(LevelReader level, BlockPos pos, Direction side) {
        BlockPos under = pos.below(2);
        if (canHoldTail(level, under)) {
            return under;
        }
        BlockPos beside = under.relative(side);
        return canHoldTail(level, beside) ? beside : null;
    }

    private static boolean canHoldTail(LevelReader level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.getBlock() instanceof HangingMossBlock;
    }

    private static Direction tailFace(BlockPos tail, BlockPos pos, Direction side) {
        return tail.equals(pos.below(2)) ? side : side.getOpposite();
    }

    private static void growTail(Level level, BlockPos pos, Direction side) {
        BlockPos tail = tailPos(level, pos, side);
        if (tail == null) {
            return;
        }
        BlockState existing = level.getBlockState(tail);
        BlockState base = existing.getBlock() instanceof HangingMossBlock
                ? existing
                : WildscapesBlocks.HANGING_MOSS.get().defaultBlockState();
        level.setBlock(tail, base.setValue(HangingMossBlock.faceFacing(tailFace(tail, pos, side)), true),
                Block.UPDATE_ALL);
    }

    private static void clearTail(Level level, BlockPos pos, Direction side) {
        for (BlockPos tail : new BlockPos[] { pos.below(2), pos.below(2).relative(side) }) {
            BlockState state = level.getBlockState(tail);
            if (state.getBlock() instanceof HangingMossBlock) {
                HangingMossBlock.removeFace(level, tail, state, tailFace(tail, pos, side));
            }
        }
    }
}
