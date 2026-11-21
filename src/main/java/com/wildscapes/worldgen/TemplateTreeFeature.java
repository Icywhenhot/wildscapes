package com.wildscapes.worldgen;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.wildscapes.Wildscapes;
import com.wildscapes.block.DrapingMossCarpetBlock;
import com.wildscapes.block.HangingMossBlock;
import com.wildscapes.block.ShelfMushroomBlock;
import com.wildscapes.block.WildscapesBlocks;
import com.wildscapes.worldgen.TemplateTreeConfiguration.Ground;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockStateMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class TemplateTreeFeature extends Feature<TemplateTreeConfiguration> {
    private static final RuleProcessor SWAP_TO_CYPRESS = new RuleProcessor(buildRules());

    private static List<ProcessorRule> buildRules() {
        List<ProcessorRule> rules = new java.util.ArrayList<>();
        addPillar(rules, Blocks.DARK_OAK_LOG, WildscapesBlocks.CYPRESS_LOG);
        addPillar(rules, Blocks.DARK_OAK_WOOD, WildscapesBlocks.CYPRESS_WOOD);
        addPillar(rules, Blocks.STRIPPED_DARK_OAK_LOG, WildscapesBlocks.STRIPPED_CYPRESS_LOG);
        addPillar(rules, Blocks.STRIPPED_DARK_OAK_WOOD, WildscapesBlocks.STRIPPED_CYPRESS_WOOD);

        BlockState leaves = WildscapesBlocks.CYPRESS_LEAVES.defaultBlockState()
                .setValue(LeavesBlock.PERSISTENT, true)
                .setValue(LeavesBlock.DISTANCE, 1);
        rules.add(new ProcessorRule(new BlockMatchTest(Blocks.MANGROVE_LEAVES), AlwaysTrueTest.INSTANCE, leaves));
        rules.add(new ProcessorRule(new BlockMatchTest(Blocks.DARK_OAK_LEAVES), AlwaysTrueTest.INSTANCE, leaves));
        return rules;
    }

    private static void addPillar(List<ProcessorRule> rules, Block from, Block to) {
        for (net.minecraft.core.Direction.Axis axis : net.minecraft.core.Direction.Axis.values()) {
            rules.add(new ProcessorRule(
                    new BlockStateMatchTest(from.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis)),
                    AlwaysTrueTest.INSTANCE,
                    to.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis)));
        }
    }

    public TemplateTreeFeature(Codec<TemplateTreeConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<TemplateTreeConfiguration> context) {
        List<Identifier> templates = context.config().templates();
        if (templates.isEmpty()) {
            return false;
        }
        WorldGenLevel level = context.level();
        RandomSource random = context.random();

        BlockPos surface = context.origin();
        if (!hasFooting(level, surface, context.config().ground(), context.config().maxWaterDepth())) {
            return false;
        }

        StructureTemplateManager manager = level.getLevel().getStructureManager();
        Identifier id = templates.get(random.nextInt(templates.size()));
        Optional<StructureTemplate> found = manager.get(id);
        if (found.isEmpty()) {
            Wildscapes.LOGGER.warn("Tree structure {} is missing; expected it in data/{}/structure/{}.nbt",
                    id, id.getNamespace(), id.getPath());
            return false;
        }
        StructureTemplate template = found.get();

        BlockPos anchor = trunkAnchor(template);
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setRotation(Rotation.getRandom(random))
                .setMirror(random.nextBoolean() ? Mirror.NONE : Mirror.FRONT_BACK)
                .setRotationPivot(anchor)
                .setIgnoreEntities(true)
                .setBoundingBox(writableArea(surface))
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR)
                .addProcessor(SWAP_TO_CYPRESS);

        BlockPos trunk = surface.above(context.config().yOffset());
        BlockPos corner = trunk.subtract(StructureTemplate.calculateRelativePosition(settings, anchor));
        if (!hasRoomFor(level, template, settings, corner, trunk)
                || hitsBuiltBlocks(level, template, settings, corner)
                || overhangs(level, template, settings, corner)) {
            return false;
        }
        if (!template.placeInWorld(level, corner, corner, settings, random, Block.UPDATE_CLIENTS)) {
            return false;
        }
        repairVines(level, template, settings, corner);
        growMoss(level, template.getBoundingBox(settings, corner), random);
        return true;
    }

    private static boolean hitsBuiltBlocks(WorldGenLevel level, StructureTemplate template,
            StructurePlaceSettings settings, BlockPos corner) {
        BoundingBox writable = settings.getBoundingBox();
        for (StructureTemplate.StructureBlockInfo info : settings.getRandomPalette(template.palettes, corner).blocks()) {
            if (info.state().isAir() || info.state().is(Blocks.STRUCTURE_VOID)) {
                continue;
            }
            BlockPos pos = StructureTemplate.calculateRelativePosition(settings, info.pos()).offset(corner);
            if (writable != null && !writable.isInside(pos)) {
                continue;
            }
            if (!isWild(level.getBlockState(pos))) {
                return true;
            }
        }
        return false;
    }

    private static boolean overhangs(WorldGenLevel level, StructureTemplate template,
            StructurePlaceSettings settings, BlockPos corner) {
        List<StructureTemplate.StructureBlockInfo> blocks = settings.getRandomPalette(template.palettes, corner).blocks();
        int lowest = Integer.MAX_VALUE;
        for (StructureTemplate.StructureBlockInfo info : blocks) {
            if (isTrunk(info.state())) {
                lowest = Math.min(lowest, info.pos().getY());
            }
        }
        for (StructureTemplate.StructureBlockInfo info : blocks) {
            if (info.pos().getY() != lowest || !isTrunk(info.state())) {
                continue;
            }
            BlockPos below = StructureTemplate.calculateRelativePosition(settings, info.pos()).offset(corner).below();
            BlockState support = level.getBlockState(below);
            if (!support.is(Blocks.WATER) && (support.isAir() || support.canBeReplaced())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isTrunk(BlockState state) {
        return state.is(Blocks.DARK_OAK_LOG) || state.is(Blocks.DARK_OAK_WOOD)
                || state.is(WildscapesBlocks.CYPRESS_LOG) || state.is(WildscapesBlocks.CYPRESS_WOOD);
    }

    private static boolean isWild(BlockState state) {
        Block block = state.getBlock();
        return state.isAir()
                || state.is(Blocks.WATER)
                || state.is(BlockTags.REPLACEABLE_BY_TREES)
                || state.is(BlockTags.LEAVES)
                || state.is(BlockTags.LOGS)
                || state.is(BlockTags.DIRT)
                || state.is(BlockTags.SAND)
                || state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(Blocks.CLAY)
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.MOSS_CARPET)
                || block instanceof BushBlock
                || block instanceof VineBlock
                || block instanceof DrapingMossCarpetBlock
                || block instanceof HangingMossBlock
                || block instanceof ShelfMushroomBlock;
    }

    private static BoundingBox writableArea(BlockPos origin) {
        int minX = SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(origin.getX()) - 1);
        int minZ = SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(origin.getZ()) - 1);
        return new BoundingBox(minX, Integer.MIN_VALUE / 2, minZ, minX + 47, Integer.MAX_VALUE / 2, minZ + 47);
    }

    private static final float MOSS_CHANCE = 0.25F;

    private static void growMoss(WorldGenLevel level, BoundingBox box, RandomSource random) {
        for (BlockPos pos : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(),
                box.maxX(), box.maxY(), box.maxZ())) {
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.MOSS_CARPET)) {
                DrapingMossCarpetBlock.placeGrown(level, pos);
                continue;
            }
            if (!state.is(BlockTags.LOGS) && !state.is(BlockTags.LEAVES)) {
                continue;
            }
            BlockPos top = pos.above();
            if (level.getBlockState(top).isAir() && random.nextFloat() < MOSS_CHANCE) {
                DrapingMossCarpetBlock.placeGrown(level, top);
            }
        }
    }

    private static void repairVines(WorldGenLevel level, StructureTemplate template,
            StructurePlaceSettings settings, BlockPos corner) {
        for (StructureTemplate.StructureBlockInfo info : template.filterBlocks(corner, settings, Blocks.VINE, true)) {
            BlockPos pos = info.pos();
            BlockState vine = level.getBlockState(pos);
            if (!vine.is(Blocks.VINE) || hasAnyFace(vine)) {
                continue;
            }
            BlockState hung = rehang(level, pos);
            level.setBlock(pos, hung == null ? Blocks.AIR.defaultBlockState() : hung, Block.UPDATE_CLIENTS);
        }
    }

    private static BlockState rehang(WorldGenLevel level, BlockPos pos) {
        BlockState above = level.getBlockState(pos.above());
        if (above.is(Blocks.VINE) && hasAnyFace(above)) {
            return copyFaces(above, Blocks.VINE.defaultBlockState());
        }

        BlockState gripping = Blocks.VINE.defaultBlockState();
        boolean gripped = false;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos neighbour = pos.relative(side);
            if (level.getBlockState(neighbour).isFaceSturdy(level, neighbour, side.getOpposite())) {
                gripping = gripping.setValue(VineBlock.getPropertyForFace(side), true);
                gripped = true;
            }
        }
        if (gripped) {
            return gripping;
        }

        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockState beside = level.getBlockState(pos.relative(side));
            if (beside.is(Blocks.VINE) && hasAnyFace(beside)) {
                return copyFaces(beside, Blocks.VINE.defaultBlockState());
            }
        }

        return null;
    }

    private static BlockState copyFaces(BlockState from, BlockState to) {
        for (Direction side : Direction.Plane.HORIZONTAL) {
            to = to.setValue(VineBlock.getPropertyForFace(side), from.getValue(VineBlock.getPropertyForFace(side)));
        }
        return to;
    }

    private static boolean hasAnyFace(BlockState vine) {
        if (vine.getValue(VineBlock.UP)) {
            return true;
        }
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (vine.getValue(VineBlock.getPropertyForFace(side))) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasFooting(WorldGenLevel level, BlockPos origin, Ground ground,
            int maxWaterDepth) {
        BlockState here = level.getBlockState(origin);
        BlockState below = level.getBlockState(origin.below());

        if (ground == Ground.WATER) {
            if (!here.getFluidState().is(FluidTags.WATER) && !below.getFluidState().is(FluidTags.WATER)) {
                return false;
            }
            return waterDepth(level, origin, maxWaterDepth) <= maxWaterDepth;
        }
        return (below.is(WildscapesBlocks.SOIL) || below.is(Blocks.MUD)
                        || below.is(Blocks.CLAY) || below.is(Blocks.SAND))
                && !below.getFluidState().is(FluidTags.WATER)
                && (here.isAir() || here.canBeReplaced())
                && !here.getFluidState().is(FluidTags.WATER);
    }

    private static int waterDepth(WorldGenLevel level, BlockPos bed, int giveUpAfter) {
        BlockPos.MutableBlockPos cursor = bed.mutable();
        int depth = 0;
        while (depth <= giveUpAfter && level.getBlockState(cursor).getFluidState().is(FluidTags.WATER)) {
            depth++;
            cursor.move(0, 1, 0);
        }
        return depth;
    }

    private static final Block[] SOLID_PARTS = {
            Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_WOOD, Blocks.MANGROVE_LEAVES, Blocks.DARK_OAK_LEAVES };

    private static final int TRUNK_SPACING = 3;
    private static final int TRUNK_HEIGHT = 4;

    private static boolean hasRoomFor(WorldGenLevel level, StructureTemplate template,
            StructurePlaceSettings settings, BlockPos corner, BlockPos trunk) {
        for (int dx = -TRUNK_SPACING; dx <= TRUNK_SPACING; dx++) {
            for (int dz = -TRUNK_SPACING; dz <= TRUNK_SPACING; dz++) {
                for (int dy = 0; dy <= TRUNK_HEIGHT; dy++) {
                    if (level.getBlockState(trunk.offset(dx, dy, dz)).is(BlockTags.LOGS)) {
                        return false;
                    }
                }
            }
        }

        for (Block part : SOLID_PARTS) {
            for (StructureTemplate.StructureBlockInfo info : template.filterBlocks(corner, settings, part, true)) {
                BlockState existing = level.getBlockState(info.pos());
                if (existing.is(BlockTags.LOGS) || existing.is(WildscapesBlocks.CYPRESS_LEAVES)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static BlockPos trunkAnchor(StructureTemplate template) {
        StructurePlaceSettings raw = new StructurePlaceSettings();
        int count = 0;
        int sumX = 0;
        int sumZ = 0;
        int lowest = Integer.MAX_VALUE;
        for (Block trunk : new Block[] { Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_WOOD,
                WildscapesBlocks.CYPRESS_LOG, WildscapesBlocks.CYPRESS_WOOD }) {
            for (StructureTemplate.StructureBlockInfo info : template.filterBlocks(BlockPos.ZERO, raw, trunk, false)) {
                BlockPos pos = info.pos();
                if (pos.getY() > lowest) {
                    continue;
                }
                if (pos.getY() < lowest) {
                    lowest = pos.getY();
                    count = 0;
                    sumX = 0;
                    sumZ = 0;
                }
                count++;
                sumX += pos.getX();
                sumZ += pos.getZ();
            }
        }
        if (count == 0) {
            Vec3i size = template.getSize();
            return new BlockPos(size.getX() / 2, 0, size.getZ() / 2);
        }
        return new BlockPos(sumX / count, 0, sumZ / count);
    }
}
