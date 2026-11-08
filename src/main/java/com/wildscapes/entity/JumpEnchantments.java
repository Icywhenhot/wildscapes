package com.wildscapes.entity;

import com.wildscapes.Wildscapes;
import com.wildscapes.item.MirelashItem;
import com.wildscapes.particle.WildscapesParticles;
import com.wildscapes.sound.WildscapesSounds;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;

public final class JumpEnchantments {
    public static final ResourceKey<Enchantment> MULTI_JUMP = ResourceKey.create(Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath(Wildscapes.MODID, "multi_jump"));
    public static final ResourceKey<Enchantment> EMPOWERED_JUMP = ResourceKey.create(Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath(Wildscapes.MODID, "empowered_jump"));
    private static final String USED = "wildscapes:air_jumps";
    public static final int FULL_CHARGE = 181;
    private static final String CHARGE = "wildscapes:jump_charge";

    private JumpEnchantments() {}

    public static boolean canJump(Player player) {
        return player.isAlive() && !player.isSpectator() && !player.getAbilities().flying
                && !player.isPassenger() && !player.isSleeping() && !player.isFallFlying()
                && !player.isInWater() && !player.isInLava() && !player.onClimbable();
    }

    public static boolean canCharge(Player player) {
        return canJump(player) && player.onGround()
                && MirelashItem.enchantmentLevel(player.level(), player.getItemBySlot(EquipmentSlot.LEGS), EMPOWERED_JUMP) > 0;
    }

    public static void onTick(Player ticking) {
        if (!(ticking instanceof ServerPlayer player)) {
            return;
        }
        var data = ((WildscapesEntityData) player).wildscapes$data();
        if (player.onGround() || !canJump(player)) {
            data.remove(USED);
        }
        int charge = data.getIntOr(CHARGE, 0);
        if (!player.isShiftKeyDown() || !canCharge(player)) {
            data.remove(CHARGE);
            if (charge >= 60 && canCharge(player)) {
                launch(player, charge);
            }
            return;
        }
        if (charge < FULL_CHARGE) {
            data.putInt(CHARGE, ++charge);
            if (charge == FULL_CHARGE) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        WildscapesSounds.EMPOWERED_JUMP_READY, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    private static void launch(ServerPlayer player, int charge) {
        double boost = Mth.clamp((charge - 60) / (double) (FULL_CHARGE - 60), 0.0, 1.0);
        player.setDeltaMovement(0.0, 0.9 + boost * 1.1, 0.0);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                WildscapesSounds.EMPOWERED_JUMP_LAUNCH, SoundSource.PLAYERS, 1.0F, 1.0F);
        push(player);
    }

    public static boolean jump(ServerPlayer player) {
        if (!canJump(player)) {
            return false;
        }
        var data = ((WildscapesEntityData) player).wildscapes$data();
        int level = Math.min(2, MirelashItem.enchantmentLevel(player.level(),
                player.getItemBySlot(EquipmentSlot.FEET), MULTI_JUMP));
        int used = data.getIntOr(USED, 0);
        if (player.onGround() || used >= level) {
            return false;
        }
        player.jumpFromGround();
        data.putInt(USED, used + 1);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                WildscapesSounds.MULTI_JUMP, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.level().sendParticles(WildscapesParticles.MULTI_JUMP, player.getX(),
                player.getY() + 0.02, player.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        push(player);
        return true;
    }

    private static void push(ServerPlayer player) {
        player.fallDistance = 0.0F;
        player.setOnGround(false);
        player.needsSync = true;
        player.connection.send(new ClientboundSetEntityMotionPacket(player));
    }
}
