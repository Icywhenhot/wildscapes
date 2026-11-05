package com.wildscapes.worldgen;

import com.wildscapes.Wildscapes;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public final class WildscapesConfiguredFeatures {
    private WildscapesConfiguredFeatures() {}

    public static final ResourceKey<ConfiguredFeature<?, ?>> CYPRESS = ResourceKey.create(
            Registries.CONFIGURED_FEATURE,
            Identifier.fromNamespaceAndPath(Wildscapes.MODID, "cypress"));
}
