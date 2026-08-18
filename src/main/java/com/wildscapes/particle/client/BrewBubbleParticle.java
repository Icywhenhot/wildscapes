package com.wildscapes.particle.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * A bubble swelling up out of the brew when nether wart is added: it drifts slowly upwards while
 * playing its five sprite frames once, so the frame it shows tracks how close it is to popping.
 */
public class BrewBubbleParticle extends TextureSheetParticle {

    private final SpriteSet sprites;

    protected BrewBubbleParticle(ClientLevel level, double x, double y, double z,
            double xd, double yd, double zd, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.xd = xd * 0.2;
        this.yd = yd * 0.2 + 0.02;
        this.zd = zd * 0.2;
        this.gravity = 0.0F;
        this.friction = 0.96F;
        this.hasPhysics = false;
        this.quadSize *= 0.6F + this.random.nextFloat() * 0.5F;
        this.lifetime = 14 + this.random.nextInt(8);
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

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xd, double yd, double zd) {
            return new BrewBubbleParticle(level, x, y, z, xd, yd, zd, sprites);
        }
    }
}
