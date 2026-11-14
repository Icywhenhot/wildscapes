package com.wildscapes.mixin;

import com.wildscapes.item.WildscapesEnchantments;

import net.minecraft.util.random.Weight;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantmentInstance.class)
public abstract class EnchantmentInstanceMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void weight(Enchantment enchantment, int level, CallbackInfo ci) {
        if (enchantment == WildscapesEnchantments.ELASTICITY.get()) {
            ((WeightedEntryAccessor) this).wildscapes$setWeight(Weight.of(3));
        }
    }
}
