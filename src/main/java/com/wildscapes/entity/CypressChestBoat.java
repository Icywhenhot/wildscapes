package com.wildscapes.entity;

import com.wildscapes.item.WildscapesItems;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class CypressChestBoat extends ChestBoat {
    public CypressChestBoat(EntityType<? extends Boat> type, Level level) {
        super(type, level);
    }

    @Override
    public Item getDropItem() {
        return WildscapesItems.CYPRESS_CHEST_BOAT.get();
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
