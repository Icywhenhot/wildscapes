package com.wildscapes.mixin;

import java.util.List;

import com.wildscapes.item.MirelashItem;
import com.wildscapes.item.WildscapesEnchantments;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
    @Inject(method = "getAvailableEnchantmentResults", at = @At("RETURN"))
    private static void mirelashTable(int power, ItemStack stack, boolean treasure,
            CallbackInfoReturnable<List<EnchantmentInstance>> cir) {
        if (stack.getItem() instanceof MirelashItem mirelash) {
            cir.getReturnValue().removeIf(entry -> !mirelash.canApplyAtEnchantingTable(stack, entry.enchantment));
        } else {
            cir.getReturnValue().removeIf(entry -> WildscapesEnchantments.onlyForMirelash(entry.enchantment));
        }
    }
}
