package com.wildscapes.worldgen;

import com.wildscapes.Wildscapes;

import net.minecraft.core.Registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;

public final class WildscapesFeatures {
    private WildscapesFeatures() {}

    public static final CypressTreeFeature CYPRESS_TREE =
            Registry.register(BuiltInRegistries.FEATURE, Wildscapes.id("cypress_tree"), new CypressTreeFeature(NoneFeatureConfiguration.CODEC));

    public static final TemplateTreeFeature TEMPLATE_TREE =
            Registry.register(BuiltInRegistries.FEATURE, Wildscapes.id("template_tree"), new TemplateTreeFeature(TemplateTreeConfiguration.CODEC));

    public static final WaterloggedDoublePlantFeature WATERLOGGED_DOUBLE_PLANT =
            Registry.register(BuiltInRegistries.FEATURE, Wildscapes.id("waterlogged_double_plant"), new WaterloggedDoublePlantFeature(SimpleBlockConfiguration.CODEC));

    public static final ShelfMushroomFeature SHELF_MUSHROOM =
            Registry.register(BuiltInRegistries.FEATURE, Wildscapes.id("shelf_mushroom"), new ShelfMushroomFeature(SimpleBlockConfiguration.CODEC));

    public static void register() {}
}
