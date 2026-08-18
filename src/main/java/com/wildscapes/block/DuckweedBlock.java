package com.wildscapes.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Duckweed floats flat on the surface of still water, like a lily pad.
 * It may only be placed directly above a water source block.
 *
 * <p>A raft of it is thick enough to drag on anything wading through, the way a cobweb does —
 * only far gentler: it takes the edge off horizontal movement and leaves falling alone entirely,
 * so it slows a crossing rather than trapping anyone in it.
 */
public class DuckweedBlock extends BushBlock {
    public static final MapCodec<DuckweedBlock> CODEC = simpleCodec(DuckweedBlock::new);

    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);

    /**
     * How much horizontal momentum survives each tick spent in the weed.
     *
     * <p>Deliberately not {@code makeStuckInBlock}, which is how a cobweb does it: that also
     * zeroes the entity's momentum outright every tick ({@code Entity.move}), so every value
     * passed to it — gentle or not — comes out feeling like a cobweb. Scaling the movement
     * ourselves leaves momentum intact and makes the drag proportional.
     *
     * <p>It compounds against the friction already being applied, so the felt slowdown is larger
     * than the number suggests and depends on what you are doing: around a quarter slower
     * swimming through it, barely anything while walking the bed underneath, which is about
     * right for a mat of weed floating on the surface.
     */
    private static final double DRAG = 0.90D;

    public DuckweedBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        Vec3 movement = entity.getDeltaMovement();
        entity.setDeltaMovement(movement.x * DRAG, movement.y, movement.z * DRAG);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getFluidState().getType() == Fluids.WATER && state.getFluidState().isSource();
    }
}
