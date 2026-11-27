package com.wildscapes.worldgen;

import com.mojang.serialization.MapCodec;
import com.wildscapes.Wildscapes;

import net.minecraft.core.Registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;

public final class WildscapesFeatures {
    private WildscapesFeatures() {}

    public static final MapCodec<CypressTreeFeature> CYPRESS_TREE =
            Registry.register(BuiltInRegistries.FEATURE_TYPE, Wildscapes.id("cypress_tree"), CypressTreeFeature.CODEC);

    public static final MapCodec<TemplateTreeFeature> TEMPLATE_TREE =
            Registry.register(BuiltInRegistries.FEATURE_TYPE, Wildscapes.id("template_tree"), TemplateTreeFeature.CODEC);

    public static final MapCodec<WaterloggedDoublePlantFeature> WATERLOGGED_DOUBLE_PLANT =
            Registry.register(BuiltInRegistries.FEATURE_TYPE, Wildscapes.id("waterlogged_double_plant"), WaterloggedDoublePlantFeature.CODEC);

    public static final MapCodec<ShelfMushroomFeature> SHELF_MUSHROOM =
            Registry.register(BuiltInRegistries.FEATURE_TYPE, Wildscapes.id("shelf_mushroom"), ShelfMushroomFeature.CODEC);

    public static void register() {}
}
