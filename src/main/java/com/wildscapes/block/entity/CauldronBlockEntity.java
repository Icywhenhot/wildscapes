package com.wildscapes.block.entity;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.wildscapes.block.CauldronSoups;
import com.wildscapes.block.WildscapesCauldronBlock;
import com.wildscapes.item.SoupContents;
import com.wildscapes.item.WildscapesDataComponents;
import com.wildscapes.item.WildscapesItems;
import com.wildscapes.sound.WildscapesSounds;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * State for an enhanced cauldron: plain or dyed water, a stored potion, or a magic soup being
 * brewed. The vanilla cauldron has no block entity, so {@link com.wildscapes.block.CauldronSwap}
 * swaps a real cauldron for {@link WildscapesCauldronBlock} (which carries this) whenever it
 * gains a heat source below or one of these enhanced states, and this entity reverts it once it
 * is a plain, cold cauldron again.
 */
public class CauldronBlockEntity extends BlockEntity {

    public enum Contents { EMPTY, WATER, POTION, BREW }

    /** Default vanilla water blue, used when the water is not dyed. */
    public static final int WATER_COLOR = 0x3F76E4;

    /** 30 seconds at 20 ticks/s. */
    public static final int MIX_DURATION = 600;

    public static final int MAX_LEVEL = 3;

    private Contents contents = Contents.EMPTY;
    private int fillLevel;
    /** RGB of dyed water, or -1 for plain water. */
    private int dyeColor = -1;
    private PotionContents potion = PotionContents.EMPTY;
    private boolean hasSoupBase;
    private final List<MobEffectInstance> soupEffects = new ArrayList<>();
    /** The ingredient dropped in and awaiting a stir, or empty. */
    private ItemStack pendingIngredient = ItemStack.EMPTY;
    private int mixTicks;

    private int ambientTimer;

    public CauldronBlockEntity(BlockPos pos, BlockState state) {
        super(WildscapesBlockEntities.CAULDRON.get(), pos, state);
    }

    // ---- Ticking ----------------------------------------------------------------

    public static void serverTick(Level level, BlockPos pos, BlockState state, CauldronBlockEntity be) {
        boolean heat = WildscapesCauldronBlock.hasHeatSourceBelow(level, pos);
        boolean boilingState = state.getValue(WildscapesCauldronBlock.BOILING);
        if (heat != boilingState) {
            level.setBlock(pos, state.setValue(WildscapesCauldronBlock.BOILING, heat), Block.UPDATE_ALL);
            if (heat) {
                level.playSound(null, pos, WildscapesSounds.CAULDRON_BOIL_START.get(), SoundSource.BLOCKS, 0.8F, 1.0F);
            } else {
                // Yanking the fire ruins an in-progress brew (accepted trade-off of the swap design).
                be.spoilBrew();
            }
        }

        if (be.mixTicks > 0) {
            be.mixTicks--;
            if (be.mixTicks == 0) {
                be.finishMixing((ServerLevel) level, pos);
            }
        }

        // While a finished soup sits ready, keep the villager-style sparkle going.
        if (heat && be.contents == Contents.BREW && !be.soupEffects.isEmpty()
                && be.pendingIngredient.isEmpty() && be.mixTicks == 0) {
            if (++be.ambientTimer >= 40) {
                be.ambientTimer = 0;
                ((ServerLevel) level).sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5, 2, 0.25, 0.05, 0.25, 0.0);
            }
        }

        be.tryRevert(level, pos, heat);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, CauldronBlockEntity be) {
        if (!state.getValue(WildscapesCauldronBlock.BOILING) || be.fillLevel <= 0) {
            return;
        }
        double surface = pos.getY() + 0.42 + 0.16 * be.fillLevel;
        var random = level.random;
        int puffs = be.mixTicks > 0 ? 3 : 1;
        for (int i = 0; i < puffs; i++) {
            double x = pos.getX() + 0.25 + random.nextDouble() * 0.5;
            double z = pos.getZ() + 0.25 + random.nextDouble() * 0.5;
            level.addParticle(ParticleTypes.BUBBLE_POP, x, surface, z, 0.0, 0.02, 0.0);
        }
        if (random.nextInt(4) == 0) {
            level.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5, surface + 0.1, pos.getZ() + 0.5,
                    0.0, 0.01, 0.0);
        }
    }

    private void finishMixing(ServerLevel level, BlockPos pos) {
        if (!pendingIngredient.isEmpty() && soupEffects.size() < CauldronSoups.MAX_EFFECTS) {
            MobEffectInstance effect = CauldronSoups.effectFor(pendingIngredient.getItem());
            if (effect != null) {
                soupEffects.add(effect);
            }
        }
        pendingIngredient = ItemStack.EMPTY;
        level.playSound(null, pos, WildscapesSounds.CAULDRON_SOUP_DONE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5,
                12, 0.3, 0.2, 0.3, 0.0);
        sync();
    }

    /** Reverts to a plain vanilla cauldron once it is cold and holds nothing special. */
    private void tryRevert(Level level, BlockPos pos, boolean heat) {
        if (heat) {
            return;
        }
        boolean plainWater = contents == Contents.WATER && dyeColor == -1;
        if (contents == Contents.EMPTY || plainWater) {
            WildscapesCauldronBlock.revertToVanilla(level, pos, contents == Contents.EMPTY ? 0 : fillLevel);
        }
    }

    // ---- Mutators (called from WildscapesCauldronBlock interactions) -------------

    public void setPlainWater(int newLevel) {
        contents = Contents.WATER;
        fillLevel = Math.min(newLevel, MAX_LEVEL);
        dyeColor = -1;
        potion = PotionContents.EMPTY;
        clearBrew();
        sync();
    }

    public void setDyedWater(int newLevel, int color) {
        contents = Contents.WATER;
        fillLevel = Math.min(newLevel, MAX_LEVEL);
        dyeColor = color;
        clearBrew();
        sync();
    }

    public void mixInDye(int color) {
        dyeColor = dyeColor == -1 ? color : average(dyeColor, color);
        sync();
    }

    /** Uses up one level of water (dyeing/washing leather, filling a bottle), emptying at zero. */
    public void drainWater() {
        if (--fillLevel <= 0) {
            reset();
        } else {
            sync();
        }
    }

    public boolean addPotion(PotionContents added) {
        if (contents == Contents.POTION) {
            if (fillLevel >= MAX_LEVEL || !samePotion(potion, added)) {
                return false;
            }
            fillLevel++;
        } else if (contents == Contents.EMPTY || fillLevel == 0) {
            contents = Contents.POTION;
            potion = added;
            fillLevel = 1;
            dyeColor = -1;
            clearBrew();
        } else {
            return false;
        }
        sync();
        return true;
    }

    /** Pours one level of the stored potion back into a bottle, emptying the cauldron if it hits zero. */
    public PotionContents takePotion() {
        PotionContents taken = potion;
        if (--fillLevel <= 0) {
            reset();
        }
        sync();
        return taken;
    }

    /** Nether wart turns boiling water into a soup base. */
    public void startSoupBase() {
        contents = Contents.BREW;
        hasSoupBase = true;
        dyeColor = -1;
        potion = PotionContents.EMPTY;
        sync();
    }

    public boolean addIngredient(ItemStack stack) {
        if (contents != Contents.BREW || !hasSoupBase || !pendingIngredient.isEmpty()
                || mixTicks > 0 || soupEffects.size() >= CauldronSoups.MAX_EFFECTS) {
            return false;
        }
        pendingIngredient = stack.copyWithCount(1);
        sync();
        return true;
    }

    public void startMixing() {
        mixTicks = MIX_DURATION;
        sync();
    }

    /** Scoops one level of finished soup into a bowl, emptying the cauldron when the last level goes. */
    public ItemStack scoopSoup() {
        ItemStack bowl = new ItemStack(WildscapesItems.MAGIC_SOUP.get());
        bowl.set(WildscapesDataComponents.SOUP_CONTENTS.get(), new SoupContents(List.copyOf(soupEffects)));
        if (--fillLevel <= 0) {
            reset();
        }
        sync();
        return bowl;
    }

    public void reset() {
        contents = Contents.EMPTY;
        fillLevel = 0;
        dyeColor = -1;
        potion = PotionContents.EMPTY;
        clearBrew();
        sync();
    }

    private void clearBrew() {
        hasSoupBase = false;
        soupEffects.clear();
        pendingIngredient = ItemStack.EMPTY;
        mixTicks = 0;
    }

    private void spoilBrew() {
        if (contents == Contents.BREW) {
            // Fall back to plain water at the same level; tryRevert then hands it to vanilla.
            contents = fillLevel > 0 ? Contents.WATER : Contents.EMPTY;
            dyeColor = -1;
            clearBrew();
            sync();
        }
    }

    // ---- Queries ----------------------------------------------------------------

    public Contents getContents() {
        return contents;
    }

    public int getFillLevel() {
        return fillLevel;
    }

    public boolean canAddIngredient() {
        return contents == Contents.BREW && hasSoupBase && pendingIngredient.isEmpty()
                && mixTicks == 0 && soupEffects.size() < CauldronSoups.MAX_EFFECTS;
    }

    public boolean isAwaitingStir() {
        return !pendingIngredient.isEmpty() && mixTicks == 0;
    }

    public boolean isMixing() {
        return mixTicks > 0;
    }

    public boolean hasSoupBase() {
        return hasSoupBase;
    }

    public boolean hasFinishedSoup() {
        return contents == Contents.BREW && !soupEffects.isEmpty();
    }

    public ItemStack getPendingIngredient() {
        return pendingIngredient;
    }

    public PotionContents getPotion() {
        return potion;
    }

    /** The colour the liquid surface should render, or -1 if there is no surface. */
    public int getSurfaceColor() {
        return switch (contents) {
            case EMPTY -> -1;
            case WATER -> dyeColor == -1 ? WATER_COLOR : dyeColor;
            case POTION -> potion.getColor();
            case BREW -> CauldronSoups.colorOf(soupEffects);
        };
    }

    private static boolean samePotion(PotionContents a, PotionContents b) {
        return a.potion().equals(b.potion());
    }

    private static int average(int a, int b) {
        int r = (((a >> 16) & 0xFF) + ((b >> 16) & 0xFF)) / 2;
        int g = (((a >> 8) & 0xFF) + ((b >> 8) & 0xFF)) / 2;
        int bl = ((a & 0xFF) + (b & 0xFF)) / 2;
        return (r << 16) | (g << 8) | bl;
    }

    // ---- Persistence & sync -----------------------------------------------------

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
        tag.putByte("Contents", (byte) contents.ordinal());
        tag.putByte("Level", (byte) fillLevel);
        tag.putInt("DyeColor", dyeColor);
        tag.putBoolean("HasBase", hasSoupBase);
        tag.putInt("MixTicks", mixTicks);
        if (!potion.equals(PotionContents.EMPTY)) {
            PotionContents.CODEC.encodeStart(ops, potion).result().ifPresent(t -> tag.put("Potion", t));
        }
        if (!pendingIngredient.isEmpty()) {
            tag.putString("Pending", pendingIngredient.getItem().builtInRegistryHolder().key().location().toString());
        }
        if (!soupEffects.isEmpty()) {
            MobEffectInstance.CODEC.listOf().encodeStart(ops, soupEffects).result()
                    .ifPresent(t -> tag.put("Effects", t));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
        contents = Contents.values()[tag.getByte("Contents")];
        fillLevel = tag.getByte("Level");
        dyeColor = tag.getInt("DyeColor");
        hasSoupBase = tag.getBoolean("HasBase");
        mixTicks = tag.getInt("MixTicks");
        potion = tag.contains("Potion")
                ? PotionContents.CODEC.parse(ops, tag.get("Potion")).result().orElse(PotionContents.EMPTY)
                : PotionContents.EMPTY;
        pendingIngredient = tag.contains("Pending")
                ? new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM
                        .get(net.minecraft.resources.ResourceLocation.parse(tag.getString("Pending"))))
                : ItemStack.EMPTY;
        soupEffects.clear();
        if (tag.contains("Effects")) {
            MobEffectInstance.CODEC.listOf().parse(ops, tag.get("Effects")).result()
                    .ifPresent(soupEffects::addAll);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
