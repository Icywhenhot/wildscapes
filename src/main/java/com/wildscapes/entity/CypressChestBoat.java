package com.wildscapes.entity;

import com.wildscapes.item.WildscapesItems;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class CypressChestBoat extends ChestBoat {
    public CypressChestBoat(EntityType<? extends Boat> type, Level level) {
        super(type, level);
    }

    @Override
    public Item getDropItem() {
        return WildscapesItems.CYPRESS_CHEST_BOAT.get();
    }

}
