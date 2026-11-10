package com.wildscapes.particle;

import com.mojang.serialization.Codec;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public record SoulTrailOptions(Vec3 target, int duration) implements ParticleOptions {
    public static final Codec<SoulTrailOptions> CODEC = RecordCodecBuilder.create(i -> i.group(
            Vec3.CODEC.fieldOf("target").forGetter(SoulTrailOptions::target),
            Codec.INT.fieldOf("duration").forGetter(SoulTrailOptions::duration)
    ).apply(i, SoulTrailOptions::new));

    public static final ParticleOptions.Deserializer<SoulTrailOptions> DESERIALIZER = new ParticleOptions.Deserializer<>() {
        @Override
        public SoulTrailOptions fromCommand(ParticleType<SoulTrailOptions> type, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            double x = reader.readDouble();
            reader.expect(' ');
            double y = reader.readDouble();
            reader.expect(' ');
            double z = reader.readDouble();
            reader.expect(' ');
            return new SoulTrailOptions(new Vec3(x, y, z), reader.readInt());
        }

        @Override
        public SoulTrailOptions fromNetwork(ParticleType<SoulTrailOptions> type, FriendlyByteBuf buf) {
            return new SoulTrailOptions(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()), buf.readVarInt());
        }
    };

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeDouble(target.x);
        buf.writeDouble(target.y);
        buf.writeDouble(target.z);
        buf.writeVarInt(duration);
    }

    @Override
    public String writeToString() {
        return String.format(java.util.Locale.ROOT, "wildscapes:soul_trail %s %s %s %d", target.x, target.y, target.z, duration);
    }

    @Override
    public ParticleType<?> getType() {
        return WildscapesParticles.SOUL_TRAIL.get();
    }
}
