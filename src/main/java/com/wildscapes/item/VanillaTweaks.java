package com.wildscapes.item;

import java.util.List;


import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class VanillaTweaks {
    private VanillaTweaks() {}

    private static final float FAST_EAT_SECONDS = 0.4F;

    private static final List<Item> STEWS =
            List.of(Items.MUSHROOM_STEW, Items.RABBIT_STEW, Items.BEETROOT_SOUP, Items.SUSPICIOUS_STEW);

    public static void register() {
        DefaultItemComponentEvents.MODIFY.register(VanillaTweaks::modifyComponents);
    }

    private static void modifyComponents(DefaultItemComponentEvents.ModifyContext event) {
        event.modify(Items.POTION, builder -> builder.set(DataComponents.MAX_STACK_SIZE, 8));

        for (Item stew : STEWS) {
            Consumable food = stew.components().get(DataComponents.CONSUMABLE);
            event.modify(stew, builder -> {
                builder.set(DataComponents.MAX_STACK_SIZE, 8);
                if (food != null) {
                    builder.set(DataComponents.CONSUMABLE, new Consumable(FAST_EAT_SECONDS, food.animation(),
                            food.sound(), food.hasConsumeParticles(), food.onConsumeEffects()));
                }
            });
        }
    }
}
