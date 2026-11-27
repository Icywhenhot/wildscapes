package com.wildscapes.worldgen;

import net.minecraft.world.level.levelgen.feature.Feature;
import com.wildscapes.Wildscapes;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;

public final class WildscapesConfiguredFeatures {
    private WildscapesConfiguredFeatures() {}

    public static final ResourceKey<Feature> CYPRESS = ResourceKey.create(
            Registries.FEATURE,
            Identifier.fromNamespaceAndPath(Wildscapes.MODID, "cypress"));
}
