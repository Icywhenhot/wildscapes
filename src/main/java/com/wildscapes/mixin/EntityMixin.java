package com.wildscapes.mixin;

import java.util.List;

import com.wildscapes.effect.Intangibility;
import com.wildscapes.entity.WildscapesEntityData;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin implements WildscapesEntityData {
    @Unique
    private CompoundTag wildscapes$data = new CompoundTag();

    @Override
    public CompoundTag wildscapes$data() {
        return wildscapes$data;
    }

    @Inject(method = "saveWithoutId", at = @At("TAIL"))
    private void saveWildscapesData(ValueOutput output, CallbackInfo ci) {
        if (!wildscapes$data.isEmpty()) {
            output.store("wildscapes_data", CompoundTag.CODEC, wildscapes$data);
        }
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void loadWildscapesData(ValueInput input, CallbackInfo ci) {
        wildscapes$data = input.read("wildscapes_data", CompoundTag.CODEC)
                .or(() -> input.read("NeoForgeData", CompoundTag.CODEC)).orElseGet(CompoundTag::new);
    }

    @Shadow
    protected Vec3 stuckSpeedMultiplier;

    @Inject(method = "move", at = @At("HEAD"))
    private void intangibleStuck(MoverType type, Vec3 movement, CallbackInfo ci) {
        if ((Object) this instanceof Player player && Intangibility.isActive(player)) {
            stuckSpeedMultiplier = Vec3.ZERO;
        }
    }

    @Inject(method = "checkInsideBlocks(Ljava/util/List;Lnet/minecraft/world/entity/InsideBlockEffectApplier$StepBasedCollector;)V", at = @At("HEAD"), cancellable = true)
    private void intangibleBlocks(CallbackInfo ci) {
        if ((Object) this instanceof Player player && Intangibility.isActive(player)) {
            ci.cancel();
        }
    }

    @Inject(method = "collide", at = @At("HEAD"), cancellable = true)
    private void intangibleCollision(Vec3 movement, CallbackInfoReturnable<Vec3> cir) {
        Entity entity = (Entity) (Object) this;
        if (!(entity instanceof Player player) || !Intangibility.isActive(player)) {
            return;
        }

        Vec3 fall = Entity.collideBoundingBox(entity, new Vec3(0.0, Math.min(movement.y, 0.0), 0.0),
                entity.getBoundingBox(), entity.level(), List.of());
        cir.setReturnValue(new Vec3(movement.x, movement.y < 0.0 ? fall.y : movement.y, movement.z));
    }
    @Inject(method = "canBeHitByProjectile", at = @At("HEAD"), cancellable = true)
    private void intangibleProjectile(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Player player && Intangibility.isActive(player)) {
            cir.setReturnValue(false);
        }
    }
}
