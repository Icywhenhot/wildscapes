package com.wildscapes.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public interface WildscapesEntityData {
    CompoundTag wildscapes$data();

    boolean wildscapes$fresh();

    void wildscapes$markFresh();

    static CompoundTag of(Entity entity) {
        return ((WildscapesEntityData) entity).wildscapes$data();
    }

    static boolean loadedFromDisk(Entity entity) {
        return !((WildscapesEntityData) entity).wildscapes$fresh();
    }
}
