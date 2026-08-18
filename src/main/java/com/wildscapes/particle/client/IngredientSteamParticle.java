package com.wildscapes.particle.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * The reaction an ingredient stirs up when it hits the brew: a puff that steams straight up out
 * of the pot, thinning through its four sprite frames as it goes.
 *
 * <p>It always rises, whatever velocity it is spawned with. The boiling pot is deep — at low
 * water the surface sits well below the rim — so a puff that arced back down like a splash
 * would spend its whole life hidden behind the walls.
 */
public class IngredientSteamParticle extends TextureSheetParticle {

    private final SpriteSet sprites;

    protected IngredientSteamParticle(ClientLevel level, double x, double y, double z,
            double xd, double yd, double zd, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.xd = xd * 0.3;
        this.yd = Math.abs(yd) * 0.5 + 0.07;
        this.zd = zd * 0.3;
        this.gravity = 0.0F;
        this.friction = 0.97F;
        this.hasPhysics = false;
        this.quadSize *= 0.8F + this.random.nextFloat() * 0.5F;
        this.lifetime = 22 + this.random.nextInt(10);
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
            return new IngredientSteamParticle(level, x, y, z, xd, yd, zd, sprites);
        }
    }
}
