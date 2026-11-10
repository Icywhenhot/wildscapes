package com.wildscapes.client;

import com.wildscapes.entity.SlimeMerging;
import com.wildscapes.entity.SoulHarvest;
import com.wildscapes.entity.SwampVariants;
import com.wildscapes.entity.WitchRitual;
import com.wildscapes.network.EntityStatePayload;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public final class ClientPackets {
    private ClientPackets() {}

    public static void entityState(EntityStatePayload packet) {
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        Entity entity = level.getEntity(packet.entityId());
        if (entity == null) {
            return;
        }
        float scale = SlimeMerging.scale(entity);
        var data = entity.getPersistentData();
        data.putByte(SwampVariants.SWAMP_BORN, packet.swampBorn());
        data.putBoolean(SoulHarvest.EMPOWERED, packet.empowered());
        data.putBoolean(WitchRitual.CHANTING, packet.chanting());
        data.putFloat(SlimeMerging.SCALE, packet.slimeScale());
        if (scale != packet.slimeScale()) {
            entity.refreshDimensions();
        }
    }
}
