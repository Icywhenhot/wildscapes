package com.wildscapes.item;

import com.wildscapes.Wildscapes;

import net.minecraft.core.Registry;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

public final class WildscapesDataComponents {
    private WildscapesDataComponents() {}

    public static final DataComponentType<SoupContents> SOUP_CONTENTS =
            Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Wildscapes.id("soup_contents"), DataComponentType.<SoupContents>builder()
                    .persistent(SoupContents.CODEC)
                    .networkSynchronized(SoupContents.STREAM_CODEC)
                    .build());

    public static void register() {}
}
