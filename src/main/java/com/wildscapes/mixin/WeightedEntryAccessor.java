package com.wildscapes.mixin;

import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WeightedEntry.IntrusiveBase.class)
public interface WeightedEntryAccessor {
    @Mutable
    @Accessor("weight")
    void wildscapes$setWeight(Weight weight);
}
