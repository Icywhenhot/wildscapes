package com.wildscapes.network;

import com.wildscapes.Wildscapes;
import com.wildscapes.entity.JumpEnchantments;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record JumpPayload() implements CustomPacketPayload {
    public static final Type<JumpPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "jump"));
    public static final JumpPayload INSTANCE = new JumpPayload();
    public static final StreamCodec<RegistryFriendlyByteBuf, JumpPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("2").playToServer(TYPE, STREAM_CODEC, JumpPayload::handle);
    }

    private static void handle(JumpPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> JumpEnchantments.jump((ServerPlayer) context.player()));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
