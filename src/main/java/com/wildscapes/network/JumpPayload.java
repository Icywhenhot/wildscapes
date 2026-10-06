package com.wildscapes.network;

import com.wildscapes.Wildscapes;
import com.wildscapes.entity.JumpEnchantments;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record JumpPayload(boolean powered) implements CustomPacketPayload {
    public static final Type<JumpPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "jump"));
    public static final StreamCodec<RegistryFriendlyByteBuf, JumpPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, JumpPayload::powered, JumpPayload::new);

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC, JumpPayload::handle);
    }

    private static void handle(JumpPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> JumpEnchantments.jump((ServerPlayer) context.player(), payload.powered()));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
