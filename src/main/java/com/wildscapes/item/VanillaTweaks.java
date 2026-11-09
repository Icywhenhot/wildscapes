package com.wildscapes.item;

import java.util.List;

import com.wildscapes.Wildscapes;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid = Wildscapes.MODID)
public final class VanillaTweaks {
    private VanillaTweaks() {}

    private static final List<Item> STEWS =
            List.of(Items.MUSHROOM_STEW, Items.RABBIT_STEW, Items.BEETROOT_SOUP, Items.SUSPICIOUS_STEW);

    public static void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Items.POTION.maxStackSize = 8;
            STEWS.forEach(stew -> stew.maxStackSize = 8);
        });
    }

    public static boolean isFastFood(ItemStack stack) {
        return STEWS.contains(stack.getItem()) || stack.getItem() instanceof MagicSoupItem;
    }

    @SubscribeEvent
    static void finishEating(LivingEntityUseItemEvent.Finish event) {
        if (!isFastFood(event.getItem()) || event.getItem().getCount() <= 1) {
            return;
        }
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player player && player.getAbilities().instabuild) {
            return;
        }
        ItemStack remaining = event.getItem().copy();
        remaining.shrink(1);
        event.setResultStack(remaining);
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
    }
}
