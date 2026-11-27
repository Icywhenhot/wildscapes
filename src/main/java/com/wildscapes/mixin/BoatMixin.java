package com.wildscapes.mixin;

import com.wildscapes.block.WildscapesBlocks;
import com.wildscapes.entity.CypressBoat;
import com.wildscapes.entity.CypressChestBoat;

import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Boat.class)
public abstract class BoatMixin {
    @ModifyArg(method = "checkFallDamage", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/world/entity/vehicle/Boat;spawnAtLocation(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;"))
    private ItemLike planks(ItemLike drop) {
        return (Object) this instanceof CypressBoat || (Object) this instanceof CypressChestBoat
                ? WildscapesBlocks.CYPRESS_PLANKS.get() : drop;
    }
}
