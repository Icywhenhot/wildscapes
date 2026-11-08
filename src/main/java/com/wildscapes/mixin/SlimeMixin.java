package com.wildscapes.mixin;

import com.wildscapes.entity.SwampSpawns;

import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slime.class)
public abstract class SlimeMixin {
    @Inject(method = "finalizeSpawn", at = @At("RETURN"))
    private void smallDaySlime(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            SpawnGroupData group, CallbackInfoReturnable<SpawnGroupData> cir) {
        SwampSpawns.sizeSlime((Slime) (Object) this, level, reason);
    }
}
