package com.wildscapes.worldgen;

import com.wildscapes.Wildscapes;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public final class WildscapesConfiguredFeatures {
    private WildscapesConfiguredFeatures() {}

    public static final ResourceKey<ConfiguredFeature<?, ?>> CYPRESS = ResourceKey.create(
            Registries.CONFIGURED_FEATURE,
            ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "cypress"));
}
