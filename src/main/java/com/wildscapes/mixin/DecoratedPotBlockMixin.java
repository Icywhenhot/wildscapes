package com.wildscapes.mixin;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DecoratedPotBlock;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DecoratedPotBlock.class)
public abstract class DecoratedPotBlockMixin {
    @Inject(method = "getDrops", at = @At("RETURN"), cancellable = true)
    private void cellarLoot(BlockState state, LootParams.Builder params, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (!(params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof DecoratedPotBlockEntity pot)) {
            return;
        }
        var data = pot.getPersistentData();
        String table = data.getString("wildscapes:loot_table");
        ItemStack contents = ItemStack.of(data.getCompound("wildscapes:stored_item"));
        if (table.isEmpty() && contents.isEmpty()) {
            return;
        }
        List<ItemStack> drops = new ArrayList<>(cir.getReturnValue());
        if (!contents.isEmpty()) {
            drops.add(contents);
        }
        ResourceLocation id = table.isEmpty() ? null : ResourceLocation.tryParse(table);
        if (id != null) {
            var level = params.getLevel();
            var loot = new LootParams.Builder(level)
                    .withParameter(LootContextParams.ORIGIN, params.getParameter(LootContextParams.ORIGIN))
                    .withOptionalParameter(LootContextParams.THIS_ENTITY, params.getOptionalParameter(LootContextParams.THIS_ENTITY))
                    .create(LootContextParamSets.CHEST);
            drops.addAll(level.getServer().getLootData().getLootTable(id).getRandomItems(loot));
        }
        cir.setReturnValue(drops);
    }
}
