package com.wildscapes.particle.client;

import org.joml.Quaternionf;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class MultiJumpParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected MultiJumpParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z);
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
    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        renderRotatedQuad(buffer, camera, new Quaternionf().rotateX((float) (Math.PI / 2)), partialTick);
        renderRotatedQuad(buffer, camera, new Quaternionf().rotateX((float) (-Math.PI / 2)), partialTick);
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xd, double yd, double zd) {
            return new MultiJumpParticle(level, x, y, z, sprites);
        }
    }
}
