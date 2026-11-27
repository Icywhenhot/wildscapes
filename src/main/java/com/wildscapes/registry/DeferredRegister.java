package com.wildscapes.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public final class DeferredRegister<T> {
    private final ResourceKey<? extends Registry<T>> registryKey;
    private final String namespace;
    private final List<RegistryObject<? extends T>> entries = new ArrayList<>();
    private boolean registered;

    private DeferredRegister(ResourceKey<? extends Registry<T>> registryKey, String namespace) {
        this.registryKey = registryKey;
        this.namespace = namespace;
    }

    public static <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> registryKey, String namespace) {
        return new DeferredRegister<>(registryKey, namespace);
    }

    public <I extends T> RegistryObject<I> register(String name, Supplier<? extends I> factory) {
        RegistryObject<I> entry = new RegistryObject<>(new ResourceLocation(namespace, name), factory);
        entries.add(entry);
        if (registered) {
            entry.registerInto(registry());
        }
        return entry;
    }

    public List<RegistryObject<? extends T>> getEntries() {
        return List.copyOf(entries);
    }

    public void register() {
        if (registered) {
            return;
        }
        registered = true;
        Registry<T> registry = registry();
        for (RegistryObject<? extends T> entry : entries) {
            entry.registerInto(registry);
        }
    }

    @SuppressWarnings("unchecked")
    private Registry<T> registry() {
        Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.get(registryKey.location());
        if (registry == null) {
            throw new IllegalStateException("no registry " + registryKey.location());
        }
        return registry;
    }
}
