package com.wildscapes.block;

import javax.annotation.Nullable;

import com.wildscapes.block.entity.CauldronOfSoulsBlockEntity;
import com.wildscapes.block.entity.WildscapesBlockEntities;
import com.wildscapes.particle.WildscapesParticles;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CauldronOfSoulsBlock extends Block implements EntityBlock {
    public static final EnumProperty<Phase> PHASE = EnumProperty.create("phase", Phase.class);

    private static final VoxelShape SHAPE = Shapes.or(box(0.0, 3.0, 0.0, 16.0, 16.0, 16.0),
            box(0.0, 0.0, 0.0, 4.0, 3.0, 4.0), box(12.0, 0.0, 0.0, 16.0, 3.0, 4.0),
            box(0.0, 0.0, 12.0, 4.0, 3.0, 16.0), box(12.0, 0.0, 12.0, 16.0, 3.0, 16.0));

    public CauldronOfSoulsBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(PHASE, Phase.INACTIVE));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof CauldronOfSoulsBlockEntity be) {
            be.collapse();
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(PHASE) == Phase.INACTIVE || random.nextInt(4) != 0) {
            return;
        }
        level.addParticle(WildscapesParticles.CAULDRON_SWIRL.get(),
                pos.getX() + 0.25 + random.nextDouble() * 0.5, pos.getY() + 1.05, pos.getZ() + 0.25 + random.nextDouble() * 0.5,
                0.0, 0.03 + random.nextDouble() * 0.02, 0.0);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CauldronOfSoulsBlockEntity(pos, state);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != WildscapesBlockEntities.CAULDRON_OF_SOULS.get()) {
            return null;
        }
        BlockEntityTicker<CauldronOfSoulsBlockEntity> ticker = level.isClientSide
                ? CauldronOfSoulsBlockEntity::clientTick
                : CauldronOfSoulsBlockEntity::serverTick;
        return (BlockEntityTicker<T>) ticker;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PHASE);
    }

    public enum Phase implements StringRepresentable {
        INACTIVE("inactive"),
        ACTIVE("active"),
        REWARDS("rewards");

        private final String name;

        Phase(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
