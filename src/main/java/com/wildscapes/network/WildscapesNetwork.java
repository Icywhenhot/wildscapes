package com.wildscapes.network;

import com.wildscapes.Wildscapes;

import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public final class WildscapesNetwork {
    public static final ResourceLocation JUMP = new ResourceLocation(Wildscapes.MODID, "jump");
    public static final ResourceLocation MIRELASH_ATTACK = new ResourceLocation(Wildscapes.MODID, "mirelash_attack");
    public static final ResourceLocation ENTITY_STATE = new ResourceLocation(Wildscapes.MODID, "entity_state");

    private WildscapesNetwork() {}

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(JUMP, (server, player, handler, buf, sender) ->
                server.execute(() -> JumpPayload.handle(player)));
        ServerPlayNetworking.registerGlobalReceiver(MIRELASH_ATTACK, (server, player, handler, buf, sender) -> {
            MirelashAttackPayload packet = new MirelashAttackPayload(buf.readVarInt());
            server.execute(() -> packet.handle(player));
        });
        EntityTrackingEvents.START_TRACKING.register((entity, player) -> send(player, new EntityStatePayload(entity)));
    }

    public static void sync(Entity entity) {
        if (entity.level().isClientSide) {
            return;
        }
        EntityStatePayload packet = new EntityStatePayload(entity);
        for (ServerPlayer player : PlayerLookup.tracking(entity)) {
            send(player, packet);
        }
        if (entity instanceof ServerPlayer self) {
            send(self, packet);
        }
    }

    private static void send(ServerPlayer player, EntityStatePayload packet) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        packet.encode(buf);
        ServerPlayNetworking.send(player, ENTITY_STATE, buf);
    }
}
