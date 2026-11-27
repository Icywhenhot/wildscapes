package com.wildscapes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.wildscapes.Backports;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Raider.class)
public abstract class RaiderMixin {
    @WrapOperation(method = "die", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;removeEffectNoUpdate(Lnet/minecraft/world/effect/MobEffect;)Lnet/minecraft/world/effect/MobEffectInstance;"))
    private MobEffectInstance keepOmenForBottle(Player player, MobEffect effect, Operation<MobEffectInstance> original) {
        return Backports.hasBottle() ? original.call(player, effect) : null;
    }

    @WrapOperation(method = "die", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"))
    private boolean dropOminousBottle(Player player, MobEffectInstance omen, Operation<Boolean> original) {
        if (Backports.hasBottle()) {
            return original.call(player, omen);
        }
        Raider captain = (Raider) (Object) this;
        captain.spawnAtLocation(Backports.captainBottle(captain.getRandom()));
        return false;
    }
}
