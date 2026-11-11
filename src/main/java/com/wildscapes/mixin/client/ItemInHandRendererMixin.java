package com.wildscapes.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wildscapes.client.WitchCampMarker;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Redirect(method = "renderMap", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/MapItem;getSavedData(Ljava/lang/Integer;Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;"))
    private MapItemSavedData witchCamp(Integer id, Level level, PoseStack pose, MultiBufferSource buffer,
            int light, ItemStack stack) {
        var data = MapItem.getSavedData(id, level);
        WitchCampMarker.remember(stack, data);
        return data;
    }
}
