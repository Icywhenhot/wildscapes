package com.wildscapes.block;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Recolours a mud brick block in place when it is right-clicked with a dye.
 *
 * <p>This is handled as an interaction event rather than in a block class so it also
 * covers the four vanilla mud brick blocks, which we cannot change. Dyeing works in
 * both directions and between colours: any member of a family plus any dye gives that
 * colour, matching how the crafting recipes are tagged.
 */
public final class MudBrickDyeing {
    private MudBrickDyeing() {}

    /** Every dyeable block, mapped to the colour-keyed family it belongs to. */
    private static final Map<Block, Map<DyeColor, Block>> FAMILIES = new HashMap<>();

    /**
     * Fills in {@link #FAMILIES}. Must run after registration — {@code Wildscapes} calls
     * this from common setup.
     */
    public static void buildLookup() {
        FAMILIES.clear();
        addFamily(Blocks.MUD_BRICKS, MudBrickBlocks.BRICKS);
        addFamily(MudBrickBlocks.CHISELED_MUD_BRICKS.get(), MudBrickBlocks.CHISELED);
        addFamily(Blocks.MUD_BRICK_STAIRS, MudBrickBlocks.STAIRS);
        addFamily(Blocks.MUD_BRICK_SLAB, MudBrickBlocks.SLABS);
        addFamily(Blocks.MUD_BRICK_WALL, MudBrickBlocks.WALLS);
    }

    private static void addFamily(Block undyed, Map<DyeColor, ? extends DeferredBlock<? extends Block>> dyed) {
        Map<DyeColor, Block> byColor = new EnumMap<>(DyeColor.class);
        dyed.forEach((color, block) -> byColor.put(color, block.get()));

        // The undyed block and all 16 dyed ones share one family, so any of them can be
        // dyed into any other.
        FAMILIES.put(undyed, byColor);
        byColor.values().forEach(block -> FAMILIES.put(block, byColor));
    }

    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof DyeItem dye)) {
            return;
        }

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState dyed = recolour(level.getBlockState(pos), dye.getDyeColor());
        if (dyed == null) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
        if (level.isClientSide) {
            return;
        }

        level.setBlockAndUpdate(pos, dyed);
        level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        if (!event.getEntity().getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    /**
     * The given state in the requested colour, or null if the block is not dyeable or is
     * already that colour.
     */
    private static BlockState recolour(BlockState state, DyeColor color) {
        Map<DyeColor, Block> family = FAMILIES.get(state.getBlock());
        if (family == null) {
            return null;
        }
        Block target = family.get(color);
        if (target == null || target == state.getBlock()) {
            return null;
        }

        // Carry over facing, half, shape, slab type, wall connections and waterlogging so
        // the block keeps its orientation and does not pop its water.
        BlockState dyed = target.defaultBlockState();
        for (Property<?> property : state.getProperties()) {
            dyed = copyProperty(state, dyed, property);
        }
        return dyed;
    }

    private static <T extends Comparable<T>> BlockState copyProperty(BlockState from, BlockState to,
            Property<T> property) {
        return to.hasProperty(property) ? to.setValue(property, from.getValue(property)) : to;
    }
}
