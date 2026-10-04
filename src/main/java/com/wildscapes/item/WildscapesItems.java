package com.wildscapes.item;

import com.wildscapes.Wildscapes;
import com.wildscapes.entity.WildscapesEntities;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class WildscapesItems {
    private WildscapesItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Wildscapes.MODID);

    public static final DeferredItem<Item> ABOMINATION_SPAWN_EGG = ITEMS.register("abomination_spawn_egg",
            () -> new DeferredSpawnEggItem(WildscapesEntities.ABOMINATION, 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredItem<Item> ILLUSIONER_SPAWN_EGG = ITEMS.register("illusioner_spawn_egg",
            () -> new DeferredSpawnEggItem(() -> EntityType.ILLUSIONER, 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredItem<Item> FROG_LEGS = ITEMS.register("frog_legs",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> LADLE = ITEMS.register("ladle",
            () -> new Item(new Item.Properties().durability(10)));

    public static final DeferredItem<Item> ABOMINATION_TONGUE = ITEMS.register("abomination_tongue",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<MirelashItem> MIRELASH = ITEMS.register("mirelash",
            () -> new MirelashItem(new Item.Properties().durability(64)
                    .attributes(SwordItem.createAttributes(Tiers.IRON, 3, -2.4F))));

    public static final DeferredItem<Item> ILLUSIONIST_NECKLACE = ITEMS.register("illusionist_necklace",
            () -> new Item(new Item.Properties().durability(432)));

    public static final DeferredItem<IncendiaryBottleItem> INCENDIARY_BOTTLE = ITEMS.register("incendiary_bottle",
            () -> new IncendiaryBottleItem(new Item.Properties().stacksTo(16)));

    public static final DeferredItem<MagicSoupItem> MAGIC_SOUP = ITEMS.register("magic_soup",
            () -> new MagicSoupItem(new Item.Properties()));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
