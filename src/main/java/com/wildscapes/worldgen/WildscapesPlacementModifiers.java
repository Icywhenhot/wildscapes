package com.wildscapes.worldgen;

import com.mojang.serialization.MapCodec;
import com.wildscapes.Wildscapes;

import net.minecraft.core.Registry;

import net.minecraft.core.registries.BuiltInRegistries;

public final class WildscapesPlacementModifiers {
    private WildscapesPlacementModifiers() {}

    public static final MapCodec<ShoreDensityFilter> SHORE_DENSITY = Registry.register(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, Wildscapes.id("shore_density"), ShoreDensityFilter.CODEC);

    public static void register() {}
}
