package com.wildscapes.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.wildscapes.item.CypressBoat;
import com.wildscapes.item.WildscapesItems;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.BoatDispenseItemBehavior;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BoatDispenseItemBehavior.class)
public abstract class BoatDispenserMixin {
    @ModifyExpressionValue(method = "execute", at = @At(value = "NEW", target = "(Lnet/minecraft/world/level/Level;DDD)Lnet/minecraft/world/entity/vehicle/Boat;"))
    private Boat cypressBoat(Boat boat, BlockSource source, ItemStack stack) {
        if (!stack.is(WildscapesItems.CYPRESS_BOAT)) return boat;
        Boat result = CypressBoat.BOAT.create(source.level());
        result.setPos(boat.position());
        return result;
    }

    @ModifyExpressionValue(method = "execute", at = @At(value = "NEW", target = "(Lnet/minecraft/world/level/Level;DDD)Lnet/minecraft/world/entity/vehicle/ChestBoat;"))
    private ChestBoat cypressChestBoat(ChestBoat boat, BlockSource source, ItemStack stack) {
        if (!stack.is(WildscapesItems.CYPRESS_CHEST_BOAT)) return boat;
        ChestBoat result = CypressBoat.CHEST_BOAT.create(source.level());
        result.setPos(boat.position());
        return result;
    }
}
