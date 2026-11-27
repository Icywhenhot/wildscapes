package com.wildscapes.block.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;

public interface PotData {
    CompoundTag wildscapes$data();

    static CompoundTag of(DecoratedPotBlockEntity pot) {
        return ((PotData) pot).wildscapes$data();
    }
}
