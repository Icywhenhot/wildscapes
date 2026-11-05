package com.wildscapes.network;

import com.wildscapes.Wildscapes;
import com.wildscapes.item.MirelashItem;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public record MirelashAttackPayload(int targetId) implements CustomPacketPayload {
    public static final Type<MirelashAttackPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(Wildscapes.MODID, "mirelash_attack"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MirelashAttackPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MirelashAttackPayload::targetId, MirelashAttackPayload::new);

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(TYPE, STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(TYPE, MirelashAttackPayload::handle);
    }

    private static void handle(MirelashAttackPayload payload, ServerPlayNetworking.Context context) {
        Entity target = context.player().level().getEntity(payload.targetId());
        if (target instanceof LivingEntity living) {
            MirelashItem.launchAttack(context.player(), living);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
