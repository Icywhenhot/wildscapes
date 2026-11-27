package com.wildscapes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.wildscapes.Backports;
import com.wildscapes.Wildscapes;
import com.wildscapes.entity.WildscapesEntityData;

import java.util.stream.Stream;
import com.wildscapes.block.CauldronSwap;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Inject(method = "updateNeighborsAt", at = @At("TAIL"))
    private void checkCauldronHeat(BlockPos pos, Block source, CallbackInfo ci) {
        CauldronSwap.onNeighborNotify((ServerLevel) (Object) this, pos);
    }

    @Inject(method = "addEntity", at = @At("HEAD"), cancellable = true)
    private void freshEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (Backports.replaceOnJoin((ServerLevel) (Object) this, entity)) {
            cir.setReturnValue(false);
            return;
        }
        ((WildscapesEntityData) entity).wildscapes$markFresh();
    }

    @ModifyVariable(method = "addWorldGenChunkEntities", at = @At("HEAD"), argsOnly = true)
    private Stream<Entity> freshWorldGenEntities(Stream<Entity> entities) {
        ServerLevel level = (ServerLevel) (Object) this;
        return entities.filter(entity -> !Backports.replaceOnJoin(level, entity))
                .peek(entity -> ((WildscapesEntityData) entity).wildscapes$markFresh());
    }

    @WrapOperation(method = "tickNonPassenger", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;tick()V"))
    private void tick(Entity entity, Operation<Void> original) {
        Wildscapes.beforeEntityTick(entity);
        original.call(entity);
        Wildscapes.tickEntity(entity);
    }

    @WrapOperation(method = "tickPassenger", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;rideTick()V"))
    private void rideTick(Entity entity, Operation<Void> original) {
        Wildscapes.beforeEntityTick(entity);
        original.call(entity);
        Wildscapes.tickEntity(entity);
    }
}
