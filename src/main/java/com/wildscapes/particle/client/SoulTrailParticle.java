package com.wildscapes.particle.client;

import com.wildscapes.particle.SoulTrailOptions;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class SoulTrailParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final Vec3 from;
    private final Vec3 to;
    private final float wobble;

    protected SoulTrailParticle(ClientLevel level, double x, double y, double z, SoulTrailOptions options, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.from = new Vec3(x, y, z);
        this.to = options.target();
        this.lifetime = Math.max(1, options.duration());
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.quadSize *= 1.4F + this.random.nextFloat() * 0.4F;
        this.wobble = this.random.nextFloat() * Mth.TWO_PI;
        setSprite(sprites.get(0, 4));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        if (age++ >= lifetime) {
            remove();
            return;
        }
        float t = (float) age / lifetime;
        float eased = t * t * (3.0F - 2.0F * t);
        double arc = Math.sin(t * Math.PI) * 1.5;
        double sway = Math.sin(t * Math.PI * 3.0 + wobble) * 0.35 * (1.0 - t);
        Vec3 p = from.lerp(to, eased);
        setPos(p.x + sway, p.y + arc, p.z + Math.cos(t * Math.PI * 3.0 + wobble) * 0.35 * (1.0 - t));
        setSprite(sprites.get((age / 3) % 5, 4));
        alpha = t > 0.85F ? (1.0F - t) / 0.15F : 1.0F;
    }

    public static class Provider implements ParticleProvider<SoulTrailOptions> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SoulTrailOptions options, ClientLevel level, double x, double y, double z,
                double xd, double yd, double zd) {
            return new SoulTrailParticle(level, x, y, z, options, sprites);
        }
    }
}
