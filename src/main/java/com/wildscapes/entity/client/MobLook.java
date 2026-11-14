package com.wildscapes.entity.client;

import com.wildscapes.WildscapesConfig;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

public final class MobLook {
    private MobLook() {}

    public static boolean vanilla(EntityRendererProvider.Context ctx, String cem, ModelLayerLocation ours, Identifier texture) {
        return switch (WildscapesConfig.MOB_MODELS.get()) {
            case VANILLA -> true;
            case REDESIGNED -> false;
            case AUTO -> !hasCem(ctx.getResourceManager(), ours.model().getNamespace(), layerName(ours))
                    && packTouches(ctx.getResourceManager(), cem, texture);
        };
    }

    static boolean packTouches(ResourceManager rm, String cem, Identifier texture) {
        if (hasCem(rm, Identifier.DEFAULT_NAMESPACE, cem)) {
            return true;
        }
        return rm.getResource(texture).map(res -> !res.sourcePackId().equals("vanilla")).orElse(false);
    }

    private static boolean hasCem(ResourceManager rm, String ns, String name) {
        return rm.getResource(Identifier.fromNamespaceAndPath(ns, "optifine/cem/" + name + ".jem")).isPresent()
                || rm.getResource(Identifier.fromNamespaceAndPath(ns, "emf/cem/" + name + ".jem")).isPresent();
    }

    private static String layerName(ModelLayerLocation layer) {
        String path = layer.model().getPath();
        return layer.layer().equals("main") ? path : path + "_" + layer.layer();
    }
}
