package com.wildscapes.mixin;

import com.wildscapes.effect.Intangibility;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerMixin {
    @Shadow
    public ServerPlayer player;

    @Inject(method = "isPlayerCollidingWithAnythingNew", at = @At("HEAD"), cancellable = true)
    private void intangibleMove(LevelReader level, AABB oldBox, double x, double y, double z,
            CallbackInfoReturnable<Boolean> cir) {
        if (Intangibility.isActive(player)) {
            cir.setReturnValue(false);
        }
    }
}
