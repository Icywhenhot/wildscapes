package com.wildscapes.block;

import javax.annotation.Nullable;

import com.wildscapes.block.entity.CauldronBlockEntity;
import com.wildscapes.block.entity.WildscapesBlockEntities;
import com.wildscapes.item.WildscapesItems;
import com.wildscapes.sound.WildscapesSounds;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The enhanced-cauldron block. Players never craft or hold this — {@link CauldronSwap} swaps a
 * real {@code minecraft:cauldron} for it when the cauldron is heated or gains a dyed-water /
 * potion / soup state, and {@link CauldronBlockEntity} reverts it to vanilla once it is plain and
 * cold. It reimplements the vanilla water interactions plus the Bedrock-style ones (dyeing water
 * and leather, storing potions, tipping arrows) and the magic-soup brewing loop.
 */
public class WildscapesCauldronBlock extends Block implements EntityBlock {

    public static final BooleanProperty BOILING = BooleanProperty.create("boiling");

    private static final VoxelShape INSIDE = box(2.0, 4.0, 2.0, 14.0, 16.0, 14.0);
    private static final VoxelShape SHAPE = Shapes.join(Shapes.block(),
            Shapes.or(box(0.0, 0.0, 4.0, 16.0, 3.0, 12.0), box(4.0, 0.0, 0.0, 12.0, 3.0, 16.0),
                    box(2.0, 0.0, 2.0, 14.0, 3.0, 14.0), INSIDE),
            BooleanOp.ONLY_FIRST);

    public WildscapesCauldronBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(BOILING, false));
    }

    // ---- Heat / swap helpers ----------------------------------------------------

    public static boolean hasHeatSourceBelow(Level level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        if (below.is(WildscapesBlocks.BONFIRE.get())) {
            return true;
        }
        return below.getBlock() instanceof CampfireBlock && below.getValue(CampfireBlock.LIT);
    }

    /** Turns this position back into the matching vanilla cauldron. */
    public static void revertToVanilla(Level level, BlockPos pos, int waterLevel) {
        BlockState vanilla = waterLevel <= 0
                ? Blocks.CAULDRON.defaultBlockState()
                : Blocks.WATER_CAULDRON.defaultBlockState()
                        .setValue(LayeredCauldronBlock.LEVEL, Math.min(waterLevel, CauldronBlockEntity.MAX_LEVEL));
        level.setBlock(pos, vanilla, Block.UPDATE_ALL);
    }

    // ---- Interactions -----------------------------------------------------------

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof CauldronBlockEntity be)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        Item item = stack.getItem();
        CauldronBlockEntity.Contents contents = be.getContents();
        boolean boiling = state.getValue(BOILING);

        // --- Magic soup brewing ---
        if (item == CauldronSoups.BASE_INGREDIENT && boiling
                && contents == CauldronBlockEntity.Contents.WATER && !be.hasSoupBase()) {
            return act(level, () -> {
                be.startSoupBase();
                consume(player, stack);
                sfx(level, pos, WildscapesSounds.CAULDRON_ADD_INGREDIENT.get(), stack);
            });
        }
        if (CauldronSoups.isEffectIngredient(item) && be.canAddIngredient()) {
            return act(level, () -> {
                be.addIngredient(stack);
                consume(player, stack);
                sfx(level, pos, WildscapesSounds.CAULDRON_ADD_INGREDIENT.get(), stack);
            });
        }
        if (item == WildscapesItems.LADLE.get() && be.isAwaitingStir()) {
            return act(level, () -> {
                be.startMixing();
                stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND
                        ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                level.playSound(null, pos, WildscapesSounds.CAULDRON_MIX.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            });
        }
        if (item == Items.BOWL && be.hasFinishedSoup() && !be.isMixing() && be.getPendingIngredient().isEmpty()) {
            return act(level, () -> {
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, be.scoopSoup()));
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            });
        }

        // --- Water ---
        if (isWaterBottle(stack)) {
            if ((contents == CauldronBlockEntity.Contents.EMPTY
                    || (contents == CauldronBlockEntity.Contents.WATER && be.getSurfaceColor() == CauldronBlockEntity.WATER_COLOR))
                    && be.getFillLevel() < CauldronBlockEntity.MAX_LEVEL) {
                return act(level, () -> {
                    be.setPlainWater(be.getFillLevel() + 1);
                    player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
                    level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                });
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (item == Items.WATER_BUCKET && contents != CauldronBlockEntity.Contents.POTION) {
            return act(level, () -> {
                be.setPlainWater(CauldronBlockEntity.MAX_LEVEL);
                if (!player.getAbilities().instabuild) {
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                }
                level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            });
        }
        if (item == Items.BUCKET && contents == CauldronBlockEntity.Contents.WATER
                && be.getFillLevel() == CauldronBlockEntity.MAX_LEVEL && be.getSurfaceColor() == CauldronBlockEntity.WATER_COLOR) {
            return act(level, () -> {
                be.reset();
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.WATER_BUCKET)));
                level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            });
        }

        // --- Bedrock dye / leather ---
        if (item instanceof DyeItem dye && contents == CauldronBlockEntity.Contents.WATER) {
            return act(level, () -> {
                be.mixInDye(dye.getDyeColor().getTextureDiffuseColor() & 0xFFFFFF);
                consume(player, stack);
                level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            });
        }
        if (isDyeable(stack) && contents == CauldronBlockEntity.Contents.WATER && be.getFillLevel() > 0) {
            boolean dyed = be.getSurfaceColor() != CauldronBlockEntity.WATER_COLOR;
            return act(level, () -> {
                if (dyed) {
                    stack.set(DataComponents.DYED_COLOR, new DyedItemColor(be.getSurfaceColor(), true));
                } else {
                    stack.remove(DataComponents.DYED_COLOR);
                }
                be.drainWater();
                level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            });
        }

        // --- Bedrock potions / tipped arrows ---
        if (isStorablePotion(stack)) {
            PotionContents pc = stack.get(DataComponents.POTION_CONTENTS);
            if (pc != null && be.addPotion(pc)) {
                return act(level, () -> {
                    player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
                    level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                });
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (item == Items.GLASS_BOTTLE && contents == CauldronBlockEntity.Contents.POTION) {
            return act(level, () -> {
                ItemStack bottle = new ItemStack(Items.POTION);
                bottle.set(DataComponents.POTION_CONTENTS, be.takePotion());
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, bottle));
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            });
        }
        if (item == Items.GLASS_BOTTLE && contents == CauldronBlockEntity.Contents.WATER && be.getFillLevel() > 0) {
            return act(level, () -> {
                ItemStack bottle = new ItemStack(Items.POTION);
                bottle.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.WATER));
                be.drainWater();
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, bottle));
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            });
        }
        if (item == Items.ARROW && contents == CauldronBlockEntity.Contents.POTION
                && !be.getPotion().equals(PotionContents.EMPTY)) {
            int perLevel = 16;
            int tippable = Math.min(stack.getCount(), be.getFillLevel() * perLevel);
            if (tippable <= 0) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            return act(level, () -> {
                int levelsUsed = Math.max(1, (tippable + perLevel - 1) / perLevel);
                ItemStack tipped = new ItemStack(Items.TIPPED_ARROW, tippable);
                tipped.set(DataComponents.POTION_CONTENTS, be.getPotion());
                stack.shrink(tippable);
                for (int i = 0; i < levelsUsed; i++) {
                    be.takePotion();
                }
                if (!player.getInventory().add(tipped)) {
                    player.drop(tipped, false);
                }
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            });
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private interface CauldronAction {
        void run();
    }

    /** Runs the mutation server-side only, returning the sided result so the arm still swings on the client. */
    private static ItemInteractionResult act(Level level, CauldronAction action) {
        if (!level.isClientSide) {
            action.run();
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void consume(Player player, ItemStack stack) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    private static void sfx(Level level, BlockPos pos, net.minecraft.sounds.SoundEvent sound, ItemStack ingredient) {
        level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
        if (level instanceof ServerLevel server) {
            server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, ingredient),
                    pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 8, 0.2, 0.1, 0.2, 0.05);
            server.sendParticles(ParticleTypes.SPLASH,
                    pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 8, 0.2, 0.05, 0.2, 0.0);
        }
    }

    private static boolean isDyeable(ItemStack stack) {
        return stack.is(Items.LEATHER_HELMET) || stack.is(Items.LEATHER_CHESTPLATE)
                || stack.is(Items.LEATHER_LEGGINGS) || stack.is(Items.LEATHER_BOOTS)
                || stack.is(Items.LEATHER_HORSE_ARMOR) || stack.is(Items.WOLF_ARMOR);
    }

    private static boolean isWaterBottle(ItemStack stack) {
        if (stack.getItem() != Items.POTION) {
            return false;
        }
        PotionContents pc = stack.get(DataComponents.POTION_CONTENTS);
        return pc != null && pc.is(Potions.WATER);
    }

    private static boolean isStorablePotion(ItemStack stack) {
        if (stack.getItem() != Items.POTION && stack.getItem() != Items.SPLASH_POTION
                && stack.getItem() != Items.LINGERING_POTION) {
            return false;
        }
        return !isWaterBottle(stack);
    }

    // ---- Block entity wiring ----------------------------------------------------

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CauldronBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return ticker(type, WildscapesBlockEntities.CAULDRON.get(),
                level.isClientSide ? CauldronBlockEntity::clientTick : CauldronBlockEntity::serverTick);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private static <A extends BlockEntity, B extends BlockEntity> BlockEntityTicker<A> ticker(
            BlockEntityType<A> given, BlockEntityType<B> expected, BlockEntityTicker<? super B> ticker) {
        return expected == given ? (BlockEntityTicker<A>) ticker : null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BOILING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.block();
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof CauldronBlockEntity be ? be.getFillLevel() : 0;
    }
}
