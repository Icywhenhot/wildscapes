package com.wildscapes.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public record SoulTrailOptions(Vec3 target, int duration) implements ParticleOptions {
    public static final MapCodec<SoulTrailOptions> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Vec3.CODEC.fieldOf("target").forGetter(SoulTrailOptions::target),
            Codec.INT.fieldOf("duration").forGetter(SoulTrailOptions::duration)
    ).apply(i, SoulTrailOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SoulTrailOptions> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, o -> o.target.x,
            ByteBufCodecs.DOUBLE, o -> o.target.y,
            ByteBufCodecs.DOUBLE, o -> o.target.z,
            ByteBufCodecs.VAR_INT, SoulTrailOptions::duration,
            (x, y, z, t) -> new SoulTrailOptions(new Vec3(x, y, z), t));

    @Override
    public ParticleType<?> getType() {
        return WildscapesParticles.SOUL_TRAIL.get();
    }
}
