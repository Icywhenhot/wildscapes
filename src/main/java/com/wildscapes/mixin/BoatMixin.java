package com.wildscapes.mixin;

import com.wildscapes.block.WildscapesBlocks;
import com.wildscapes.entity.CypressBoat;
import com.wildscapes.entity.CypressChestBoat;

import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Boat.class)
public abstract class BoatMixin {
    @Redirect(method = "checkFallDamage", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/vehicle/Boat$Type;getPlanks()Lnet/minecraft/world/level/block/Block;"))
    private Block planks(Boat.Type type) {
        return (Object) this instanceof CypressBoat || (Object) this instanceof CypressChestBoat
                ? WildscapesBlocks.CYPRESS_PLANKS.get() : type.getPlanks();
    }
}
