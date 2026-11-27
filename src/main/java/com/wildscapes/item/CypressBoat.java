package com.wildscapes.item;

import com.wildscapes.Wildscapes;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public final class CypressBoat {
    private CypressBoat() {}

    public static final EntityType<Plain> BOAT = register("cypress_boat", Plain::new);
    public static final EntityType<WithChest> CHEST_BOAT = register("cypress_chest_boat", WithChest::new);

    private static <T extends Boat> EntityType<T> register(String name, EntityType.EntityFactory<T> factory) {
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, Wildscapes.id(name), EntityType.Builder.of(factory, MobCategory.MISC)
                .sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10).build(Wildscapes.id(name).toString()));
    }

    public static void register() {}

    public static class Plain extends Boat {
        public Plain(EntityType<? extends Boat> type, Level level) { super(type, level); }
        @Override public Item getDropItem() { return WildscapesItems.CYPRESS_BOAT; }
    }

    public static class WithChest extends ChestBoat {
        public WithChest(EntityType<? extends Boat> type, Level level) { super(type, level); }
        @Override public Item getDropItem() { return WildscapesItems.CYPRESS_CHEST_BOAT; }
    }
}
