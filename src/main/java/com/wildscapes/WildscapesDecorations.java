package com.wildscapes;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class WildscapesDecorations {
    private static final DeferredRegister<BannerPattern> BANNERS =
            DeferredRegister.create(Registries.BANNER_PATTERN, Wildscapes.MODID);
    private static final DeferredRegister<PaintingVariant> PAINTINGS =
            DeferredRegister.create(Registries.PAINTING_VARIANT, Wildscapes.MODID);

    public static final RegistryObject<BannerPattern> SLIME =
            BANNERS.register("slime", () -> new BannerPattern("wildscapes:slime"));
    public static final RegistryObject<BannerPattern> SPIRIT =
            BANNERS.register("spirit", () -> new BannerPattern("wildscapes:spirit"));
    public static final RegistryObject<PaintingVariant> ABOMINATION =
            PAINTINGS.register("abomination", () -> new PaintingVariant(32, 16));
    public static final RegistryObject<PaintingVariant> SWAMP_NIGHT =
            PAINTINGS.register("swamp_night", () -> new PaintingVariant(32, 32));

    private WildscapesDecorations() {}

    public static void register(IEventBus bus) {
        BANNERS.register(bus);
        PAINTINGS.register(bus);
    }
}
