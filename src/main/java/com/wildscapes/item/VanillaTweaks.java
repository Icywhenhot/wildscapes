package com.wildscapes.item;

import java.util.List;

import com.wildscapes.Wildscapes;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

/**
 * Global tweaks to vanilla consumables to match the cauldron feature: drinkable potions stack in
 * eights (splash, lingering and tipped arrows are deliberately left alone), and every stew stacks
 * the same way and is eaten almost instantly — like the mod's own magic soups.
 */
@EventBusSubscriber(modid = Wildscapes.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class VanillaTweaks {
    private VanillaTweaks() {}

    /** ~0.4 s, matching {@link MagicSoupItem}. */
    private static final float FAST_EAT_SECONDS = 0.4F;

    private static final List<Item> STEWS =
            List.of(Items.MUSHROOM_STEW, Items.RABBIT_STEW, Items.BEETROOT_SOUP, Items.SUSPICIOUS_STEW);

    @SubscribeEvent
    static void modifyComponents(ModifyDefaultComponentsEvent event) {
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
