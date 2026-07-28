package com.wildscapes.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A decorative bonfire. The flames are an animated texture rather than a moving model,
 * so this is a plain block — the collision box is only the log cage, and the fire planes
 * rising out of it are not solid.
 *
 * <p>Standing in it burns you exactly as a campfire does: same damage source, same rate,
 * same one point of damage. A bonfire is always alight, so unlike a campfire there is no
 * lit state to check.
 */
public class BonfireBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);

    /** Matches {@code Blocks.CAMPFIRE}'s fire damage. */
    private static final float FIRE_DAMAGE = 1.0F;

    public BonfireBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity) {
            entity.hurt(level.damageSources().campfire(), FIRE_DAMAGE);
        }

        super.entityInside(state, level, pos, entity);
    }
}
