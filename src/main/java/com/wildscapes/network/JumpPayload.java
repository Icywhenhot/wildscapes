package com.wildscapes.network;

import com.wildscapes.Wildscapes;
import com.wildscapes.entity.JumpEnchantments;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public record JumpPayload() implements CustomPacketPayload {
    public static final Type<JumpPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(Wildscapes.MODID, "jump"));
    public static final JumpPayload INSTANCE = new JumpPayload();
    public static final StreamCodec<RegistryFriendlyByteBuf, JumpPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(TYPE, STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(TYPE, JumpPayload::handle);
    }

    private static void handle(JumpPayload payload, ServerPlayNetworking.Context context) {
        JumpEnchantments.jump(context.player());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
