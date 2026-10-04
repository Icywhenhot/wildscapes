package com.wildscapes.mixin;

import com.wildscapes.entity.SoulHarvest;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MeleeAttackGoal.class)
public abstract class MeleeAttackGoalMixin {
    @Shadow
    @Final
    protected PathfinderMob mob;

    @Shadow
    private int ticksUntilNextAttack;

    @Inject(method = "resetAttackCooldown", at = @At("TAIL"))
    private void empoweredSwing(CallbackInfo ci) {
        if (SoulHarvest.isEmpowered(mob)) {
            ticksUntilNextAttack = ticksUntilNextAttack * 4 / 5;
        }
    }
}
