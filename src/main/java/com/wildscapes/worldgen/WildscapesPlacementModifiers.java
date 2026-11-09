package com.wildscapes.worldgen;

import com.wildscapes.Wildscapes;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class WildscapesPlacementModifiers {
    private WildscapesPlacementModifiers() {}

    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS =
            DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, Wildscapes.MODID);

    public static final RegistryObject<PlacementModifierType<ShoreDensityFilter>>
            SHORE_DENSITY = PLACEMENT_MODIFIERS.register("shore_density", () -> () -> ShoreDensityFilter.CODEC);

    public static void register(IEventBus bus) {
        PLACEMENT_MODIFIERS.register(bus);
    }
}
