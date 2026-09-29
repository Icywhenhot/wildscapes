package com.wildscapes.worldgen;

import java.util.List;
import java.util.Optional;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

public class WitchCampStructure extends Structure {
    public static final MapCodec<WitchCampStructure> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    settingsCodec(instance),
                    StructureTemplatePool.CODEC.fieldOf("start_pool")
                            .forGetter(s -> s.startPool),
                    Codec.intRange(0, 20).fieldOf("size")
                            .forGetter(s -> s.maxDepth),
                    Codec.intRange(1, 128).optionalFieldOf("max_distance_from_center", 80)
                            .forGetter(s -> s.maxDistanceFromCenter),
                    Codec.floatRange(0.0F, 1.0F).optionalFieldOf("min_dry_land", 0.8F)
                            .forGetter(s -> s.minDryLand),
                    Codec.intRange(1, 64).optionalFieldOf("dry_land_radius", 24)
                            .forGetter(s -> s.dryLandRadius),
                    Band.CODEC.listOf().optionalFieldOf("requirements", List.of())
                            .forGetter(s -> s.requirements),
                    Codec.STRING.optionalFieldOf("buried", "")
                            .forGetter(s -> s.buried),
                    Codec.intRange(1, 64).optionalFieldOf("attempts", 16)
                            .forGetter(s -> s.attempts))
                    .apply(instance, WitchCampStructure::new));

    private static final LiquidSettings LIQUIDS = LiquidSettings.IGNORE_WATERLOGGING;

    private final Holder<StructureTemplatePool> startPool;
    private final int maxDepth;
    private final int maxDistanceFromCenter;
    private final float minDryLand;
    private final int dryLandRadius;
    private final List<Band> requirements;
    private final String buried;
    private final int attempts;

    public WitchCampStructure(StructureSettings settings, Holder<StructureTemplatePool> startPool,
            int maxDepth, int maxDistanceFromCenter, float minDryLand, int dryLandRadius,
            List<Band> requirements, String buried, int attempts) {
        super(settings);
        this.startPool = startPool;
        this.maxDepth = maxDepth;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
        this.minDryLand = minDryLand;
        this.dryLandRadius = dryLandRadius;
        this.requirements = requirements;
        this.buried = buried;
        this.attempts = attempts;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        if (!isDryLand(context)) {
            return Optional.empty();
        }

        ChunkPos chunk = context.chunkPos();
        BlockPos start = new BlockPos(chunk.getMinBlockX(), 0, chunk.getMinBlockZ());

        for (int attempt = 0; attempt < attempts; attempt++) {
            Optional<GenerationStub> laid = JigsawPlacement.addPieces(
                    context,
                    startPool,
                    Optional.empty(),
                    maxDepth,
                    start,
                    false,
                    Optional.of(Heightmap.Types.WORLD_SURFACE_WG),
                    maxDistanceFromCenter,
                    PoolAliasLookup.EMPTY,
                    DimensionPadding.ZERO,
                    LIQUIDS);
            if (laid.isEmpty()) {
                continue;
            }

            StructurePiecesBuilder pieces = laid.get().getPiecesBuilder();
            List<String> ids = SwampVillageStructure.idsIn(pieces);
            if (requirements.stream().allMatch(band -> band.holds(ids))) {
                return Optional.of(new GenerationStub(laid.get().position(), Either.right(bury(context, pieces))));
            }
        }
        return Optional.empty();
    }

    private StructurePiecesBuilder bury(GenerationContext context, StructurePiecesBuilder pieces) {
        if (buried.isEmpty()) {
            return pieces;
        }
        StructurePiecesBuilder out = new StructurePiecesBuilder();
        for (StructurePiece piece : pieces.build().pieces()) {
            if (piece instanceof PoolElementStructurePiece pooled && pooled.getElement().toString().contains(buried)) {
                out.addPiece(new BuriedPiece(context.structureTemplateManager(), pooled, LIQUIDS));
            } else {
                out.addPiece(piece);
            }
        }
        return out;
    }

    private boolean isDryLand(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int dry = 0;
        int total = 0;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int x = chunk.getMiddleBlockX() + dx * dryLandRadius;
                int z = chunk.getMiddleBlockZ() + dz * dryLandRadius;
                int floor = context.chunkGenerator().getBaseHeight(
                        x, z, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
                int top = context.chunkGenerator().getBaseHeight(
                        x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
                boolean land = floor >= top;
                if (dx == 0 && dz == 0 && !land) {
                    return false;
                }
                total++;
                if (land) {
                    dry++;
                }
            }
        }
        return dry >= total * minDryLand;
    }

    public record Band(int min, int max, List<String> pieces, List<String> sameCountAs) {
        public static final Codec<Band> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        Codec.intRange(0, 64).optionalFieldOf("min", 0).forGetter(Band::min),
                        Codec.intRange(0, 64).optionalFieldOf("max", 64).forGetter(Band::max),
                        Codec.STRING.listOf().fieldOf("pieces").forGetter(Band::pieces),
                        Codec.STRING.listOf().optionalFieldOf("same_count_as", List.of()).forGetter(Band::sameCountAs))
                        .apply(instance, Band::new));

        boolean holds(List<String> ids) {
            long seen = ids.stream().filter(pieces::contains).count();
            if (!sameCountAs.isEmpty() && seen != ids.stream().filter(sameCountAs::contains).count()) {
                return false;
            }
            return seen >= min && seen <= max;
        }
    }

    @Override
    public StructureType<?> type() {
        return WildscapesStructures.WITCH_CAMP.get();
    }
}
