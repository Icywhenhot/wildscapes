package com.wildscapes.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wildscapes.client.WitchCampMarker;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.client.gui.MapRenderer$MapInstance")
public abstract class MapInstanceMixin {
    @Shadow
    private MapItemSavedData data;

    @Redirect(method = "draw", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/saveddata/maps/MapDecoration;render(I)Z", remap = false))
    private boolean witchCamp(MapDecoration icon, int index, PoseStack pose, MultiBufferSource buffer,
            boolean frame, int light) {
        return WitchCampMarker.render(data, icon, index, pose, buffer, light) || icon.render(index);
    }
}
