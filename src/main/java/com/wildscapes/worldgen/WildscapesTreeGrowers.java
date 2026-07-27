package com.wildscapes.worldgen;

import java.util.Optional;

import net.minecraft.world.level.block.grower.TreeGrower;

public final class WildscapesTreeGrowers {
    private WildscapesTreeGrowers() {}

    public static final TreeGrower CYPRESS = new TreeGrower(
            "wildscapes:cypress",
            0.0F,
            Optional.empty(),
            Optional.empty(),
            Optional.of(WildscapesConfiguredFeatures.CYPRESS),
            Optional.empty(),
            Optional.empty(),
            Optional.empty());
}
