package com.wildscapes.client;

import com.wildscapes.network.WildscapesNetwork;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import com.wildscapes.entity.WildscapesEntityData;

import com.wildscapes.entity.SlimeMerging;
import com.wildscapes.entity.SoulHarvest;
import com.wildscapes.entity.SwampVariants;
import com.wildscapes.entity.WitchRitual;
import com.wildscapes.network.EntityStatePayload;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public final class ClientPackets {
    private ClientPackets() {}

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(WildscapesNetwork.ENTITY_STATE, (client, handler, buf, sender) -> {
            EntityStatePayload packet = EntityStatePayload.decode(buf);
            client.execute(() -> entityState(packet));
        });
    }

    private static void entityState(EntityStatePayload packet) {
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        Entity entity = level.getEntity(packet.entityId());
        if (entity == null) {
            return;
        }
        float scale = SlimeMerging.scale(entity);
        var data = WildscapesEntityData.of(entity);
        data.putByte(SwampVariants.SWAMP_BORN, packet.swampBorn());
        data.putBoolean(SoulHarvest.EMPOWERED, packet.empowered());
        data.putBoolean(WitchRitual.CHANTING, packet.chanting());
        data.putFloat(SlimeMerging.SCALE, packet.slimeScale());
        if (scale != packet.slimeScale()) {
            entity.refreshDimensions();
        }
    }
}
