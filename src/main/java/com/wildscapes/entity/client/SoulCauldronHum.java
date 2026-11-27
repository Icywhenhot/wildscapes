package com.wildscapes.entity.client;

import com.wildscapes.block.entity.CauldronOfSoulsBlockEntity;
import com.wildscapes.sound.WildscapesSounds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;

public final class SoulCauldronHum extends AbstractTickableSoundInstance {
    private final CauldronOfSoulsBlockEntity cauldron;

    private SoulCauldronHum(CauldronOfSoulsBlockEntity cauldron) {
        super(WildscapesSounds.CAULDRON_OF_SOULS_HUM.get(), SoundSource.BLOCKS, cauldron.getLevel().getRandom());
        this.cauldron = cauldron;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.8F;
        this.x = cauldron.getBlockPos().getX() + 0.5;
        this.y = cauldron.getBlockPos().getY() + 0.5;
        this.z = cauldron.getBlockPos().getZ() + 0.5;
    }

    public static void start(CauldronOfSoulsBlockEntity cauldron) {
        Minecraft.getInstance().getSoundManager().play(new SoulCauldronHum(cauldron));
    }

    @Override
    public void tick() {
        if (cauldron.isRemoved()) {
            stop();
            cauldron.stopHumming();
        }
    }
}
