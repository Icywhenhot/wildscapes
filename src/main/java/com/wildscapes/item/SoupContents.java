package com.wildscapes.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;

public record SoupContents(List<MobEffectInstance> effects) {
    public static final SoupContents EMPTY = new SoupContents(List.of());

    private static final String KEY = "wildscapes:soup_contents";

    public static SoupContents read(ItemStack stack) {
        return stack.hasTag() ? load(stack.getTag().getList(KEY, Tag.TAG_COMPOUND)) : EMPTY;
    }

    public static SoupContents load(ListTag tag) {
        List<MobEffectInstance> effects = new ArrayList<>();
        for (int i = 0; i < tag.size(); i++) {
            MobEffectInstance effect = MobEffectInstance.load(tag.getCompound(i));
            if (effect != null) {
                effects.add(effect);
            }
        }
        return new SoupContents(List.copyOf(effects));
    }

    public ListTag save() {
        ListTag tag = new ListTag();
        for (MobEffectInstance effect : effects) {
            tag.add(effect.save(new CompoundTag()));
        }
        return tag;
    }

    public void write(ItemStack stack) {
        stack.getOrCreateTag().put(KEY, save());
    }

    public boolean isEmpty() {
        return effects.isEmpty();
    }
}
