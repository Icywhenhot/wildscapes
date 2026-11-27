package com.wildscapes.mixin;

import com.wildscapes.item.VanillaTweaks;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMixin {
    @Inject(method = "getUseDuration", at = @At("HEAD"), cancellable = true)
    private void eatTime(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (VanillaTweaks.isFastFood(stack)) {
            cir.setReturnValue(8);
        }
    }
}
