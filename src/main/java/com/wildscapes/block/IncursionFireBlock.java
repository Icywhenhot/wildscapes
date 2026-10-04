package com.wildscapes.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class IncursionFireBlock extends BaseFireBlock {
    public static final MapCodec<IncursionFireBlock> CODEC = simpleCodec(IncursionFireBlock::new);

    public IncursionFireBlock(Properties properties) {
        super(properties, 1.0F);
    }

    public static void spread(Level level, Vec3 at) {
        BlockPos center = BlockPos.containing(at);
        BlockState fire = WildscapesBlocks.INCURSION_FIRE.get().defaultBlockState();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy : new int[] {0, 1, -1}) {
                    BlockPos p = center.offset(dx, dy, dz);
                    BlockState here = level.getBlockState(p);
                    if ((here.isAir() || here.canBeReplaced() && here.getFluidState().isEmpty())
                            && fire.canSurvive(level, p)) {
                        level.setBlock(p, fire, Block.UPDATE_ALL);
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected MapCodec<? extends BaseFireBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean canBurn(BlockState state) {
        return true;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
            BlockPos pos, BlockPos neighborPos) {
        return canSurvive(state, level, pos) ? state : Blocks.AIR.defaultBlockState();
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
        super.onPlace(state, level, pos, oldState, moved);
        level.scheduleTick(pos, this, 100 + level.random.nextInt(60));
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.removeBlock(pos, false);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof Player) {
            super.entityInside(state, level, pos, entity);
        }
    }
}
