package com.wildscapes.worldgen;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class ClearProcessor extends StructureProcessor {
    public static final MapCodec<ClearProcessor> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(

                    Codec.intRange(0, 8).optionalFieldOf("margin", 1)
                            .forGetter(p -> p.margin),

                    Codec.intRange(0, 16).optionalFieldOf("headroom", 3)
                            .forGetter(p -> p.headroom))
                    .apply(instance, ClearProcessor::new));

    private static final Set<Long> OWN_GROUND = ConcurrentHashMap.newKeySet();

    private static final int REMEMBERED = 1 << 17;

    private final int margin;
    private final int headroom;

    public ClearProcessor(int margin, int headroom) {
        this.margin = margin;
        this.headroom = headroom;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return WildscapesStructures.CLEAR.get();
    }

    @Override
    public List<StructureTemplate.StructureBlockInfo> finalizeProcessing(ServerLevelAccessor level,
            BlockPos origin, BlockPos pivot,
            List<StructureTemplate.StructureBlockInfo> original,
            List<StructureTemplate.StructureBlockInfo> processed, StructurePlaceSettings settings) {
        if (processed.isEmpty()) {
            return processed;
        }

        remember(processed);

        BoundingBox box = null;
        for (StructureTemplate.StructureBlockInfo info : processed) {
            BoundingBox one = new BoundingBox(info.pos());
            box = box == null ? one : box.encapsulate(one);
        }

        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
        for (int x = box.minX() - margin; x <= box.maxX() + margin; x++) {
            for (int z = box.minZ() - margin; z <= box.maxZ() + margin; z++) {
                for (int y = box.minY() + 1; y <= box.maxY() + headroom; y++) {
                    at.set(x, y, z);
                    if (isLandscape(level, at)) {
                        level.setBlock(at, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
        return processed;
    }

    private static void remember(List<StructureTemplate.StructureBlockInfo> processed) {
        if (OWN_GROUND.size() > REMEMBERED) {
            OWN_GROUND.clear();
        }
        for (StructureTemplate.StructureBlockInfo info : processed) {
            if (isGroundMaterial(info.state())) {
                OWN_GROUND.add(info.pos().asLong());
            }
        }
    }

    private static boolean isLandscape(ServerLevelAccessor level, BlockPos at) {
        if (OWN_GROUND.contains(at.asLong())) {
            return false;
        }
        if (!isGroundMaterial(level.getBlockState(at))) {
            return false;
        }

        return !isBuilt(level.getBlockState(at.relative(Direction.DOWN)));
    }

    private static boolean isGroundMaterial(BlockState state) {
        if (state.isAir() || !state.getFluidState().isEmpty()) {
            return false;
        }
        return state.is(BlockTags.DIRT)
                || state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(BlockTags.SAND)
                || state.is(BlockTags.LEAVES)
                || state.is(BlockTags.REPLACEABLE)
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.CLAY)
                || state.is(Blocks.MUD)
                || state.is(Blocks.MOSS_BLOCK)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.POWDER_SNOW);
    }

    private static boolean isBuilt(BlockState state) {
        return state.is(BlockTags.PLANKS)
                || state.is(BlockTags.LOGS)
                || state.is(BlockTags.SLABS)
                || state.is(BlockTags.STAIRS)
                || state.is(BlockTags.WOODEN_TRAPDOORS)
                || state.is(BlockTags.FENCES)
                || state.is(BlockTags.WALLS)
                || state.is(Blocks.MOSSY_COBBLESTONE)
                || state.is(Blocks.COBBLESTONE)
                || state.is(Blocks.MUD_BRICKS)
                || state.is(Blocks.PACKED_MUD);
    }
}
