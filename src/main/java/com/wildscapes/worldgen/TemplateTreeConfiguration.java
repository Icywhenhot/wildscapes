package com.wildscapes.worldgen;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * What a {@link TemplateTreeFeature} may grow and where.
 *
 * <p>{@code templates} names the structures, {@code wildscapes:swamp_oak_a} meaning
 * {@code data/wildscapes/structure/swamp_oak_a.nbt}; one is picked at random each time. Adding a
 * tree to the world is adding its file and its name to that list.
 */
public record TemplateTreeConfiguration(List<ResourceLocation> templates, Ground ground, int yOffset)
        implements FeatureConfiguration {

    /** The footing a tree needs, and what the bottom layer of its structure is measured against. */
    public enum Ground implements StringRepresentable {
        /** Dry soil, mud or sand, with the bottom layer of the structure at ground level. */
        LAND("land"),
        /** Standing water, with the bottom layer of the structure down on the bed of it. */
        WATER("water");

        public static final Codec<Ground> CODEC = StringRepresentable.fromEnum(Ground::values);

        private final String name;

        Ground(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public static final Codec<TemplateTreeConfiguration> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ResourceLocation.CODEC.listOf().fieldOf("templates")
                            .forGetter(TemplateTreeConfiguration::templates),
                    Ground.CODEC.optionalFieldOf("ground", Ground.LAND)
                            .forGetter(TemplateTreeConfiguration::ground),
                    // Nudges the whole thing up or down, for structures that were not saved
                    // starting exactly at the block the tree should stand on.
                    Codec.intRange(-8, 8).optionalFieldOf("y_offset", 0)
                            .forGetter(TemplateTreeConfiguration::yOffset))
                    .apply(instance, TemplateTreeConfiguration::new));
}
