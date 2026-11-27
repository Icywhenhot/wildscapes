package com.wildscapes.network;

import com.wildscapes.entity.WildscapesEntityData;


import com.wildscapes.entity.SlimeMerging;
import com.wildscapes.entity.SoulHarvest;
import com.wildscapes.entity.SwampVariants;
import com.wildscapes.entity.WitchRitual;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;

public record EntityStatePayload(int entityId, byte swampBorn, boolean empowered, boolean chanting, float slimeScale) {
    public EntityStatePayload(Entity entity) {
        this(entity.getId(), WildscapesEntityData.of(entity).getByte(SwampVariants.SWAMP_BORN),
                SoulHarvest.isEmpowered(entity), WitchRitual.isChanting(entity), SlimeMerging.scale(entity));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeByte(swampBorn);
        buf.writeBoolean(empowered);
        buf.writeBoolean(chanting);
        buf.writeFloat(slimeScale);
    }

    public static EntityStatePayload decode(FriendlyByteBuf buf) {
        return new EntityStatePayload(buf.readVarInt(), buf.readByte(), buf.readBoolean(), buf.readBoolean(), buf.readFloat());
    }

}
