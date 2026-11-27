package com.wildscapes.worldgen;

import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public final class WildscapesTreeGrowers {
    private WildscapesTreeGrowers() {}

    public static final AbstractTreeGrower CYPRESS = new AbstractTreeGrower() {
        @Override
        protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource random, boolean bees) {
            return WildscapesConfiguredFeatures.CYPRESS;
        }
    };
}
