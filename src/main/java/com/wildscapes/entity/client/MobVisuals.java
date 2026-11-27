package com.wildscapes.entity.client;

import com.wildscapes.Wildscapes;

import net.minecraft.util.context.ContextKey;

public final class MobVisuals {
    public static final ContextKey<Boolean> EMPOWERED = new ContextKey<>(Wildscapes.id("empowered"));
    public static final ContextKey<Boolean> SWAMP = new ContextKey<>(Wildscapes.id("swamp"));
    public static final ContextKey<Boolean> MIRAGE = new ContextKey<>(Wildscapes.id("mirage"));
    public static final ContextKey<Boolean> BUBBLE = new ContextKey<>(Wildscapes.id("bubble"));
    public static final ContextKey<Boolean> PHASED = new ContextKey<>(Wildscapes.id("phased"));
    public static final ContextKey<Boolean> LOCAL = new ContextKey<>(Wildscapes.id("local"));
    public static final ContextKey<Boolean> CHANTING = new ContextKey<>(Wildscapes.id("chanting"));
    public static final ContextKey<Float> MERGE_ALPHA = new ContextKey<>(Wildscapes.id("merge_alpha"));

    private MobVisuals() {}
}
