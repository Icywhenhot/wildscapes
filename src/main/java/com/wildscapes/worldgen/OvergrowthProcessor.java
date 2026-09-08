package com.wildscapes.worldgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wildscapes.block.WildscapesBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class OvergrowthProcessor extends StructureProcessor {
    public static final MapCodec<OvergrowthProcessor> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.floatRange(0.0F, 1.0F).optionalFieldOf("vines", 0.12F)
                            .forGetter(p -> p.vines),
                    Codec.floatRange(0.0F, 1.0F).optionalFieldOf("moss", 0.08F)
                            .forGetter(p -> p.moss))
                    .apply(instance, OvergrowthProcessor::new));

    private final float vines;
    private final float moss;

    public OvergrowthProcessor(float vines, float moss) {
        this.vines = vines;
        this.moss = moss;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return WildscapesStructures.OVERGROWTH.get();
    }

    @Override
    public List<StructureTemplate.StructureBlockInfo> finalizeProcessing(ServerLevelAccessor level,
            BlockPos origin, BlockPos pivot,
            List<StructureTemplate.StructureBlockInfo> original,
            List<StructureTemplate.StructureBlockInfo> processed, StructurePlaceSettings settings) {
        if (processed.isEmpty() || (vines <= 0.0F && moss <= 0.0F)) {
            return processed;
        }

        Map<BlockPos, BlockState> standing = new HashMap<>();
        for (StructureTemplate.StructureBlockInfo info : processed) {
            standing.put(info.pos(), info.state());
        }

        Rotation rotation = settings.getRotation();
        Mirror mirror = settings.getMirror();
        List<StructureTemplate.StructureBlockInfo> grown = new ArrayList<>(processed.size());

        for (StructureTemplate.StructureBlockInfo info : processed) {
            BlockPos at = info.pos();
            if (!info.state().isAir() || !level.getBlockState(at).getFluidState().isEmpty()) {
                grown.add(info);
                continue;
            }

            RandomSource random = settings.getRandom(at);
            BlockState grew = grow(standing, at, random, rotation, mirror);
            grown.add(grew == null ? info : new StructureTemplate.StructureBlockInfo(at, grew, null));
        }
        return grown;
    }

    private BlockState grow(Map<BlockPos, BlockState> standing, BlockPos at, RandomSource random,
            Rotation rotation, Mirror mirror) {
        if (holds(standing.get(at.below())) && random.nextFloat() < moss) {
            return WildscapesBlocks.MOSS_CARPET.get().defaultBlockState();
        }
        if (random.nextFloat() >= vines) {
            return null;
        }
        Direction face = pickFace(standing, at, random);
        if (face == null) {
            return null;
        }
        BlockState vine = Blocks.VINE.defaultBlockState()
                .setValue(VineBlock.PROPERTY_BY_DIRECTION.get(face), true);
        return asFiled(vine, mirror, rotation);
    }

    private static Direction pickFace(Map<BlockPos, BlockState> standing, BlockPos at, RandomSource random) {
        List<Direction> faces = new ArrayList<>(4);
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (holds(standing.get(at.relative(side)))) {
                faces.add(side);
            }
        }
        return faces.isEmpty() ? null : faces.get(random.nextInt(faces.size()));
    }

    private static boolean holds(BlockState state) {
        return state != null && state.isSolid() && !state.is(Blocks.STRUCTURE_VOID);
    }

    private static BlockState asFiled(BlockState state, Mirror mirror, Rotation rotation) {
        return state.rotate(opposite(rotation)).mirror(mirror);
    }

    private static Rotation opposite(Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90 -> Rotation.COUNTERCLOCKWISE_90;
            case COUNTERCLOCKWISE_90 -> Rotation.CLOCKWISE_90;
            default -> rotation;
        };
    }
}
