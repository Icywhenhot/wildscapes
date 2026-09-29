package com.wildscapes.worldgen;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.neoforged.neoforge.common.world.PieceBeardifierModifier;

public class BuriedPiece extends PoolElementStructurePiece implements PieceBeardifierModifier {
    public BuriedPiece(StructureTemplateManager templates, PoolElementStructurePiece piece, LiquidSettings liquids) {
        super(templates, piece.getElement(), piece.getPosition(), piece.getGroundLevelDelta(),
                piece.getRotation(), piece.getBoundingBox(), liquids);
        piece.getJunctions().forEach(this::addJunction);
    }

    public BuriedPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(context, tag);
    }

    @Override
    public StructurePieceType getType() {
        return WildscapesStructures.BURIED_PIECE.get();
    }

    @Override
    public BoundingBox getBeardifierBox() {
        return getBoundingBox();
    }

    @Override
    public TerrainAdjustment getTerrainAdjustment() {
        return TerrainAdjustment.NONE;
    }
}
