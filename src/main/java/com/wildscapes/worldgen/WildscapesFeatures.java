package com.wildscapes.worldgen;

import com.wildscapes.Wildscapes;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class WildscapesFeatures {
    private WildscapesFeatures() {}

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, Wildscapes.MODID);

    public static final RegistryObject<CypressTreeFeature> CYPRESS_TREE =
            FEATURES.register("cypress_tree", () -> new CypressTreeFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<TemplateTreeFeature> TEMPLATE_TREE =
            FEATURES.register("template_tree", () -> new TemplateTreeFeature(TemplateTreeConfiguration.CODEC));

    public static final RegistryObject<WaterloggedDoublePlantFeature> WATERLOGGED_DOUBLE_PLANT =
            FEATURES.register("waterlogged_double_plant",
                    () -> new WaterloggedDoublePlantFeature(SimpleBlockConfiguration.CODEC));

    public static final RegistryObject<ShelfMushroomFeature> SHELF_MUSHROOM =
            FEATURES.register("shelf_mushroom", () -> new ShelfMushroomFeature(SimpleBlockConfiguration.CODEC));

    public static void register(IEventBus bus) {
        FEATURES.register(bus);
    }
}
