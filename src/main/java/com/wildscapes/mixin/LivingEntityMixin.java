package com.wildscapes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.wildscapes.WildscapesConfig;
import com.wildscapes.effect.Intangibility;
import com.wildscapes.entity.SoulHarvest;
import com.wildscapes.item.VanillaTweaks;
import com.wildscapes.item.WildscapesItems;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow
    protected abstract void dropExperience();

    @Inject(method = "hasLineOfSight", at = @At("HEAD"), cancellable = true)
    private void intangibleSight(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Player player && Intangibility.hiddenFrom(player, (Entity) (Object) this)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = {"isPushable", "canBeSeenByAnyone", "isInWall"}, at = @At("HEAD"), cancellable = true)
    private void intangiblePush(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Player player && Intangibility.isActive(player)) {
            cir.setReturnValue(false);
        }
    }

    @ModifyVariable(method = "hurt", at = @At("HEAD"), argsOnly = true)
    private float empoweredDamage(float amount, DamageSource source) {
        return SoulHarvest.damage(source, amount);
    }

    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"), cancellable = true)
    private void wildscapesDrops(DamageSource source, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!SoulHarvest.keepsDrops(entity)) {
            dropExperience();
            ci.cancel();
        } else if (entity.getType() == EntityType.ILLUSIONER) {
            if (entity.getRandom().nextDouble() < WildscapesConfig.get(WildscapesConfig.NECKLACE_DROP_CHANCE)) {
                entity.spawnAtLocation(new ItemStack(WildscapesItems.ILLUSIONIST_NECKLACE.get()));
            }
        } else if (entity.getType() == EntityType.FROG && WildscapesConfig.get(WildscapesConfig.FROG_LEGS)) {
            entity.spawnAtLocation(new ItemStack(WildscapesItems.FROG_LEGS.get(), 1 + entity.getRandom().nextInt(2)));
        }
    }

    @WrapOperation(method = "completeUsingItem", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;finishUsingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack fastFoodStacks(ItemStack stack, Level level, LivingEntity entity, Operation<ItemStack> original) {
        ItemStack eaten = stack.copy();
        return VanillaTweaks.finishEating(entity, eaten, original.call(stack, level, entity));
    }
}
