package com.wildscapes.particle.client;

import org.joml.Quaternionf;

import net.minecraft.client.renderer.state.level.QuadParticleRenderState;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.RandomSource;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class MultiJumpParticle extends SingleQuadParticle {
    private final SpriteSet sprites;

    protected MultiJumpParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z, sprites.get(0, 1));
        this.sprites = sprites;
        lifetime = 10;
        quadSize = 0.7F;
        hasPhysics = false;
        setSprite(sprites.get(0, 4));
    }

    @Override
    public void tick() {
        super.tick();
        setSprite(sprites.get(Math.min(age / 2, 4), 4));
    }

    @Override
    public void extract(QuadParticleRenderState state, Camera camera, float partialTick) {
        extractRotatedQuad(state, camera, new Quaternionf().rotateX((float) (Math.PI / 2)), partialTick);
        extractRotatedQuad(state, camera, new Quaternionf().rotateX((float) (-Math.PI / 2)), partialTick);
    }

    @Override
    public int getLightCoords(float partialTick) {
        return 0xF000F0;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xd, double yd, double zd, RandomSource random) {
            return new MultiJumpParticle(level, x, y, z, sprites);
        }
    }
}
