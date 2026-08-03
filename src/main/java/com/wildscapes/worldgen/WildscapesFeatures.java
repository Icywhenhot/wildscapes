package com.wildscapes.worldgen;

import com.wildscapes.Wildscapes;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class WildscapesFeatures {
    private WildscapesFeatures() {}

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, Wildscapes.MODID);

    public static final DeferredHolder<Feature<?>, CypressTreeFeature> CYPRESS_TREE =
            FEATURES.register("cypress_tree", () -> new CypressTreeFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, WaterloggedDoublePlantFeature> WATERLOGGED_DOUBLE_PLANT =
            FEATURES.register("waterlogged_double_plant",
                    () -> new WaterloggedDoublePlantFeature(SimpleBlockConfiguration.CODEC));

    public static void register(IEventBus bus) {
        FEATURES.register(bus);
    }
}
