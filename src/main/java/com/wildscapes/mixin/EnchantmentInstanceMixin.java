package com.wildscapes.mixin;

import com.wildscapes.item.WildscapesEnchantments;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EnchantmentInstance.class)
public abstract class EnchantmentInstanceMixin {
    @Redirect(method = "<init>", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/enchantment/Enchantment$Rarity;getWeight()I"))
    private static int weight(Enchantment.Rarity rarity, Enchantment enchantment, int level) {
        return enchantment == WildscapesEnchantments.ELASTICITY.get() ? 3 : rarity.getWeight();
    }
}
