package com.wildscapes.mixin;

import com.wildscapes.block.CauldronSwap;
import com.wildscapes.block.MossCarpetSwap;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
    @Inject(method = "place", at = @At("RETURN"))
    private void swapPlacedBlock(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (cir.getReturnValue().consumesAction()) {
            CauldronSwap.onPlace(context.getLevel(), context.getClickedPos());
            MossCarpetSwap.onPlace(context.getLevel(), context.getClickedPos());
        }
    }
}
