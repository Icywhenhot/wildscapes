package com.wildscapes.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wildscapes.client.WitchCampMarker;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.client.gui.MapRenderer$MapInstance")
public abstract class MapInstanceMixin {
    @WrapOperation(method = "draw", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;getDecorations()Ljava/lang/Iterable;"))
    private Iterable<MapDecoration> witchCamp(MapItemSavedData data, Operation<Iterable<MapDecoration>> original,
            @Local(argsOnly = true) PoseStack pose, @Local(argsOnly = true) MultiBufferSource buffer,
            @Local(argsOnly = true, ordinal = 0) int light) {
        return WitchCampMarker.drawMarkers(data, original.call(data), pose, buffer, light);
    }
}
