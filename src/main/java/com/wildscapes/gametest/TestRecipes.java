package com.wildscapes.gametest;

import java.util.List;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;

final class TestRecipes {
    private TestRecipes() {}

    static CraftingContainer crafting(int width, int height, List<ItemStack> items) {
        AbstractContainerMenu menu = new AbstractContainerMenu(null, -1) {
            @Override
            public ItemStack quickMoveStack(Player player, int slot) {
                return ItemStack.EMPTY;
            }

            @Override
            public boolean stillValid(Player player) {
                return true;
            }
        };
        CraftingContainer grid = new TransientCraftingContainer(menu, width, height);
        for (int i = 0; i < items.size(); i++) {
            grid.setItem(i, items.get(i));
        }
        return grid;
    }

    static SimpleContainer slots(ItemStack... items) {
        return new SimpleContainer(items);
    }
}
