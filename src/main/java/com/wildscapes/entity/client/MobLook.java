package com.wildscapes.entity.client;

import com.wildscapes.WildscapesConfig;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public final class MobLook {
    private MobLook() {}

    public static boolean vanilla(EntityRendererProvider.Context ctx, String cem, ResourceLocation texture) {
        return switch (WildscapesConfig.MOB_MODELS.get()) {
            case VANILLA -> true;
            case REDESIGNED -> false;
            case AUTO -> packTouches(ctx.getResourceManager(), cem, texture);
        };
    }

    static boolean packTouches(ResourceManager rm, String cem, ResourceLocation texture) {
        if (rm.getResource(ResourceLocation.withDefaultNamespace("optifine/cem/" + cem + ".jem")).isPresent()
                || rm.getResource(ResourceLocation.withDefaultNamespace("emf/cem/" + cem + ".jem")).isPresent()) {
            return true;
        }
        return rm.getResource(texture).map(res -> !res.sourcePackId().equals("vanilla")).orElse(false);
    }
}
