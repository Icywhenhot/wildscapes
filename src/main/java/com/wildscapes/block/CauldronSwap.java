package com.wildscapes.block;

import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.InteractionHand;
import java.util.function.Consumer;

import com.wildscapes.block.entity.CauldronBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.wildscapes.item.StoredPotion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class CauldronSwap {
    private CauldronSwap() {}

    public static void onPlace(Level level, BlockPos pos) {
        BlockState placed = level.getBlockState(pos);

        if (isVanillaCauldron(placed) && WildscapesCauldronBlock.hasHeatSourceBelow(level, pos)) {
            swapForHeat(level, pos, placed);
            return;
        }

        if (isHeatSource(placed)) {
            heatCauldronAbove(level, pos.above());
        }
    }

    public static void onNeighborNotify(Level level, BlockPos pos) {
        if (isHeatSource(level.getBlockState(pos))) {
            heatCauldronAbove(level, pos.above());
        }
    }

    private static void heatCauldronAbove(Level level, BlockPos cauldronPos) {
        BlockState above = level.getBlockState(cauldronPos);
        if (isVanillaCauldron(above)) {
            swapForHeat(level, cauldronPos, above);
        }
    }

    private static void swapForHeat(Level level, BlockPos pos, BlockState vanilla) {
        int waterLevel = vanilla.is(Blocks.WATER_CAULDRON) ? vanilla.getValue(LayeredCauldronBlock.LEVEL) : 0;
        toOurBlock(level, pos, true, be -> {
            if (waterLevel > 0) {
                be.setPlainWater(waterLevel);
            }
        });
    }

    public static InteractionResult onRightClick(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
        if (player.isSpectator()) {
            return InteractionResult.PASS;
        }
        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        ItemStack held = player.getItemInHand(hand);

        if (held.getItem() instanceof DyeItem dye && state.is(Blocks.WATER_CAULDRON)) {
            int waterLevel = state.getValue(LayeredCauldronBlock.LEVEL);
            int color = CauldronBlockEntity.dyeColor(dye.getDyeColor());
            boolean heat = WildscapesCauldronBlock.hasHeatSourceBelow(level, pos);
            return finish(level, () -> {
                toOurBlock(level, pos, heat, be -> be.setDyedWater(waterLevel, color));
                consume(player, held);
            });
        }

        if (isStorablePotion(held) && state.is(Blocks.CAULDRON)) {
            StoredPotion pc = StoredPotion.read(held);
            boolean heat = WildscapesCauldronBlock.hasHeatSourceBelow(level, pos);
            return finish(level, () -> {
                toOurBlock(level, pos, heat, be -> be.addPotion(pc));
                giveBottle(player, held);
            });
        }
        return InteractionResult.PASS;
    }

    private static void toOurBlock(Level level, BlockPos pos, boolean boiling, Consumer<CauldronBlockEntity> init) {
        level.setBlock(pos, WildscapesBlocks.CAULDRON.get().defaultBlockState()
                .setValue(WildscapesCauldronBlock.BOILING, boiling), Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof CauldronBlockEntity be) {
            init.accept(be);
        }
    }

    private static InteractionResult finish(Level level, Runnable serverAction) {
        if (!level.isClientSide) {
            serverAction.run();
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void consume(Player player, ItemStack held) {
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
    }

    private static void giveBottle(Player player, ItemStack held) {
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
        if (!player.getInventory().add(bottle)) {
            player.drop(bottle, false);
        }
    }

    private static boolean isVanillaCauldron(BlockState state) {
        return state.is(Blocks.CAULDRON) || state.is(Blocks.WATER_CAULDRON);
    }

    private static boolean isHeatSource(BlockState state) {
        return state.getBlock() instanceof BonfireBlock
                || (state.getBlock() instanceof net.minecraft.world.level.block.CampfireBlock
                        && state.getValue(net.minecraft.world.level.block.CampfireBlock.LIT));
    }

    private static boolean isStorablePotion(ItemStack stack) {
        if (stack.getItem() != Items.POTION && stack.getItem() != Items.SPLASH_POTION
                && stack.getItem() != Items.LINGERING_POTION) {
            return false;
        }
        StoredPotion pc = StoredPotion.read(stack);
        return !pc.is(net.minecraft.world.item.alchemy.Potions.WATER);
    }
}
