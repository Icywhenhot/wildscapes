package com.wildscapes.item;

import com.wildscapes.Wildscapes;
import com.wildscapes.entity.WildscapesEntities;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class WildscapesItems {
    private WildscapesItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Wildscapes.MODID);

    // Colors are white so the spawn-egg tint (applied to the generated model's tintindex-0
    // layer) is a no-op — the custom abomination_spawn_egg sprite renders at its true colors
    // rather than the vanilla two-tone egg.
    public static final DeferredItem<Item> ABOMINATION_SPAWN_EGG = ITEMS.register("abomination_spawn_egg",
            () -> new DeferredSpawnEggItem(WildscapesEntities.ABOMINATION, 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    // Dropped by regular vanilla frogs on death (see Wildscapes#onLivingDrops).
    public static final DeferredItem<Item> FROG_LEGS = ITEMS.register("frog_legs",
            () -> new Item(new Item.Properties()));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
