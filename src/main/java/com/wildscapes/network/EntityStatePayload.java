package com.wildscapes.network;

import java.util.function.Supplier;

import com.wildscapes.client.ClientPackets;
import com.wildscapes.entity.SlimeMerging;
import com.wildscapes.entity.SoulHarvest;
import com.wildscapes.entity.SwampVariants;
import com.wildscapes.entity.WitchRitual;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public record EntityStatePayload(int entityId, byte swampBorn, boolean empowered, boolean chanting, float slimeScale) {
    public EntityStatePayload(Entity entity) {
        this(entity.getId(), entity.getPersistentData().getByte(SwampVariants.SWAMP_BORN),
                SoulHarvest.isEmpowered(entity), WitchRitual.isChanting(entity), SlimeMerging.scale(entity));
    }

    static void encode(EntityStatePayload packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.entityId());
        buf.writeByte(packet.swampBorn());
        buf.writeBoolean(packet.empowered());
        buf.writeBoolean(packet.chanting());
        buf.writeFloat(packet.slimeScale());
    }

    static EntityStatePayload decode(FriendlyByteBuf buf) {
        return new EntityStatePayload(buf.readVarInt(), buf.readByte(), buf.readBoolean(), buf.readBoolean(), buf.readFloat());
    }

    static void handle(EntityStatePayload packet, Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPackets.entityState(packet));
    }
}
