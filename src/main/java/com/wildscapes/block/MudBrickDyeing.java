package com.wildscapes.block;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

public final class MudBrickDyeing {
    private MudBrickDyeing() {}

    private static final Map<Block, Map<DyeColor, Block>> FAMILIES = new HashMap<>();

    public static void buildLookup() {
        FAMILIES.clear();
        addFamily(Blocks.MUD_BRICKS, MudBrickBlocks.BRICKS);
        addFamily(MudBrickBlocks.CHISELED_MUD_BRICKS, MudBrickBlocks.CHISELED);
        addFamily(Blocks.MUD_BRICK_STAIRS, MudBrickBlocks.STAIRS);
        addFamily(Blocks.MUD_BRICK_SLAB, MudBrickBlocks.SLABS);
        addFamily(Blocks.MUD_BRICK_WALL, MudBrickBlocks.WALLS);
    }

    private static void addFamily(Block undyed, Map<DyeColor, ? extends Block> dyed) {
        Map<DyeColor, Block> byColor = new EnumMap<>(DyeColor.class);
        byColor.putAll(dyed);

        FAMILIES.put(undyed, byColor);
        byColor.values().forEach(block -> FAMILIES.put(block, byColor));
    }

    public static InteractionResult onRightClick(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
        if (player.isSpectator()) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof DyeItem dye)) {
            return InteractionResult.PASS;
        }

        BlockPos pos = hit.getBlockPos();
        BlockState dyed = recolour(level.getBlockState(pos), dye.getDyeColor());
        if (dyed == null) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        level.setBlockAndUpdate(pos, dyed);
        level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    private static BlockState recolour(BlockState state, DyeColor color) {
        Map<DyeColor, Block> family = FAMILIES.get(state.getBlock());
        if (family == null) {
            return null;
        }
        Block target = family.get(color);
        if (target == null || target == state.getBlock()) {
            return null;
        }

        BlockState dyed = target.defaultBlockState();
        for (Property<?> property : state.getProperties()) {
            dyed = copyProperty(state, dyed, property);
        }
        return dyed;
    }

    private static <T extends Comparable<T>> BlockState copyProperty(BlockState from, BlockState to,
            Property<T> property) {
        return to.hasProperty(property) ? to.setValue(property, from.getValue(property)) : to;
    }
}
