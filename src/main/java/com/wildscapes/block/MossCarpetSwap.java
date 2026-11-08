package com.wildscapes.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class MossCarpetSwap {
    private MossCarpetSwap() {}

    public static InteractionResult onBonemeal(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
        if (player.isSpectator() || level.isClientSide() || !player.getItemInHand(hand).is(Items.BONE_MEAL)) {
            return InteractionResult.PASS;
        }
        BlockPos pos = hit.getBlockPos();
        if (level.getBlockState(pos).is(Blocks.MOSS_CARPET)) {
            level.setBlock(pos, WildscapesBlocks.MOSS_CARPET.defaultBlockState(), Block.UPDATE_ALL);
        }
        return InteractionResult.PASS;
    }

    public static void onPlace(Level level, BlockPos pos) {
        if (level.isClientSide() || !level.getBlockState(pos).is(Blocks.MOSS_CARPET)) {
            return;
        }
        BlockState draping = WildscapesBlocks.MOSS_CARPET.defaultBlockState();

        level.setBlock(pos, draping, Block.UPDATE_ALL);
    }
}
