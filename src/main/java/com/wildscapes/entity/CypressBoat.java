package com.wildscapes.entity;

import com.wildscapes.item.WildscapesItems;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class CypressBoat extends Boat {
    public CypressBoat(EntityType<? extends Boat> type, Level level) {
        super(type, level);
    }

    @Override
    public Item getDropItem() {
        return WildscapesItems.CYPRESS_BOAT.get();
    }

}
