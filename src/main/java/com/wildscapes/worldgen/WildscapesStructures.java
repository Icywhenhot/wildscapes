package com.wildscapes.worldgen;

import com.wildscapes.Wildscapes;

import net.minecraft.core.Registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

public final class WildscapesStructures {
    private WildscapesStructures() {}

    public static final StructureType<SwampVillageStructure> SWAMP_VILLAGE =
            Registry.register(BuiltInRegistries.STRUCTURE_TYPE, Wildscapes.id("swamp_village"), () -> SwampVillageStructure.CODEC);

    public static final StructureType<WitchCampStructure> WITCH_CAMP =
            Registry.register(BuiltInRegistries.STRUCTURE_TYPE, Wildscapes.id("witch_camp"), () -> WitchCampStructure.CODEC);

    public static final StructurePieceType BURIED_PIECE =
            Registry.register(BuiltInRegistries.STRUCTURE_PIECE, Wildscapes.id("buried_jigsaw"), BuriedPiece::new);

    public static final StructureProcessorType<StiltProcessor> STILTS =
            Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR, Wildscapes.id("stilts"), () -> StiltProcessor.CODEC);

    public static final StructureProcessorType<ConnectProcessor> CONNECT =
            Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR, Wildscapes.id("connect"), () -> ConnectProcessor.CODEC);

    public static final StructureProcessorType<ClearProcessor> CLEAR =
            Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR, Wildscapes.id("clear"), () -> ClearProcessor.CODEC);

    public static final StructureProcessorType<OvergrowthProcessor> OVERGROWTH =
            Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR, Wildscapes.id("overgrowth"), () -> OvergrowthProcessor.CODEC);

    public static void register() {}
}
