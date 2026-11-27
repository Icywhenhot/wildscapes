package com.wildscapes.item;

import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import java.util.List;


import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class VanillaTweaks {
    private VanillaTweaks() {}

    private static final float FAST_EAT_SECONDS = 0.4F;

    private static final List<Item> STEWS =
            List.of(Items.MUSHROOM_STEW, Items.RABBIT_STEW, Items.BEETROOT_SOUP, Items.SUSPICIOUS_STEW);

    public static void modifyComponents(ModifyDefaultComponentsEvent event) {
        event.modify(Items.POTION, (builder, lookup, id) -> builder.set(DataComponents.MAX_STACK_SIZE, 8));

        for (Item stew : STEWS) {
            event.modify(stew, (builder, lookup, id) -> {
                builder.set(DataComponents.MAX_STACK_SIZE, 8);
                Consumable food = builder.get(DataComponents.CONSUMABLE);
                if (food != null) {
                    builder.set(DataComponents.CONSUMABLE, new Consumable(FAST_EAT_SECONDS, food.animation(),
                            food.sound(), food.hasConsumeParticles(), food.onConsumeEffects()));
                }
            });
        }
    }
}
