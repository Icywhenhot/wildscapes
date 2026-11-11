package com.wildscapes.entity.client;

import com.wildscapes.WildscapesConfig;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public final class MobLook {
    private MobLook() {}

    public static boolean vanilla(EntityRendererProvider.Context ctx, String cem, ModelLayerLocation ours, ResourceLocation texture) {
        return switch (WildscapesConfig.MOB_MODELS.get()) {
            case VANILLA -> true;
            case REDESIGNED -> false;
            case AUTO -> !hasCem(ctx.getResourceManager(), ours.getModel().getNamespace(), layerName(ours))
                    && packTouches(ctx.getResourceManager(), cem, texture);
        };
    }

    static boolean packTouches(ResourceManager rm, String cem, ResourceLocation texture) {
        if (hasCem(rm, ResourceLocation.DEFAULT_NAMESPACE, cem)) {
            return true;
        }
        return rm.getResource(texture).map(res -> !res.sourcePackId().equals("vanilla")).orElse(false);
    }

    private static boolean hasCem(ResourceManager rm, String ns, String name) {
        return rm.getResource(ResourceLocation.fromNamespaceAndPath(ns, "optifine/cem/" + name + ".jem")).isPresent()
                || rm.getResource(ResourceLocation.fromNamespaceAndPath(ns, "emf/cem/" + name + ".jem")).isPresent();
    }

    private static String layerName(ModelLayerLocation layer) {
        String path = layer.getModel().getPath();
        return layer.getLayer().equals("main") ? path : path + "_" + layer.getLayer();
    }
}
