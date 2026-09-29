package com.wildscapes.particle.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class ResidueParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected ResidueParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd,
            SpriteSet sprites, int life, float size, float drag, float lift) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.xd = xd;
        this.yd = yd + lift;
        this.zd = zd;
        this.gravity = 0.0F;
        this.friction = drag;
        this.hasPhysics = false;
        this.lifetime = life + this.random.nextInt(Math.max(1, life / 2));
        this.quadSize *= size * (0.8F + this.random.nextFloat() * 0.4F);
        setSpriteFromAge(sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        setSpriteFromAge(sprites);
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final int life;
        private final float size;
        private final float drag;
        private final float lift;

        public Provider(SpriteSet sprites, int life, float size, float drag, float lift) {
            this.sprites = sprites;
            this.life = life;
            this.size = size;
            this.drag = drag;
            this.lift = lift;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xd, double yd, double zd) {
            return new ResidueParticle(level, x, y, z, xd, yd, zd, sprites, life, size, drag, lift);
        }
    }
}
