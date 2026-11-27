package com.wildscapes.worldgen;

import com.wildscapes.Wildscapes;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import com.wildscapes.registry.RegistryObject;
import com.wildscapes.registry.DeferredRegister;

public final class WildscapesPlacementModifiers {
    private WildscapesPlacementModifiers() {}

    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS =
            DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, Wildscapes.MODID);

    public static final RegistryObject<PlacementModifierType<ShoreDensityFilter>>
            SHORE_DENSITY = PLACEMENT_MODIFIERS.register("shore_density", () -> () -> ShoreDensityFilter.CODEC);

    public static void register() {
        PLACEMENT_MODIFIERS.register();
    }
}
