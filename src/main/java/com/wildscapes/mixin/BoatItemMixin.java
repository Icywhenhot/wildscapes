package com.wildscapes.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.wildscapes.item.CypressBoat;
import com.wildscapes.item.WildscapesItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BoatItem.class)
public abstract class BoatItemMixin {
    @ModifyExpressionValue(method = "getBoat", at = @At(value = "NEW", target = "(Lnet/minecraft/world/level/Level;DDD)Lnet/minecraft/world/entity/vehicle/Boat;"))
    private Boat cypressBoat(Boat boat, Level level, HitResult hit, ItemStack stack, Player player) {
        if (!stack.is(WildscapesItems.CYPRESS_BOAT)) return boat;
        Boat result = CypressBoat.BOAT.create(level);
        result.setPos(boat.position());
        return result;
    }

    @ModifyExpressionValue(method = "getBoat", at = @At(value = "NEW", target = "(Lnet/minecraft/world/level/Level;DDD)Lnet/minecraft/world/entity/vehicle/ChestBoat;"))
    private ChestBoat cypressChestBoat(ChestBoat boat, Level level, HitResult hit, ItemStack stack, Player player) {
        if (!stack.is(WildscapesItems.CYPRESS_CHEST_BOAT)) return boat;
        ChestBoat result = CypressBoat.CHEST_BOAT.create(level);
        result.setPos(boat.position());
        return result;
    }
}
