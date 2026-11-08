package com.wildscapes.entity;

import com.wildscapes.Wildscapes;

import net.minecraft.core.Registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class WildscapesEntities {
    private WildscapesEntities() {}

    public static final EntityType<AbominationEntity> ABOMINATION =
            Registry.register(BuiltInRegistries.ENTITY_TYPE, Wildscapes.id("abomination"), EntityType.Builder.<AbominationEntity>of(AbominationEntity::new, MobCategory.MONSTER)
                    .sized(1.6F, 1.9F)
                    .clientTrackingRange(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Wildscapes.id("abomination"))));

    public static final EntityType<MirelashHook> MIRELASH_HOOK =
            Registry.register(BuiltInRegistries.ENTITY_TYPE, Wildscapes.id("mirelash_hook"), EntityType.Builder.<MirelashHook>of(MirelashHook::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(12)
                    .updateInterval(1)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Wildscapes.id("mirelash_hook"))));

    public static final EntityType<ResidueCloud> RESIDUE_CLOUD =
            Registry.register(BuiltInRegistries.ENTITY_TYPE, Wildscapes.id("residue_cloud"), EntityType.Builder.<ResidueCloud>of(ResidueCloud::new, MobCategory.MISC)
                    .sized(6.0F, 0.5F)
                    .clientTrackingRange(10)
                    .updateInterval(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Wildscapes.id("residue_cloud"))));

    public static final EntityType<IncendiaryBottle> INCENDIARY_BOTTLE =
            Registry.register(BuiltInRegistries.ENTITY_TYPE, Wildscapes.id("incendiary_bottle"), EntityType.Builder.<IncendiaryBottle>of(IncendiaryBottle::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(8)
                    .updateInterval(2)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Wildscapes.id("incendiary_bottle"))));

    public static void register() {
        AbominationEntity.registerData();
    }
}
