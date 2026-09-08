package com.wildscapes.entity;

import com.wildscapes.Wildscapes;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class WildscapesEntities {
    private WildscapesEntities() {}

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Wildscapes.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<AbominationEntity>> ABOMINATION =
            ENTITY_TYPES.register("abomination", () -> EntityType.Builder.of(AbominationEntity::new, MobCategory.MONSTER)
                    .sized(1.6F, 1.9F)
                    .clientTrackingRange(10)
                    .build("abomination"));

    public static final DeferredHolder<EntityType<?>, EntityType<MirelashHook>> MIRELASH_HOOK =
            ENTITY_TYPES.register("mirelash_hook", () -> EntityType.Builder.<MirelashHook>of(MirelashHook::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(12)
                    .updateInterval(1)
                    .build("mirelash_hook"));

    public static void register(IEventBus bus) {
        ENTITY_TYPES.register(bus);
    }
}
