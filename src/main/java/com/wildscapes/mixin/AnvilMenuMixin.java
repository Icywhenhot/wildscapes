package com.wildscapes.mixin;

import com.wildscapes.item.WildscapesEnchantments;

import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {
    @Unique
    private boolean wildscapes$slimeSnare;

    @Redirect(method = "createResult", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/enchantment/Enchantment;getRarity()Lnet/minecraft/world/item/enchantment/Enchantment$Rarity;"))
    private Enchantment.Rarity rarity(Enchantment enchantment) {
        wildscapes$slimeSnare = enchantment == WildscapesEnchantments.SLIME_SNARE.get();
        return enchantment.getRarity();
    }

    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 8))
    private int cost(int value) {
        return wildscapes$slimeSnare ? 6 : value;
    }
}
