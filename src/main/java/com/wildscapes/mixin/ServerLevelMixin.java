package com.wildscapes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.wildscapes.block.PavShrooms;
import com.wildscapes.block.CauldronSwap;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.redstone.Orientation;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.wildscapes.effect.Intangibility;
import com.wildscapes.effect.MirageSync;
import com.wildscapes.entity.JumpEnchantments;
import com.wildscapes.entity.SlimeMerging;
import com.wildscapes.entity.SoulHarvest;
import com.wildscapes.entity.WitchGoals;
import com.wildscapes.item.MirelashItem;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Inject(method = "updateNeighborsAt(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/level/redstone/Orientation;)V", at = @At("TAIL"))
    private void checkCauldronHeat(BlockPos pos, Block source, Orientation orientation, CallbackInfo ci) {
        CauldronSwap.onNeighborNotify((ServerLevel) (Object) this, pos);
    }


    @WrapOperation(method = "tickNonPassenger", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;tick()V"))
    private void tick(Entity entity, Operation<Void> original) {
        tickEntity(entity, original);
    }

    @WrapOperation(method = "tickPassenger", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;rideTick()V"))
    private void rideTick(Entity entity, Operation<Void> original) {
        tickEntity(entity, original);
    }

    @Unique
    private static void tickEntity(Entity entity, Operation<Void> original) {
        Intangibility.onMobTick(entity);
        if (entity instanceof Player player) {
            SoulHarvest.onPlayerTick(player);
        }
        original.call(entity);
        if (entity instanceof Player player) {
            Intangibility.onPlayerTick(player);
            MirelashItem.onPlayerTick(player);
            JumpEnchantments.onTick(player);
            PavShrooms.onTick(player);
        }
        WitchGoals.onEntityTick(entity);
        MirageSync.onEntityTick(entity);
        SlimeMerging.onEntityTick(entity);
    }
}
