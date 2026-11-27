package com.wildscapes.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wildscapes.client.WitchCampMarker;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @ModifyVariable(method = "renderMap", ordinal = 0, at = @At("STORE"))
    private MapItemSavedData witchCamp(MapItemSavedData data, PoseStack pose, MultiBufferSource buffer,
            int light, ItemStack stack) {
        WitchCampMarker.remember(stack, data);
        return data;
    }
}
