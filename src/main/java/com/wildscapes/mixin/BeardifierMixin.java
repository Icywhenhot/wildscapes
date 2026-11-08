package com.wildscapes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.wildscapes.worldgen.BuriedPiece;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Beardifier.class)
public abstract class BeardifierMixin {
    @WrapOperation(method = "forStructuresInChunk", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/structure/StructurePiece;isCloseToChunk(Lnet/minecraft/world/level/ChunkPos;I)Z"))
    private static boolean skipBuriedPiece(StructurePiece piece, ChunkPos pos, int distance, Operation<Boolean> original) {
        return !(piece instanceof BuriedPiece) && original.call(piece, pos, distance);
    }
}
