package com.wildscapes.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BonfireBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 15.0D, 15.0D);
    private static final int SMOKE_SCAN_RANGE = 5;

    private final float damage;

    public BonfireBlock(float damage, Properties properties) {
        super(properties);
        this.damage = damage;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity) {
            entity.hurt(level.damageSources().campfire(), damage);
        }

        super.entityInside(state, level, pos, entity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(10) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS,
                    0.5F + random.nextFloat(), random.nextFloat() * 0.7F + 0.6F, false);
        }

        if (random.nextInt(5) == 0) {
            boolean signal = isSmokeyPos(level, pos);
            for (int i = 0; i < random.nextInt(1) + 1; i++) {
                makeParticles(level, pos, signal);
            }
        }
    }

    private static void makeParticles(Level level, BlockPos pos, boolean signal) {
        RandomSource random = level.getRandom();
        SimpleParticleType particle = signal ? ParticleTypes.CAMPFIRE_SIGNAL_SMOKE : ParticleTypes.CAMPFIRE_COSY_SMOKE;
        level.addAlwaysVisibleParticle(particle, true,
                pos.getX() + 0.5 + random.nextDouble() / 3.0 * (random.nextBoolean() ? 1 : -1),
                pos.getY() + random.nextDouble() + random.nextDouble(),
                pos.getZ() + 0.5 + random.nextDouble() / 3.0 * (random.nextBoolean() ? 1 : -1),
                0.0, 0.07, 0.0);
        if (random.nextInt(4) == 0) {
            makeParticles(level, pos, signal);
        }
    }

    private static boolean isSmokeyPos(Level level, BlockPos pos) {
        for (int i = 1; i <= SMOKE_SCAN_RANGE; i++) {
            BlockPos below = pos.below(i);
            BlockState state = level.getBlockState(below);
            if (state.is(Blocks.HAY_BLOCK)) {
                return true;
            }
            if (Shapes.faceShapeOccludes(Shapes.empty(), state.getCollisionShape(level, below))) {
                return false;
            }
        }
        return false;
    }
}
