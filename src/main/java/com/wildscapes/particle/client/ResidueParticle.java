package com.wildscapes.particle.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;

public class ResidueParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final int frames;

    protected ResidueParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd,
            SpriteSet sprites, int life, float size, float drag, float lift) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.frames = count(sprites);
        this.xd = xd;
        this.yd = yd + lift;
        this.zd = zd;
        this.gravity = 0.0F;
        this.friction = drag;
        this.hasPhysics = false;
        this.lifetime = life + this.random.nextInt(Math.max(1, life / 2));
        this.quadSize *= size * (0.8F + this.random.nextFloat() * 0.4F);
        frame();
    }

    private static int count(SpriteSet sprites) {
        int n = 1;
        TextureAtlasSprite last = sprites.get(0, 64);
        for (int i = 1; i <= 64; i++) {
            TextureAtlasSprite s = sprites.get(i, 64);
            if (s != last) {
                n++;
                last = s;
            }
        }
        return n;
    }

    private void frame() {
        if (frames < 2) {
            setSprite(sprites.get(0, 1));
            return;
        }
        setSprite(sprites.get(Math.min(age * frames / lifetime, frames - 1), frames - 1));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        frame();
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
