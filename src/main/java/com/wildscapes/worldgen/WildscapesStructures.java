package com.wildscapes.worldgen;

import com.wildscapes.Wildscapes;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class WildscapesStructures {
    private WildscapesStructures() {}

    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, Wildscapes.MODID);

    public static final DeferredRegister<StructureProcessorType<?>> PROCESSORS =
            DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, Wildscapes.MODID);

    public static final DeferredHolder<StructureType<?>, StructureType<SwampVillageStructure>> SWAMP_VILLAGE =
            STRUCTURE_TYPES.register("swamp_village", () -> () -> SwampVillageStructure.CODEC);

    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<StiltProcessor>> STILTS =
            PROCESSORS.register("stilts", () -> () -> StiltProcessor.CODEC);

    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<ConnectProcessor>> CONNECT =
            PROCESSORS.register("connect", () -> () -> ConnectProcessor.CODEC);

    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<ClearProcessor>> CLEAR =
            PROCESSORS.register("clear", () -> () -> ClearProcessor.CODEC);

    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<OvergrowthProcessor>> OVERGROWTH =
            PROCESSORS.register("overgrowth", () -> () -> OvergrowthProcessor.CODEC);

    public static void register(IEventBus bus) {
        STRUCTURE_TYPES.register(bus);
        PROCESSORS.register(bus);
    }
}
