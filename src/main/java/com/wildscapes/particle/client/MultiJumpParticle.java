package com.wildscapes.particle.client;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

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

    private void renderRotatedQuad(VertexConsumer buffer, Camera camera, Quaternionf rotation, float partialTick) {
        var pos = camera.getPosition();
        float dx = (float) (Mth.lerp(partialTick, xo, x) - pos.x);
        float dy = (float) (Mth.lerp(partialTick, yo, y) - pos.y);
        float dz = (float) (Mth.lerp(partialTick, zo, z) - pos.z);
        Vector3f[] corners = {new Vector3f(-1, -1, 0), new Vector3f(-1, 1, 0),
                new Vector3f(1, 1, 0), new Vector3f(1, -1, 0)};
        for (int i = 0; i < corners.length; i++) {
            var v = corners[i].rotate(rotation).mul(getQuadSize(partialTick)).add(dx, dy, dz);
            buffer.vertex(v.x, v.y, v.z).uv(i < 2 ? getU1() : getU0(), i == 0 || i == 3 ? getV1() : getV0())
                    .color(rCol, gCol, bCol, alpha).uv2(getLightColor(partialTick)).endVertex();
        }
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
