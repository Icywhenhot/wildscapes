package com.wildscapes.entity.client;

import com.wildscapes.block.WildscapesCauldronBlock;
import com.wildscapes.block.entity.CauldronBlockEntity;
import com.wildscapes.sound.WildscapesSounds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

public final class CauldronBoilSound extends AbstractTickableSoundInstance {
    private final CauldronBlockEntity cauldron;
    private final Level level;
    private final BlockPos pos;

    private CauldronBoilSound(CauldronBlockEntity cauldron) {
        super(WildscapesSounds.CAULDRON_BOIL_START.get(), SoundSource.BLOCKS, cauldron.getLevel().getRandom());
        this.cauldron = cauldron;
        this.level = cauldron.getLevel();
        this.pos = cauldron.getBlockPos();
        this.looping = true;
        this.delay = 0;
        this.volume = 0.6F;
        this.x = pos.getX() + 0.5;
        this.y = pos.getY() + 0.5;
        this.z = pos.getZ() + 0.5;
    }

    public static void start(CauldronBlockEntity cauldron) {
        cauldron.setClientBoilSoundActive(true);
        Minecraft.getInstance().getSoundManager().play(new CauldronBoilSound(cauldron));
    }

    @Override
    public void tick() {
        if (cauldron.isRemoved()
                || !(level.getBlockState(pos).getBlock() instanceof WildscapesCauldronBlock)
                || !level.getBlockState(pos).getValue(WildscapesCauldronBlock.BOILING)
                || cauldron.getFillLevel() <= 0) {
            stop();
            cauldron.setClientBoilSoundActive(false);
        }
    }
}
