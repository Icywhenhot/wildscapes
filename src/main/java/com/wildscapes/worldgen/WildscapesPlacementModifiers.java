package com.wildscapes.worldgen;

import com.wildscapes.Wildscapes;

import net.minecraft.core.Registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public final class WildscapesPlacementModifiers {
    private WildscapesPlacementModifiers() {}

    public static final PlacementModifierType<ShoreDensityFilter> SHORE_DENSITY = Registry.register(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, Wildscapes.id("shore_density"), () -> ShoreDensityFilter.CODEC);

    public static void register() {}
}
