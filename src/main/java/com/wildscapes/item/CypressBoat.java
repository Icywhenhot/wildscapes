package com.wildscapes.item;

import com.wildscapes.Wildscapes;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;

public final class CypressBoat {
    private CypressBoat() {}

    public static final EntityType<Boat> BOAT = register("cypress_boat",
            (type, level) -> new Boat(type, level, () -> WildscapesItems.CYPRESS_BOAT));

    public static final EntityType<ChestBoat> CHEST_BOAT = register("cypress_chest_boat",
            (type, level) -> new ChestBoat(type, level, () -> WildscapesItems.CYPRESS_CHEST_BOAT));

    private static <T extends AbstractBoat> EntityType<T> register(String name, EntityType.EntityFactory<T> factory) {
        var key = ResourceKey.create(Registries.ENTITY_TYPE, Wildscapes.id(name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, EntityType.Builder.of(factory, MobCategory.MISC)
                .noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10).build(key));
    }
}
