package com.wildscapes.mixin.client;

import com.wildscapes.client.WitchCampMarker;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MapItem.class)
public abstract class MapItemMixin {
    @Inject(method = "getSavedData(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;",
            at = @At("RETURN"))
    private static void witchCamp(ItemStack stack, Level level, CallbackInfoReturnable<MapItemSavedData> cir) {
        if (level.isClientSide) {
            WitchCampMarker.remember(stack, cir.getReturnValue());
        }
    }
}
