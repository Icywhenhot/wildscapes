package com.wildscapes.mixin;

import com.wildscapes.block.entity.PotData;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DecoratedPotBlockEntity.class)
public abstract class DecoratedPotBlockEntityMixin implements PotData {
    @Unique
    private CompoundTag wildscapes$data = new CompoundTag();

    @Override
    public CompoundTag wildscapes$data() {
        return wildscapes$data;
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void loadPotData(CompoundTag tag, CallbackInfo ci) {
        wildscapes$data = tag.getCompound("ForgeData");
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void savePotData(CompoundTag tag, CallbackInfo ci) {
        if (!wildscapes$data.isEmpty()) {
            tag.put("ForgeData", wildscapes$data);
        }
    }
}
