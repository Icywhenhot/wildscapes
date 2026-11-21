package com.wildscapes.worldgen;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

public class KeepAwaySpreadPlacement extends RandomSpreadStructurePlacement {
    public record KeepAway(ResourceLocation set, int chunks) {
        public static final Codec<KeepAway> CODEC = RecordCodecBuilder.create(i -> i.group(
                ResourceLocation.CODEC.fieldOf("set").forGetter(KeepAway::set),
                Codec.intRange(1, 16).fieldOf("chunk_count").forGetter(KeepAway::chunks)
        ).apply(i, KeepAway::new));
    }

    public static final Codec<KeepAwaySpreadPlacement> CODEC = RecordCodecBuilder.create(i -> i.group(
            Vec3i.offsetCodec(16).optionalFieldOf("locate_offset", Vec3i.ZERO).forGetter(p -> p.locateOffset()),
            FrequencyReductionMethod.CODEC.optionalFieldOf("frequency_reduction_method", FrequencyReductionMethod.DEFAULT)
                    .forGetter(p -> p.frequencyReductionMethod()),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("frequency", 1.0F).forGetter(p -> p.frequency()),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("salt").forGetter(p -> p.salt()),
            Codec.intRange(0, 4096).fieldOf("spacing").forGetter(RandomSpreadStructurePlacement::spacing),
            Codec.intRange(0, 4096).fieldOf("separation").forGetter(RandomSpreadStructurePlacement::separation),
            RandomSpreadType.CODEC.optionalFieldOf("spread_type", RandomSpreadType.LINEAR)
                    .forGetter(RandomSpreadStructurePlacement::spreadType),
            KeepAway.CODEC.listOf().fieldOf("keep_away").forGetter(p -> p.keepAway)
    ).apply(i, KeepAwaySpreadPlacement::new));

    private final List<KeepAway> keepAway;

    public KeepAwaySpreadPlacement(Vec3i locateOffset, FrequencyReductionMethod reduction, float frequency, int salt,
            int spacing, int separation, RandomSpreadType spreadType, List<KeepAway> keepAway) {
        super(locateOffset, reduction, frequency, salt, Optional.empty(), spacing, separation, spreadType);
        this.keepAway = keepAway;
    }

    @Override
    public boolean isStructureChunk(ChunkGeneratorStructureState state, int x, int z) {
        if (!super.isStructureChunk(state, x, z)) {
            return false;
        }
        for (KeepAway zone : keepAway) {
            for (Holder<StructureSet> set : state.possibleStructureSets()) {
                if (set.is(zone.set()) && state.hasStructureChunkInRange(set, x, z, zone.chunks())) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public StructurePlacementType<?> type() {
        return WildscapesStructures.KEEP_AWAY_SPREAD.get();
    }
}
