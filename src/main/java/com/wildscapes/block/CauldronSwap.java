package com.wildscapes.block;

import java.util.function.Consumer;

import com.wildscapes.block.entity.CauldronBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Bridges the vanilla cauldron and {@link WildscapesCauldronBlock}. A real {@code minecraft:cauldron}
 * is swapped for our block the moment it gains a heat source below (so it can boil) or is
 * right-clicked with a dye or potion (so it can hold that enhanced state, which the vanilla
 * cauldron has nowhere to store). Reverting the other way is handled by
 * {@link CauldronBlockEntity#serverTick} once the cauldron is plain and cold again.
 */
public final class CauldronSwap {
    private CauldronSwap() {}

    // ---- Heat: swap in on placement / lighting ----------------------------------

    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof Level level)) {
            return;
        }
        BlockPos pos = event.getPos();
        BlockState placed = event.getPlacedBlock();

        // A cauldron dropped straight onto a heat source.
        if (isVanillaCauldron(placed) && WildscapesCauldronBlock.hasHeatSourceBelow(level, pos)) {
            swapForHeat(level, pos, placed);
            return;
        }
        // A heat source slid under an existing cauldron.
        if (isHeatSource(placed)) {
            heatCauldronAbove(level, pos.above());
        }
    }

    public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        if (!(event.getLevel() instanceof Level level)) {
            return;
        }
        // Catches a campfire being lit under a cauldron (its state change notifies neighbours).
        if (isHeatSource(event.getState())) {
            heatCauldronAbove(level, event.getPos().above());
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

    // ---- Dye / potion: swap in on interaction -----------------------------------

    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        ItemStack held = event.getItemStack();
        Player player = event.getEntity();

        // Dye a water cauldron.
        if (held.getItem() instanceof DyeItem dye && state.is(Blocks.WATER_CAULDRON)) {
            int waterLevel = state.getValue(LayeredCauldronBlock.LEVEL);
            int color = dye.getDyeColor().getTextureDiffuseColor() & 0xFFFFFF;
            boolean heat = WildscapesCauldronBlock.hasHeatSourceBelow(level, pos);
            finish(event, level, () -> {
                toOurBlock(level, pos, heat, be -> be.setDyedWater(waterLevel, color));
                consume(player, held);
            });
            return;
        }

        // Pour a potion into an empty cauldron.
        if (isStorablePotion(held) && state.is(Blocks.CAULDRON)) {
            PotionContents pc = held.get(DataComponents.POTION_CONTENTS);
            boolean heat = WildscapesCauldronBlock.hasHeatSourceBelow(level, pos);
            finish(event, level, () -> {
                toOurBlock(level, pos, heat, be -> be.addPotion(pc));
                giveBottle(player, held);
            });
        }
    }

    // ---- Helpers ----------------------------------------------------------------

    private static void toOurBlock(Level level, BlockPos pos, boolean boiling, Consumer<CauldronBlockEntity> init) {
        level.setBlock(pos, WildscapesBlocks.CAULDRON.get().defaultBlockState()
                .setValue(WildscapesCauldronBlock.BOILING, boiling), Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof CauldronBlockEntity be) {
            init.accept(be);
        }
    }

    /** Cancels the vanilla interaction and runs our swap server-side, keeping the arm swing on the client. */
    private static void finish(PlayerInteractEvent.RightClickBlock event, Level level, Runnable serverAction) {
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
        if (!level.isClientSide) {
            serverAction.run();
        }
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
        return state.is(WildscapesBlocks.BONFIRE.get())
                || (state.getBlock() instanceof net.minecraft.world.level.block.CampfireBlock
                        && state.getValue(net.minecraft.world.level.block.CampfireBlock.LIT));
    }

    private static boolean isStorablePotion(ItemStack stack) {
        if (stack.getItem() != Items.POTION && stack.getItem() != Items.SPLASH_POTION
                && stack.getItem() != Items.LINGERING_POTION) {
            return false;
        }
        PotionContents pc = stack.get(DataComponents.POTION_CONTENTS);
        return pc != null && !pc.is(net.minecraft.world.item.alchemy.Potions.WATER);
    }
}
