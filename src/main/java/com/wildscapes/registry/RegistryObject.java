package com.wildscapes.registry;

import java.util.function.Supplier;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public final class RegistryObject<T> implements Supplier<T> {
    private final ResourceLocation id;
    private final Supplier<? extends T> factory;
    private T value;
    private ResourceKey<T> key;

    RegistryObject(ResourceLocation id, Supplier<? extends T> factory) {
        this.id = id;
        this.factory = factory;
    }

    @SuppressWarnings("unchecked")
    <R> void registerInto(Registry<R> registry) {
        value = factory.get();
        Registry.register(registry, id, (R) value);
        key = (ResourceKey<T>) ResourceKey.create(registry.key(), id);
    }

    @Override
    public T get() {
        if (value == null) {
            throw new IllegalStateException("registry object " + id + " is not registered yet");
        }
        return value;
    }

    public boolean isPresent() {
        return value != null;
    }

    public ResourceLocation getId() {
        return id;
    }

    public ResourceKey<T> getKey() {
        return key;
    }
}
