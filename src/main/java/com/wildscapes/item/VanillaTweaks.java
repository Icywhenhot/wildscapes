package com.wildscapes.item;

import java.util.List;

import com.wildscapes.Wildscapes;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class VanillaTweaks {
    private VanillaTweaks() {}

    private static final float FAST_EAT_SECONDS = 0.4F;

    private static final List<Item> STEWS =
            List.of(Items.MUSHROOM_STEW, Items.RABBIT_STEW, Items.BEETROOT_SOUP, Items.SUSPICIOUS_STEW);

    public static void register() {
        net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents.MODIFY.register(VanillaTweaks::modifyComponents);
    }

    private static void modifyComponents(net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents.ModifyContext event) {
        event.modify(Items.POTION, builder -> builder.set(DataComponents.MAX_STACK_SIZE, 8));

        for (Item stew : STEWS) {
            FoodProperties food = stew.components().get(DataComponents.FOOD);
            event.modify(stew, builder -> {
                builder.set(DataComponents.MAX_STACK_SIZE, 8);
                if (food != null) {
                    builder.set(DataComponents.FOOD, new FoodProperties(food.nutrition(), food.saturation(),
                            food.canAlwaysEat(), FAST_EAT_SECONDS, food.usingConvertsTo(), food.effects()));
                }
            });
        }
    }
}
