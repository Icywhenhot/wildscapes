package com.wildscapes.worldgen;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class DryPoolElement extends SinglePoolElement {
    public static final Codec<DryPoolElement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            templateCodec(), processorsCodec(), projectionCodec(),
            Codec.BOOL.optionalFieldOf("ignore_air", false).forGetter(element -> element.ignoreAir)
    ).apply(instance, DryPoolElement::new));

    private final boolean ignoreAir;

    private DryPoolElement(Either<ResourceLocation, StructureTemplate> template,
            Holder<StructureProcessorList> processors, StructureTemplatePool.Projection projection, boolean ignoreAir) {
        super(template, processors, projection);
        this.ignoreAir = ignoreAir;
    }

    @Override
    protected StructurePlaceSettings getSettings(Rotation rotation, BoundingBox bounds, boolean keepJigsaws) {
        StructurePlaceSettings settings = super.getSettings(rotation, bounds, keepJigsaws).setKeepLiquids(false);
        if (ignoreAir) {
            settings.popProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
            settings.addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
        }
        return settings;
    }

    @Override
    public StructurePoolElementType<?> getType() {
        return WildscapesStructures.DRY_POOL_ELEMENT.get();
    }
}
