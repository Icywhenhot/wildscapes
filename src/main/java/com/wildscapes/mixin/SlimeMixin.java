package com.wildscapes.mixin;

import com.wildscapes.entity.SlimeMerging;
import com.wildscapes.entity.SwampSpawns;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slime.class)
public abstract class SlimeMixin {
    @Inject(method = "getDimensions", at = @At("RETURN"), cancellable = true)
    private void mergeSize(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        cir.setReturnValue(cir.getReturnValue().scale(SlimeMerging.scale((Slime) (Object) this)));
    }

    @Inject(method = "finalizeSpawn", at = @At("HEAD"), cancellable = true)
    private void smallDaySlime(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            SpawnGroupData group, CompoundTag tag, CallbackInfoReturnable<SpawnGroupData> cir) {
        if (!SwampSpawns.keepsSpawnSize((Slime) (Object) this, level, reason)) {
            cir.setReturnValue(group);
        }
    }
}
