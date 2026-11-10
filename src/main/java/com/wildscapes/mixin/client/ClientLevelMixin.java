package com.wildscapes.mixin.client;

import com.wildscapes.Wildscapes;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
    @Inject(method = "tickNonPassenger", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;tick()V", shift = At.Shift.AFTER))
    private void entityTick(Entity entity, CallbackInfo ci) {
        Wildscapes.tickEntity(entity);
    }

    @Inject(method = "tickPassenger", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;rideTick()V", shift = At.Shift.AFTER))
    private void passengerTick(Entity vehicle, Entity entity, CallbackInfo ci) {
        Wildscapes.tickEntity(entity);
    }
}
