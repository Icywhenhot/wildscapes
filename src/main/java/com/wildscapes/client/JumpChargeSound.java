package com.wildscapes.client;

import com.wildscapes.entity.JumpEnchantments;
import com.wildscapes.sound.WildscapesSounds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;

public final class JumpChargeSound extends AbstractTickableSoundInstance {
    private final LocalPlayer player;

    public JumpChargeSound(LocalPlayer player) {
        super(WildscapesSounds.EMPOWERED_JUMP_CHARGE.get(), SoundSource.PLAYERS, player.getRandom());
        this.player = player;
        looping = false;
        delay = 0;
        volume = 0.8F;
        x = player.getX();
        y = player.getY();
        z = player.getZ();
    }

    @Override
    public void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != player || mc.screen != null || !player.input.shiftKeyDown
                || !JumpEnchantments.canCharge(player)) {
            stop();
            return;
        }
        x = player.getX();
        y = player.getY();
        z = player.getZ();
    }
}
