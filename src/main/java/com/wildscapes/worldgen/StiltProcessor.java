package com.wildscapes.worldgen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.serialization.MapCodec;
import com.wildscapes.block.WildscapesBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class StiltProcessor extends StructureProcessor {
    public static final StiltProcessor INSTANCE = new StiltProcessor();
    public static final MapCodec<StiltProcessor> CODEC = MapCodec.unit(() -> INSTANCE);

    private static final int MAX_DEPTH = 24;

    @Override
    protected StructureProcessorType<?> getType() {
        return WildscapesStructures.STILTS.get();
    }

    @Override
    public List<StructureTemplate.StructureBlockInfo> finalizeProcessing(ServerLevelAccessor level,
            BlockPos origin, BlockPos pivot,
            List<StructureTemplate.StructureBlockInfo> original,
            List<StructureTemplate.StructureBlockInfo> processed, StructurePlaceSettings settings) {
        if (processed.isEmpty()) {
            return processed;
        }

        Map<Long, StructureTemplate.StructureBlockInfo> lowest = new HashMap<>();
        for (StructureTemplate.StructureBlockInfo info : processed) {
            if (isNothing(info.state())) {
                continue;
            }
            BlockPos at = info.pos();
            long column = ChunkPos.asLong(at.getX(), at.getZ());
            StructureTemplate.StructureBlockInfo held = lowest.get(column);
            if (held == null || at.getY() < held.pos().getY()) {
                lowest.put(column, info);
            }
        }

        BlockState post = WildscapesBlocks.CYPRESS_LOG.get().defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);

        for (StructureTemplate.StructureBlockInfo info : lowest.values()) {
            if (isPost(info.state())) {
                drive(level, info.pos(), post);
            }
        }

        return processed;
    }

    private static boolean isNothing(BlockState state) {
        return state.isAir() || state.is(Blocks.STRUCTURE_VOID);
    }

    private static boolean isPost(BlockState state) {
        return state.is(WildscapesBlocks.CYPRESS_LOG.get())
                && state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y;
    }

    private static void drive(ServerLevelAccessor level, BlockPos from, BlockState post) {
        BlockPos.MutableBlockPos cursor = from.mutable();
        for (int i = 0; i < MAX_DEPTH; i++) {
            cursor.move(0, -1, 0);
            if (level.isOutsideBuildHeight(cursor)) {
                return;
            }
            BlockState here = level.getBlockState(cursor);

            if (!here.isAir() && !here.getFluidState().is(FluidTags.WATER) && !here.canBeReplaced()) {
                return;
            }
            level.setBlock(cursor, post, Block.UPDATE_CLIENTS);
        }
    }
}
