package com.wildscapes.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

public final class MossCarpetSwap {
    private MossCarpetSwap() {}

    public static void onBonemeal(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide || !event.getItemStack().is(Items.BONE_MEAL)) {
            return;
        }
        BlockPos pos = event.getPos();
        if (level.getBlockState(pos).is(Blocks.MOSS_CARPET)) {
            level.setBlock(pos, WildscapesBlocks.MOSS_CARPET.get().defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof Level level) || level.isClientSide) {
            return;
        }
        if (!event.getPlacedBlock().is(Blocks.MOSS_CARPET)) {
            return;
        }
        BlockPos pos = event.getPos();
        BlockState draping = WildscapesBlocks.MOSS_CARPET.get().defaultBlockState();

        level.setBlock(pos, draping, Block.UPDATE_ALL);
    }
}
