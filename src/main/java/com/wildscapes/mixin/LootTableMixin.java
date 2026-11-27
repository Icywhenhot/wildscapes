package com.wildscapes.mixin;

import java.util.function.Consumer;

import com.wildscapes.Backports;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LootTable.class)
public abstract class LootTableMixin {
    @ModifyVariable(method = "getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V",
            at = @At("HEAD"), argsOnly = true)
    private Consumer<ItemStack> swapBackportedItems(Consumer<ItemStack> output, LootContext context) {
        return stack -> output.accept(Backports.swapLoot(stack));
    }
}
