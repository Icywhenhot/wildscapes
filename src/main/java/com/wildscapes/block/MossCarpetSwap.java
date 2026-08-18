package com.wildscapes.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Puts {@link DrapingMossCarpetBlock} in the place of a vanilla moss carpet as it is laid down,
 * the same trick {@link CauldronSwap} plays on the cauldron: the vanilla block has nowhere to
 * record which of its edges have grown moss, so we quietly hand the job to one that does.
 *
 * <p>Only carpets someone places or bone-meals go through here. Moss that generates with the
 * world is otherwise left as vanilla laid it, so no existing world gets rewritten underfoot.
 */
public final class MossCarpetSwap {
    private MossCarpetSwap() {}

    /**
     * Bone meal on a plain moss carpet hands it over too, so a carpet that came with the terrain
     * or with a hand-built tree can be coaxed into draping instead of having to be broken and
     * laid again. The event is deliberately not cancelled: once the block underneath has become
     * ours, vanilla's own bone meal handling finds a block it can grow and does the rest.
     */
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
        // Bare to begin with — the drapes are the point of waiting.
        level.setBlock(pos, draping, Block.UPDATE_ALL);
    }
}
