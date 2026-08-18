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

/**
 * Grows a tree that was built by hand in a world and saved with a structure block, rather than
 * assembled from voxel tables the way {@link CypressTreeFeature} does its four.
 *
 * <p>The saved {@code .nbt} goes in {@code data/wildscapes/structure/} and its name in the
 * {@code templates} list of a configured feature; one is picked at random per tree, then randomly
 * rotated and mirrored. Nothing needs editing inside the file: the dark oak a tree was drafted in
 * is swapped for cypress on the way into the world by {@link #SWAP_TO_CYPRESS}, so the structures
 * stay exactly as they were exported and can be re-exported at any time.
 *
 * <p>Air in the template is skipped rather than placed, so a tree settles into whatever it lands
 * among instead of stamping a box of air around itself. The bottom layer of the structure is what
 * lands at ground level, and the tree is centred on the lowest trunk block it can find, so a
 * structure should be saved starting at the first block of the trunk, with no ground underneath.
 */
public class TemplateTreeFeature extends Feature<TemplateTreeConfiguration> {

    /**
     * The block swap, applied as each block goes in. Logs are matched one state at a time so a
     * branch laid along an axis keeps that axis; vanilla applies the structure's own rotation to
     * whatever comes out of here, so a rotated tree still lines up. Leaves come out persistent,
     * since a hand-built canopy is rarely close enough to its trunk to survive decay checks.
     */
    private static final RuleProcessor SWAP_TO_CYPRESS = new RuleProcessor(buildRules());

    private static List<ProcessorRule> buildRules() {
        List<ProcessorRule> rules = new java.util.ArrayList<>();
        addPillar(rules, Blocks.DARK_OAK_LOG, WildscapesBlocks.CYPRESS_LOG.get());
        addPillar(rules, Blocks.DARK_OAK_WOOD, WildscapesBlocks.CYPRESS_WOOD.get());
        addPillar(rules, Blocks.STRIPPED_DARK_OAK_LOG, WildscapesBlocks.STRIPPED_CYPRESS_LOG.get());
        addPillar(rules, Blocks.STRIPPED_DARK_OAK_WOOD, WildscapesBlocks.STRIPPED_CYPRESS_WOOD.get());
        // Mangrove leaves are what the trees were actually drafted with; dark oak's are covered
        // too so a tree built either way comes out the same.
        BlockState leaves = WildscapesBlocks.CYPRESS_LEAVES.get().defaultBlockState()
                .setValue(LeavesBlock.PERSISTENT, true)
                .setValue(LeavesBlock.DISTANCE, 1);
        rules.add(new ProcessorRule(new BlockMatchTest(Blocks.MANGROVE_LEAVES), AlwaysTrueTest.INSTANCE, leaves));
        rules.add(new ProcessorRule(new BlockMatchTest(Blocks.DARK_OAK_LEAVES), AlwaysTrueTest.INSTANCE, leaves));
        return rules;
    }

    /** One rule per axis, so an east-west branch does not come out standing on end. */
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
        // The spot the placement modifiers picked is what gets judged; y_offset then shifts the
        // structure relative to it, so a tree can sink its ground layer into the terrain without
        // the footing check looking at the block it is about to replace.
        BlockPos surface = context.origin();
        if (!hasFooting(level, surface, context.config().ground())) {
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
                // Air is left out, so the canopy sits among the terrain rather than clearing it.
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR)
                .addProcessor(SWAP_TO_CYPRESS);

        // The pivot maps to itself, so putting the structure's corner here lands the anchor on
        // the spot the placement modifiers picked.
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

    /**
     * Gives a home to any vine that arrived without one.
     *
     * <p>A vine block records which faces it clings to, and a handful in these structures were
     * saved clinging to nothing at all — a state a vine normally only passes through on its way to
     * being broken. It has no model, so it renders as a hole in the middle of a drape rather than
     * as anything visible. Each one is re-hung the way it most likely was built: carrying on the
     * vine above it, otherwise gripping whatever solid neighbour it has, otherwise joining the
     * drape beside it. One with nowhere to go is cleared away rather than pinned to the ceiling,
     * since that is the state that renders as a flat sheet instead of hanging.
     */
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

        // Nothing to hang off. Clinging to the ceiling would be the only state left, and that one
        // renders as a flat sheet under the block above rather than as a hanging vine.
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

    /**
     * Somewhere for this kind of tree to stand. A land tree wants dry footing and something it
     * can push aside above it; a water one just wants water at the spot, so it never turns up on
     * the bank. Water is checked at the spot and one below so that it works whether the structure
     * is anchored down on the bed or up at the surface.
     */
    private static boolean hasFooting(WorldGenLevel level, BlockPos origin, Ground ground) {
        BlockState here = level.getBlockState(origin);
        BlockState below = level.getBlockState(origin.below());

        if (ground == Ground.WATER) {
            return here.getFluidState().is(FluidTags.WATER) || below.getFluidState().is(FluidTags.WATER);
        }
        return (below.is(BlockTags.DIRT) || below.is(Blocks.MUD)
                        || below.is(Blocks.CLAY) || below.is(Blocks.SAND))
                && !below.getFluidState().is(FluidTags.WATER)
                && (here.isAir() || here.canBeReplaced())
                && !here.getFluidState().is(FluidTags.WATER);
    }

    /** The blocks of a template that stake out ground: anything else may share space freely. */
    private static final Block[] SOLID_PARTS = {
            Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_WOOD, Blocks.MANGROVE_LEAVES, Blocks.DARK_OAK_LEAVES };

    /** How much room a trunk wants to itself, and how far up that is measured. */
    private static final int TRUNK_SPACING = 3;
    private static final int TRUNK_HEIGHT = 4;

    /**
     * Whether there is room here for this tree.
     *
     * <p>Two rules, because either alone lets trees mesh. Every spot the tree's own wood and
     * leaves would land on is checked against what is there, which stops one growing bodily
     * through another. On its own that is not enough: a tree turned a different way can thread
     * itself through the gaps in another's canopy without ever sharing a block, which is exactly
     * the tangle it is meant to prevent. So a trunk also wants {@link #TRUNK_SPACING} blocks to
     * itself — measured only over the first few blocks of its height, so that branches and
     * canopies higher up may still interlock the way a real wood does.
     *
     * <p>Vines, moss and undergrowth are ignored throughout: those are meant to overlap.
     */
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

    /**
     * The block of the template the tree hangs from: the middle of the lowest layer of trunk in
     * it, at y 0. Centring on the trunk rather than on the footprint keeps a lopsided canopy from
     * dragging the tree off the spot it was meant to grow on, and makes rotation turn it about
     * its own trunk.
     */
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
            // No trunk to speak of: fall back to the middle of the footprint.
            Vec3i size = template.getSize();
            return new BlockPos(size.getX() / 2, 0, size.getZ() / 2);
        }
        return new BlockPos(sumX / count, 0, sumZ / count);
    }
}
