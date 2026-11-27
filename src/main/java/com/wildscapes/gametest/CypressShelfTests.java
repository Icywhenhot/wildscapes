package com.wildscapes.gametest;

import com.wildscapes.block.CypressShelfBlock;
import com.wildscapes.block.WildscapesBlocks;
import com.wildscapes.block.entity.CypressShelfBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class CypressShelfTests {
    private CypressShelfTests() {}

    public static void storesItemsAndSwapsHotbars(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(3, 3, 3));
        level.setBlockAndUpdate(pos, WildscapesBlocks.CYPRESS_SHELF.defaultBlockState());
        var shelf = (CypressShelfBlockEntity) level.getBlockEntity(pos);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getInventory().selected = 0;
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOWL, 3));
        shelf.setItem(1, new ItemStack(Items.APPLE, 2));
        click(player, pos);
        helper.assertTrue(player.getMainHandItem().is(Items.APPLE) && player.getMainHandItem().getCount() == 2,
                "shelf did not return its item");
        helper.assertTrue(shelf.getItem(1).is(Items.BOWL) && shelf.getItem(1).getCount() == 3,
                "shelf did not store the held stack");
        var saved = shelf.saveWithFullMetadata(level.registryAccess());
        var loaded = (CypressShelfBlockEntity) BlockEntity.loadStatic(pos, shelf.getBlockState(), saved, level.registryAccess());
        helper.assertTrue(loaded != null && ItemStack.matches(loaded.getItem(1), shelf.getItem(1)), "shelf inventory was lost on reload");

        BlockPos row = helper.absolutePos(new BlockPos(7, 3, 7));
        for (int i = 0; i < 3; i++) {
            level.setBlockAndUpdate(row.east(i).below(), Blocks.REDSTONE_BLOCK.defaultBlockState());
            level.setBlockAndUpdate(row.east(i), WildscapesBlocks.CYPRESS_SHELF.defaultBlockState().setValue(CypressShelfBlock.POWERED, true));
        }
        helper.assertTrue(WildscapesBlocks.CYPRESS_SHELF.getAllBlocksConnectedTo(level, row.east()).size() == 3,
                "powered shelves did not form a three-block chain");
        for (int i = 0; i < 9; i++) player.getInventory().setItem(i, new ItemStack(Items.EMERALD, i + 1));
        click(player, row.east());
        for (int i = 0; i < 9; i++) helper.assertTrue(player.getInventory().getItem(i).isEmpty(), "hotbar slot was not stored");
        click(player, row.east());
        for (int i = 0; i < 9; i++) {
            var stack = player.getInventory().getItem(i);
            helper.assertTrue(stack.is(Items.EMERALD) && stack.getCount() == i + 1, "hotbar order changed after two swaps");
        }
        helper.succeed();
    }

    private static void click(Player player, BlockPos pos) {
        var level = player.level();
        var hit = new BlockHitResult(Vec3.atCenterOf(pos).add(0, 0, -0.5), Direction.NORTH, pos, false);
        var state = level.getBlockState(pos);
        if (player.getMainHandItem().isEmpty()) state.useWithoutItem(level, player, hit);
        else state.useItemOn(player.getMainHandItem(), level, player, InteractionHand.MAIN_HAND, hit);
    }
}
