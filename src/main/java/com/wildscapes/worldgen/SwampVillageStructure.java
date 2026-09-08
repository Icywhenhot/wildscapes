package com.wildscapes.worldgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

public class SwampVillageStructure extends Structure {
    public static final MapCodec<SwampVillageStructure> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    settingsCodec(instance),
                    StructureTemplatePool.CODEC.fieldOf("start_pool")
                            .forGetter(s -> s.startPool),
                    Codec.intRange(0, 20).fieldOf("size")
                            .forGetter(s -> s.maxDepth),
                    HeightProvider.CODEC.fieldOf("start_height")
                            .forGetter(s -> s.startHeight),
                    Codec.intRange(1, 128).optionalFieldOf("max_distance_from_center", 80)
                            .forGetter(s -> s.maxDistanceFromCenter),

                    Codec.floatRange(0.0F, 1.0F).optionalFieldOf("min_open_water", 0.7F)
                            .forGetter(s -> s.minOpenWater),
                    Codec.intRange(1, 64).optionalFieldOf("open_water_radius", 32)
                            .forGetter(s -> s.openWaterRadius),
                    Quota.CODEC.listOf().optionalFieldOf("requirements", List.of())
                            .forGetter(s -> s.requirements),
                    Codec.intRange(1, 64).optionalFieldOf("attempts", 12)
                            .forGetter(s -> s.attempts),
                    PoolAliasBinding.CODEC.listOf().optionalFieldOf("pool_aliases", List.of())
                            .forGetter(s -> s.poolAliases),
                    Quota.CODEC.listOf().optionalFieldOf("derelict_requirements")
                            .forGetter(s -> s.derelictRequirements),
                    HeightProvider.CODEC.optionalFieldOf("derelict_start_height")
                            .forGetter(s -> s.derelictStartHeight))
                    .apply(instance, SwampVillageStructure::new));

    private final Holder<StructureTemplatePool> startPool;
    private final int maxDepth;
    private final HeightProvider startHeight;
    private final int maxDistanceFromCenter;
    private final float minOpenWater;
    private final int openWaterRadius;
    private final List<Quota> requirements;
    private final int attempts;
    private final List<PoolAliasBinding> poolAliases;
    private final Optional<List<Quota>> derelictRequirements;
    private final Optional<HeightProvider> derelictStartHeight;

    public SwampVillageStructure(StructureSettings settings, Holder<StructureTemplatePool> startPool,
            int maxDepth, HeightProvider startHeight, int maxDistanceFromCenter,
            float minOpenWater, int openWaterRadius, List<Quota> requirements, int attempts,
            List<PoolAliasBinding> poolAliases, Optional<List<Quota>> derelictRequirements,
            Optional<HeightProvider> derelictStartHeight) {
        super(settings);
        this.startPool = startPool;
        this.maxDepth = maxDepth;
        this.startHeight = startHeight;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
        this.minOpenWater = minOpenWater;
        this.openWaterRadius = openWaterRadius;
        this.requirements = requirements;
        this.attempts = attempts;
        this.poolAliases = poolAliases;
        this.derelictRequirements = derelictRequirements;
        this.derelictStartHeight = derelictStartHeight;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        if (!isOpenWater(context)) {
            return Optional.empty();
        }

        ChunkPos chunk = context.chunkPos();
        PoolAliasLookup aliases = PoolAliasLookup.create(
                poolAliases, chunk.getWorldPosition(), context.seed());
        boolean derelict = startPool.unwrapKey()
                .map(key -> !aliases.lookup(key).equals(key))
                .orElse(false);

        List<Quota> quotas = derelict ? derelictRequirements.orElse(requirements) : requirements;
        HeightProvider height = derelict ? derelictStartHeight.orElse(startHeight) : startHeight;
        LiquidSettings liquids = derelict
                ? LiquidSettings.APPLY_WATERLOGGING
                : LiquidSettings.IGNORE_WATERLOGGING;

        WorldGenerationContext heights = new WorldGenerationContext(
                context.chunkGenerator(), context.heightAccessor());
        BlockPos start = new BlockPos(
                chunk.getMinBlockX(),
                height.sample(context.random(), heights),
                chunk.getMinBlockZ());

        for (int attempt = 0; attempt < attempts; attempt++) {
            Optional<GenerationStub> laid = JigsawPlacement.addPieces(
                    context,
                    startPool,
                    Optional.empty(),
                    maxDepth,
                    start,
                    false,
                    Optional.empty(),
                    maxDistanceFromCenter,
                    aliases,
                    DimensionPadding.ZERO,
                    liquids);
            if (laid.isEmpty()) {
                continue;
            }

            StructurePiecesBuilder pieces = laid.get().getPiecesBuilder();
            if (meetsRequirements(pieces, quotas)) {
                return Optional.of(new GenerationStub(laid.get().position(), Either.right(pieces)));
            }
        }
        return Optional.empty();
    }

    private static final Pattern PIECE_ID = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");

    private static List<String> idsIn(StructurePiecesBuilder pieces) {
        List<String> ids = new ArrayList<>();
        for (StructurePiece piece : pieces.build().pieces()) {
            if (!(piece instanceof PoolElementStructurePiece pooled)) {
                continue;
            }

            Matcher found = PIECE_ID.matcher(pooled.getElement().toString());
            if (found.find()) {
                ids.add(found.group());
            }
        }
        return ids;
    }

    private boolean meetsRequirements(StructurePiecesBuilder pieces, List<Quota> quotas) {
        if (quotas.isEmpty()) {
            return true;
        }
        List<String> ids = idsIn(pieces);
        for (Quota quota : quotas) {
            int seen = 0;
            for (String id : ids) {
                if (quota.pieces().contains(id)) {
                    seen++;
                }
            }
            if (seen < quota.min()) {
                return false;
            }
        }
        return true;
    }

    public record Quota(int min, List<String> pieces) {
        public static final Codec<Quota> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        Codec.intRange(0, 64).fieldOf("min").forGetter(Quota::min),
                        Codec.STRING.listOf().fieldOf("pieces").forGetter(Quota::pieces))
                        .apply(instance, Quota::new));
    }

    private boolean isOpenWater(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int seaLevel = context.chunkGenerator().getSeaLevel();
        int wet = 0;
        int total = 0;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int x = chunk.getMiddleBlockX() + dx * openWaterRadius;
                int z = chunk.getMiddleBlockZ() + dz * openWaterRadius;
                int floor = context.chunkGenerator().getBaseHeight(
                        x, z, Heightmap.Types.OCEAN_FLOOR_WG,
                        context.heightAccessor(), context.randomState());
                boolean lake = floor < seaLevel;
                if (dx == 0 && dz == 0 && !lake) {
                    return false;
                }
                total++;
                if (lake) {
                    wet++;
                }
            }
        }
        return wet >= total * minOpenWater;
    }

    @Override
    public StructureType<?> type() {
        return WildscapesStructures.SWAMP_VILLAGE.get();
    }
}
