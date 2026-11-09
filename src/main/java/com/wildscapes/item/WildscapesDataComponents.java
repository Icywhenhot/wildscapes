package com.wildscapes.item;

import com.wildscapes.Wildscapes;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class WildscapesDataComponents {
    private WildscapesDataComponents() {}

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Wildscapes.MODID);

    public static final RegistryObject<DataComponentType<SoupContents>> SOUP_CONTENTS =
            DATA_COMPONENTS.register("soup_contents", () -> DataComponentType.<SoupContents>builder()
                    .persistent(SoupContents.CODEC)
                    .networkSynchronized(SoupContents.STREAM_CODEC)
                    .build());

    public static void register(IEventBus bus) {
        DATA_COMPONENTS.register(bus);
    }
}
