package com.wildscapes.entity.client;

import com.wildscapes.Wildscapes;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/** Model layers for the vanilla mobs Wildscapes re-skins. */
public final class WildscapesModelLayers {
    private WildscapesModelLayers() {}

    public static final ModelLayerLocation PILLAGER_SWAMP = create("pillager_swamp");
    public static final ModelLayerLocation VINDICATOR_SWAMP = create("vindicator_swamp");
    public static final ModelLayerLocation WITCH = create("witch");
    public static final ModelLayerLocation SLIME_SMALL = create("slime_small");
    public static final ModelLayerLocation SLIME_SMALL_OUTER = create("slime_small", "outer");
    public static final ModelLayerLocation SLIME_MEDIUM = create("slime_medium");
    public static final ModelLayerLocation SLIME_MEDIUM_OUTER = create("slime_medium", "outer");
    public static final ModelLayerLocation SLIME_LARGE = create("slime_large");
    public static final ModelLayerLocation SLIME_LARGE_OUTER = create("slime_large", "outer");

    private static ModelLayerLocation create(String name) {
        return create(name, "main");
    }

    private static ModelLayerLocation create(String name, String layer) {
        return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, name), layer);
    }
}
