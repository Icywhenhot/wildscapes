package com.wildscapes.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wildscapes.client.WitchCampMarker;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(targets = "net.minecraft.client.gui.MapRenderer$MapInstance")
public abstract class MapInstanceMixin {
    @Shadow
    private MapItemSavedData data;

    @ModifyVariable(method = "draw", ordinal = 0, at = @At("STORE"))
    private MapDecoration witchCamp(MapDecoration icon, PoseStack pose, MultiBufferSource buffer, boolean frame,
            int light) {
        return WitchCampMarker.wrap(data, icon, pose, buffer, light);
    }
}
