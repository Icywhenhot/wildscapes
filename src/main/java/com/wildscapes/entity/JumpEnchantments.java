package com.wildscapes.entity;

import com.wildscapes.Wildscapes;
import com.wildscapes.item.MirelashItem;
import com.wildscapes.particle.WildscapesParticles;
import com.wildscapes.sound.WildscapesSounds;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Wildscapes.MODID)
public final class JumpEnchantments {
    public static final ResourceKey<Enchantment> MULTI_JUMP = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "multi_jump"));
    public static final ResourceKey<Enchantment> EMPOWERED_JUMP = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "empowered_jump"));
    private static final String USED = "wildscapes:air_jumps";
    private static final String CHARGE = "wildscapes:jump_charge";

    private JumpEnchantments() {}

    public static boolean canJump(Player player) {
        return player.isAlive() && !player.isSpectator() && !player.getAbilities().flying
                && !player.isPassenger() && !player.isSleeping() && !player.isFallFlying()
                && !player.isInWaterOrBubble() && !player.isInLava() && !player.onClimbable();
    }

    public static boolean canCharge(Player player) {
        return canJump(player) && player.onGround()
                && MirelashItem.enchantmentLevel(player.level(), player.getItemBySlot(EquipmentSlot.LEGS), EMPOWERED_JUMP) > 0;
    }

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        var data = player.getPersistentData();
        if (player.onGround() || !canJump(player)) {
            data.remove(USED);
        }
        if (!player.isShiftKeyDown() || !canCharge(player)) {
            data.remove(CHARGE);
            return;
        }
        int charge = data.getInt(CHARGE);
        if (charge < 200) {
            data.putInt(CHARGE, ++charge);
            if (charge == 200) {
                player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                        WildscapesSounds.EMPOWERED_JUMP_READY.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    public static boolean jump(ServerPlayer player, boolean powered) {
        if (!canJump(player)) {
            return false;
        }
        var data = player.getPersistentData();
        if (powered) {
            int charge = data.getInt(CHARGE);
            if (!player.isShiftKeyDown() || !canCharge(player) || charge < 60) {
                return false;
            }
            double boost = Mth.clamp((charge - 60) / 140.0, 0.0, 1.0);
            player.setDeltaMovement(0.0, 0.9 + boost * 1.1, 0.0);
            data.remove(CHARGE);
            player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                    WildscapesSounds.EMPOWERED_JUMP_LAUNCH.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        } else {
            int level = Math.min(2, MirelashItem.enchantmentLevel(player.level(),
                    player.getItemBySlot(EquipmentSlot.FEET), MULTI_JUMP));
            int used = data.getInt(USED);
            if (player.onGround() || used >= level) {
                return false;
            }
            player.jumpFromGround();
            data.putInt(USED, used + 1);
            player.serverLevel().sendParticles(WildscapesParticles.MULTI_JUMP.get(), player.getX(),
                    player.getY() + 0.02, player.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        }
        player.fallDistance = 0.0F;
        player.setOnGround(false);
        player.hasImpulse = true;
        player.connection.send(new ClientboundSetEntityMotionPacket(player));
        return true;
    }
}
