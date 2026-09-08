package com.wildscapes.worldgen;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.wildscapes.Wildscapes;
import com.wildscapes.block.WildscapesBlocks;
import com.wildscapes.worldgen.TemplateTreeConfiguration.Ground;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
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
        addPillar(rules, Blocks.DARK_OAK_LOG, WildscapesBlocks.CYPRESS_LOG.get());
        addPillar(rules, Blocks.DARK_OAK_WOOD, WildscapesBlocks.CYPRESS_WOOD.get());
        addPillar(rules, Blocks.STRIPPED_DARK_OAK_LOG, WildscapesBlocks.STRIPPED_CYPRESS_LOG.get());
        addPillar(rules, Blocks.STRIPPED_DARK_OAK_WOOD, WildscapesBlocks.STRIPPED_CYPRESS_WOOD.get());

        BlockState leaves = WildscapesBlocks.CYPRESS_LEAVES.get().defaultBlockState()
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
        List<ResourceLocation> templates = context.config().templates();
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
        ResourceLocation id = templates.get(random.nextInt(templates.size()));
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

                .addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR)
                .addProcessor(SWAP_TO_CYPRESS);

        BlockPos corner = surface.above(context.config().yOffset()).subtract(anchor);
        if (!hasRoomFor(level, template, settings, corner, corner.offset(anchor))) {
            return false;
        }
        if (!template.placeInWorld(level, corner, corner, settings, random, Block.UPDATE_CLIENTS)) {
            return false;
        }
        repairVines(level, template, settings, corner);
        return true;
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
        return (below.is(BlockTags.DIRT) || below.is(Blocks.MUD)
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
                if (existing.is(BlockTags.LOGS) || existing.is(WildscapesBlocks.CYPRESS_LEAVES.get())) {
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
                WildscapesBlocks.CYPRESS_LOG.get(), WildscapesBlocks.CYPRESS_WOOD.get() }) {
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
