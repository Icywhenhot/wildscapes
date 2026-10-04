package com.wildscapes.block;

import com.wildscapes.particle.WildscapesParticles;
import com.wildscapes.sound.WildscapesSounds;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SummoningBonfireBlock extends Block {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 15.0D, 15.0D);

    public SummoningBonfireBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LIT, false));
    }

    public static void setLit(ServerLevel level, BlockPos pos, boolean lit) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof SummoningBonfireBlock) || state.getValue(LIT) == lit) {
            return;
        }
        level.setBlock(pos, state.setValue(LIT, lit), Block.UPDATE_ALL);
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.6;
        double z = pos.getZ() + 0.5;
        if (lit) {
            level.playSound(null, pos, WildscapesSounds.SUMMONING_BONFIRE_IGNITE.get(), SoundSource.BLOCKS, 1.0F,
                    0.9F + level.random.nextFloat() * 0.2F);
            level.sendParticles(WildscapesParticles.INCURSION_SPARK.get(), x, y, z, 14, 0.35, 0.4, 0.35, 0.06);
            level.sendParticles(WildscapesParticles.BONFIRE_EMBER.get(), x, y, z, 10, 0.3, 0.2, 0.3, 0.08);
        } else {
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 1.4F);
            level.sendParticles(WildscapesParticles.LOST_SOUL.get(), x, y + 0.3, z, 3, 0.2, 0.2, 0.2, 0.01);
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (state.getValue(LIT) && entity instanceof Player) {
            entity.hurt(level.damageSources().campfire(), 1.0F);
        }
        super.entityInside(state, level, pos, entity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }
        if (random.nextInt(10) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS,
                    0.5F + random.nextFloat(), random.nextFloat() * 0.7F + 0.6F, false);
        }
        if (random.nextInt(3) == 0) {
            level.addParticle(WildscapesParticles.BONFIRE_EMBER.get(),
                    pos.getX() + 0.3 + random.nextDouble() * 0.4, pos.getY() + 0.5 + random.nextDouble() * 0.5,
                    pos.getZ() + 0.3 + random.nextDouble() * 0.4, 0.0, 0.04 + random.nextDouble() * 0.03, 0.0);
        }
        if (random.nextInt(4) == 0) {
            level.addParticle(WildscapesParticles.INCURSION_SPARK.get(),
                    pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble() * 0.8,
                    pos.getZ() + random.nextDouble(), 0.0, 0.02, 0.0);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }
}
