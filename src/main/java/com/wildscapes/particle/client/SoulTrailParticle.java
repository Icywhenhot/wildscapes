package com.wildscapes.particle.client;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wildscapes.particle.SoulTrailOptions;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class SoulTrailParticle extends TextureSheetParticle {
    private static final int RISE = 10;

    private final SpriteSet sprites;
    private final Vec3 from;
    private final Vec3 peak;
    private final Vec3 to;
    private final Vec3 sway;
    private final float waves;
    private final float phase;
    private Vec3 heading = new Vec3(0.0, 1.0, 0.0);

    protected SoulTrailParticle(ClientLevel level, double x, double y, double z, SoulTrailOptions options, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.from = new Vec3(x, y, z);
        this.peak = from.add((random.nextDouble() - 0.5) * 0.3, 0.7 + random.nextDouble() * 0.4,
                (random.nextDouble() - 0.5) * 0.3);
        this.to = options.target();
        this.lifetime = Math.max(RISE + 5, options.duration());
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.quadSize *= 1.4F + random.nextFloat() * 0.4F;
        this.waves = 1.5F + random.nextFloat() * 1.5F;
        this.phase = random.nextFloat() * Mth.TWO_PI;

        Vec3 dir = to.subtract(peak).normalize();
        Vec3 side = dir.cross(new Vec3(0.0, 1.0, 0.0));
        side = side.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : side.normalize();
        Vec3 lift = side.cross(dir).normalize();
        double spin = random.nextDouble() * Math.PI;
        double amp = 0.5 + random.nextDouble() * 0.4;
        this.sway = side.scale(Math.cos(spin) * amp).add(lift.scale(Math.sin(spin) * amp));
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

    private Vec3 at(float age) {
        if (age <= RISE) {
            float t = age / RISE;
            return from.lerp(peak, 1.0 - (1.0 - t) * (1.0 - t));
        }
        float t = Math.min(1.0F, (age - RISE) / (lifetime - RISE));
        float eased = t * t * (3.0F - 2.0F * t);
        double wave = Math.sin(t * Mth.TWO_PI * waves + phase) * Math.sin(t * Math.PI);
        return peak.lerp(to, eased).add(sway.scale(wave));
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
        Vec3 p = at(age);
        Vec3 next = at(age + 1);
        Vec3 step = next.subtract(p);
        if (step.lengthSqr() > 1.0E-6) {
            heading = step.normalize();
        }
        setPos(p.x, p.y, p.z);
        setSprite(sprites.get((age / 3) % 5, 4));
        float t = (float) age / lifetime;
        alpha = t > 0.9F ? (1.0F - t) / 0.1F : 1.0F;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        float tilt = Mth.clamp((age + partialTicks - RISE + 2.0F) / 6.0F, 0.0F, 1.0F);
        float angle = 0.0F;
        if (tilt > 0.0F) {
            Vector3f local = new Vector3f((float) heading.x, (float) heading.y, (float) heading.z)
                    .rotate(new Quaternionf(camera.rotation()).conjugate());
            angle = (float) Mth.atan2(-local.x, local.y) * tilt;
        }
        oRoll = angle;
        roll = angle;
        super.render(buffer, camera, partialTicks);
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
