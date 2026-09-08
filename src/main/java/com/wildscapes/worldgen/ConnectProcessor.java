package com.wildscapes.worldgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class ConnectProcessor extends StructureProcessor {
    public static final ConnectProcessor INSTANCE = new ConnectProcessor();
    public static final MapCodec<ConnectProcessor> CODEC = MapCodec.unit(() -> INSTANCE);

    @Override
    protected StructureProcessorType<?> getType() {
        return WildscapesStructures.CONNECT.get();
    }

    @Override
    public List<StructureTemplate.StructureBlockInfo> finalizeProcessing(ServerLevelAccessor level,
            BlockPos origin, BlockPos pivot,
            List<StructureTemplate.StructureBlockInfo> original,
            List<StructureTemplate.StructureBlockInfo> processed, StructurePlaceSettings settings) {
        Mirror mirror = settings.getMirror();
        Rotation rotation = settings.getRotation();

        Map<BlockPos, BlockState> incoming = new HashMap<>();
        for (StructureTemplate.StructureBlockInfo info : processed) {
            incoming.put(info.pos(), asPlaced(info.state(), mirror, rotation));
        }

        List<StructureTemplate.StructureBlockInfo> mended = new ArrayList<>(processed.size());
        for (StructureTemplate.StructureBlockInfo info : processed) {
            BlockState filed = info.state();
            if (!joins(filed)) {
                mended.add(info);
                continue;
            }

            BlockPos at = info.pos();
            BlockState standing = asPlaced(filed, mirror, rotation);
            BlockState state = standing;

            for (Direction side : Direction.Plane.HORIZONTAL) {
                BlockPos beside = at.relative(side);
                BlockState neighbour = incoming.containsKey(beside)
                        ? incoming.get(beside)
                        : level.getBlockState(beside);
                state = state.updateShape(side, neighbour, level, at, beside);

                if (!incoming.containsKey(beside) && joins(neighbour)) {
                    BlockState rejoined = neighbour.updateShape(
                            side.getOpposite(), standing, level, beside, at);
                    if (rejoined != neighbour) {
                        level.setBlock(beside, rejoined, Block.UPDATE_CLIENTS);
                    }
                }
            }

            BlockState refiled = asFiled(state, mirror, rotation);
            mended.add(refiled == filed ? info
                    : new StructureTemplate.StructureBlockInfo(at, refiled, info.nbt()));
        }
        return mended;
    }

    private static BlockState asPlaced(BlockState state, Mirror mirror, Rotation rotation) {
        return state.mirror(mirror).rotate(rotation);
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

    private static boolean joins(BlockState state) {
        Block block = state.getBlock();
        return block instanceof CrossCollisionBlock
                || block instanceof WallBlock
                || block instanceof FenceGateBlock;
    }
}
