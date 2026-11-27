package com.wildscapes.item;

import java.util.List;
import java.util.OptionalInt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;

public record StoredPotion(Potion potion, List<MobEffectInstance> effects, OptionalInt color) {
    public static final StoredPotion EMPTY = new StoredPotion(Potions.EMPTY);

    public StoredPotion(Potion potion) {
        this(potion, List.of(), OptionalInt.empty());
    }

    public static StoredPotion read(ItemStack stack) {
        return stack.hasTag() ? load(stack.getTag()) : EMPTY;
    }

    public static StoredPotion load(CompoundTag tag) {
        return new StoredPotion(PotionUtils.getPotion(tag), List.copyOf(PotionUtils.getCustomEffects(tag)),
                tag.contains(PotionUtils.TAG_CUSTOM_POTION_COLOR, Tag.TAG_ANY_NUMERIC)
                        ? OptionalInt.of(tag.getInt(PotionUtils.TAG_CUSTOM_POTION_COLOR)) : OptionalInt.empty());
    }

    public CompoundTag save() {
        ItemStack stack = new ItemStack(Items.POTION);
        write(stack);
        return stack.getOrCreateTag().copy();
    }

    public void write(ItemStack stack) {
        PotionUtils.setPotion(stack, potion);
        stack.removeTagKey(PotionUtils.TAG_CUSTOM_POTION_EFFECTS);
        PotionUtils.setCustomEffects(stack, effects);
        if (color.isPresent()) {
            stack.getOrCreateTag().putInt(PotionUtils.TAG_CUSTOM_POTION_COLOR, color.getAsInt());
        } else {
            stack.removeTagKey(PotionUtils.TAG_CUSTOM_POTION_COLOR);
        }
    }

    public boolean is(Potion type) {
        return potion == type;
    }

    public int getColor() {
        return color.orElseGet(() -> potion == Potions.EMPTY && effects.isEmpty() ? 16253176
                : PotionUtils.getColor(PotionUtils.getAllEffects(potion, effects)));
    }
}
