package com.wildscapes.worldgen;


import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.grower.TreeGrower;

public final class WildscapesTreeGrowers {
    private WildscapesTreeGrowers() {}

    public static final TreeGrower CYPRESS = new TreeGrower(
            "wildscapes:cypress",
            WeightedList.of(),
            WeightedList.of(),
            WeightedList.of(),
            WildscapesConfiguredFeatures.CYPRESS);
}
