package com.wildscapes.item;

import java.util.List;

import com.wildscapes.Wildscapes;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class VanillaTweaks {
    private VanillaTweaks() {}

    private static final List<Item> STEWS =
            List.of(Items.MUSHROOM_STEW, Items.RABBIT_STEW, Items.BEETROOT_SOUP, Items.SUSPICIOUS_STEW);

    public static void setup() {
        Items.POTION.maxStackSize = 8;
        STEWS.forEach(stew -> stew.maxStackSize = 8);
    }

    public static boolean isFastFood(ItemStack stack) {
        return STEWS.contains(stack.getItem()) || stack.getItem() instanceof MagicSoupItem;
    }

    public static ItemStack finishEating(LivingEntity entity, ItemStack eaten, ItemStack result) {
        if (!isFastFood(eaten) || eaten.getCount() <= 1) {
            return result;
        }
        if (entity instanceof Player player && player.getAbilities().instabuild) {
            return result;
        }
        ItemStack remaining = eaten.copy();
        remaining.shrink(1);
        if (!entity.level().isClientSide) {
            ItemStack bowl = new ItemStack(Items.BOWL);
            if (entity instanceof Player player) {
                if (!player.getInventory().add(bowl)) {
                    player.drop(bowl, false);
                }
            } else {
                entity.spawnAtLocation(bowl);
            }
        }
        return remaining;
    }
}
