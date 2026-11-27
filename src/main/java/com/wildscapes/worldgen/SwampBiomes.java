package com.wildscapes.worldgen;

import com.wildscapes.entity.SwampVariants;
import com.wildscapes.entity.WildscapesEntities;
import net.minecraft.world.entity.EntityType;
import com.wildscapes.Wildscapes;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;

public final class SwampBiomes {
    private SwampBiomes() {}

    public static void register() {
        var swamp = BiomeSelectors.includeByKey(Biomes.SWAMP);
        var trees = ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation("trees_swamp"));
        BiomeModifications.create(Wildscapes.id("remove_swamp_oak"))
                .add(ModificationPhase.REMOVALS, swamp, context -> context.getGenerationSettings().removeFeature(trees));

        for (String name : new String[] {
                "swamp_trees_land_swamp", "swamp_trees_water_swamp", "swamp_fallen_trees_swamp",
                "swamp_tree_roots_swamp", "luminous_mushrooms_swamp", "shelf_luminous_mushrooms_swamp",
                "huge_brown_mushroom_swamp", "huge_red_mushroom_swamp", "swamp_oak_reduced",
                "duckweed_swamp", "rushes_swamp", "short_rushes_swamp"
        }) {
            BiomeModifications.addFeature(swamp, GenerationStep.Decoration.VEGETAL_DECORATION,
                    ResourceKey.create(Registries.PLACED_FEATURE, Wildscapes.id(name)));
        }

        BiomeModifications.addSpawn(swamp, MobCategory.MONSTER, EntityType.DROWNED, 40, 1, 2);
        BiomeModifications.addSpawn(BiomeSelectors.tag(SwampVariants.SWAMP_BIOMES), MobCategory.MONSTER,
                WildscapesEntities.BOGGED.get(), 30, 4, 4);
    }
}
