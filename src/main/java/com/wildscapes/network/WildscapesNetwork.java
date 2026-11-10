package com.wildscapes.network;

import com.wildscapes.Wildscapes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

@EventBusSubscriber(modid = Wildscapes.MODID)
public final class WildscapesNetwork {
    private static final String VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "main"),
            () -> VERSION, VERSION::equals, VERSION::equals);

    private WildscapesNetwork() {}

    public static void register() {
        CHANNEL.messageBuilder(JumpPayload.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder((packet, buf) -> {})
                .decoder(buf -> JumpPayload.INSTANCE)
                .consumerMainThread(JumpPayload::handle).add();
        CHANNEL.messageBuilder(MirelashAttackPayload.class, 1, NetworkDirection.PLAY_TO_SERVER)
                .encoder((packet, buf) -> buf.writeVarInt(packet.targetId()))
                .decoder(buf -> new MirelashAttackPayload(buf.readVarInt()))
                .consumerMainThread(MirelashAttackPayload::handle).add();
        CHANNEL.messageBuilder(EntityStatePayload.class, 2, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(EntityStatePayload::encode)
                .decoder(EntityStatePayload::decode)
                .consumerMainThread(EntityStatePayload::handle).add();
    }

    public static void sync(Entity entity) {
        if (!entity.level().isClientSide) {
            CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), new EntityStatePayload(entity));
        }
    }

    @SubscribeEvent
    static void startTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new EntityStatePayload(event.getTarget()));
        }
    }
}
