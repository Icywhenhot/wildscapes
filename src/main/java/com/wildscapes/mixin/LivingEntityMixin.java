package com.wildscapes.mixin;

import com.wildscapes.effect.Intangibility;
import com.wildscapes.entity.SoulHarvest;
import com.wildscapes.item.WildscapesItems;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true)
    private float empoweredDamage(float amount, ServerLevel level, DamageSource source) {
        return SoulHarvest.damage(source, amount);
    }

    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"), cancellable = true)
    private void wildscapesDrops(ServerLevel level, DamageSource source, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.hasAttached(SoulHarvest.ORIGIN)) {
            ci.cancel();
        } else if (entity.getType() == EntityTypes.ILLUSIONER) {
            entity.spawnAtLocation(level, new ItemStack(WildscapesItems.ILLUSIONIST_NECKLACE));
        } else if (entity.getType() == EntityTypes.FROG) {
            entity.spawnAtLocation(level, new ItemStack(WildscapesItems.FROG_LEGS, 1 + entity.getRandom().nextInt(2)));
        }
    }


    @Inject(method = "hasLineOfSight(Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    private void intangibleSight(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Player player && Intangibility.isActive(player)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = {"isPushable", "canBeSeenByAnyone", "isInWall"}, at = @At("HEAD"), cancellable = true)
    private void intangiblePush(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Player player && Intangibility.isActive(player)) {
            cir.setReturnValue(false);
        }
    }
}
