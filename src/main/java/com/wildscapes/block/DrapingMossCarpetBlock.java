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

/**
 * A moss carpet that grows a curtain of moss over the edges it sits on.
 *
 * <p>Players never craft or hold this: {@link MossCarpetSwap} puts it in the place of a plain
 * {@code minecraft:moss_carpet} the moment one is laid down, and breaking it hands back a vanilla
 * moss carpet again, so as far as anyone can tell it simply is the moss carpet. A fresh one is
 * bare; each side then creeps down on its own, first a short fringe and later a full curtain with
 * a ragged tail (the tail is a separate {@link HangingMossBlock}, because a block model cannot
 * reach further than one block past its own edge).
 *
 * <p>Bone meal hurries a carpet along by one stage, the same step a random tick would have made.
 * Only sides that have somewhere to hang ever grow: put a carpet on a lone block and moss spills
 * off all four edges, put a row of them along the top of a wall and only the two open faces drape,
 * since the rest would be growing into stone. Each side re-checks that every time it is ticked, so
 * walling one in trims the moss back on its own.
 */
public class DrapingMossCarpetBlock extends CarpetBlock implements BonemealableBlock {
    public static final MapCodec<DrapingMossCarpetBlock> CODEC = simpleCodec(DrapingMossCarpetBlock::new);

    /** How far the moss has crept down one side. */
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

    /**
     * One side per tick creeps a step towards where it should be — down as it grows, back up if
     * something has since been built against it. Taking a single side at a time is what makes a
     * carpet fill in unevenly, which is how a real one would; which side is picked out of the ones
     * with somewhere left to go, so a carpet that is nearly done still finishes at a steady pace.
     */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        List<Direction> pending = unsettledSides(level, pos, state, false);
        if (pending.isEmpty()) {
            // Settled — but a tail may have been broken off since, so put any missing one back.
            for (Direction side : BY_DIRECTION.keySet()) {
                if (state.getValue(sideFacing(side)) == Drape.LONG) {
                    growTail(level, pos, side);
                }
            }
            return;
        }
        step(level, pos, state, pending.get(random.nextInt(pending.size())));
    }

    // ---- Bone meal ---------------------------------------------------------------

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return !unsettledSides(level, pos, state, true).isEmpty();
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    /** A pinch of bone meal is worth one turn of the growth a random tick would have done. */
    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        List<Direction> pending = unsettledSides(level, pos, state, true);
        if (!pending.isEmpty()) {
            step(level, pos, state, pending.get(random.nextInt(pending.size())));
        }
    }

    // ---- Growing -----------------------------------------------------------------

    /**
     * Sides that are not yet where they should be. Bone meal passes {@code growingOnly}, since
     * hurrying a carpet along should never be what trims it back.
     */
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

    /** Moves one side a single stage towards where it should be, tail and all. */
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

    // ---- Where the moss is allowed to hang ---------------------------------------

    /** The longest curtain this side has room for right now. */
    private static Drape allowedDrape(LevelReader level, BlockPos pos, Direction side) {
        if (!sideIsOpen(level, pos, side)) {
            return Drape.NONE;
        }
        return tailPos(level, pos, side) == null ? Drape.SHORT : Drape.LONG;
    }

    /** Whether the face of the supporting block on this side looks out onto open air. */
    private static boolean sideIsOpen(LevelReader level, BlockPos pos, Direction side) {
        BlockPos beside = pos.below().relative(side);
        return !level.getBlockState(beside).isFaceSturdy(level, beside, side.getOpposite());
    }

    /**
     * Where the ragged tail of a full curtain would live, or null if there is no room for one.
     * The tail hangs a block below the supporting block's face: straight down when the carpet is
     * on a ledge and there is nothing under it, or out to the side when it is on a taller wall.
     */
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

    /**
     * The face of the tail block that the moss hangs on. Hosting it in the column under the
     * carpet means the moss is on that block's outward face; hosting it out to the side of a
     * wall means the moss is on the face pointing back at the wall. Either way it is the same
     * sheet of moss, in the same place.
     */
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
