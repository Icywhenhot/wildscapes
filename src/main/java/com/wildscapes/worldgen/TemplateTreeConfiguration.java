package com.wildscapes.worldgen;

import com.mojang.serialization.MapCodec;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;

public record TemplateTreeConfiguration(List<Identifier> templates, Ground ground, int yOffset,
        int maxWaterDepth) {
    public enum Ground implements StringRepresentable {
        LAND("land"),

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

    public static final MapCodec<TemplateTreeConfiguration> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Identifier.CODEC.listOf().fieldOf("templates")
                            .forGetter(TemplateTreeConfiguration::templates),
                    Ground.CODEC.optionalFieldOf("ground", Ground.LAND)
                            .forGetter(TemplateTreeConfiguration::ground),

                    Codec.intRange(-8, 8).optionalFieldOf("y_offset", 0)
                            .forGetter(TemplateTreeConfiguration::yOffset),

                    Codec.intRange(1, 32).optionalFieldOf("max_water_depth", 5)
                            .forGetter(TemplateTreeConfiguration::maxWaterDepth))
                    .apply(instance, TemplateTreeConfiguration::new));
}
