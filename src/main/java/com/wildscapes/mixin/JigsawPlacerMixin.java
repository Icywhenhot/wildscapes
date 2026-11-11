package com.wildscapes.mixin;

import com.wildscapes.worldgen.SwampVillageStructure;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement$Placer")
public abstract class JigsawPlacerMixin {
    @Inject(method = "readPoolName", at = @At("RETURN"), cancellable = true)
    private static void villagePool(StructureTemplate.StructureBlockInfo block,
            CallbackInfoReturnable<ResourceKey<StructureTemplatePool>> cir) {
        var pool = cir.getReturnValue();
        var resolved = SwampVillageStructure.resolvePool(pool);
        if (resolved != pool) {
            cir.setReturnValue(resolved);
        }
    }
}
