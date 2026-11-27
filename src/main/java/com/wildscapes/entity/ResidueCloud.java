package com.wildscapes.entity;

import com.wildscapes.particle.WildscapesParticles;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class ResidueCloud extends AreaEffectCloud {
    public ResidueCloud(EntityType<? extends ResidueCloud> type, Level level) {
        super(type, level);
    }

    public ResidueCloud(Level level, double x, double y, double z) {
        this(WildscapesEntities.RESIDUE_CLOUD.get(), level);
        setPos(x, y, z);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide || isWaiting() || tickCount % 3 != 0) {
            return;
        }
        float r = getRadius();
        int n = 1 + Mth.floor(r * 0.5F);
        for (int i = 0; i < n; i++) {
            float a = random.nextFloat() * Mth.TWO_PI;
            float d = Mth.sqrt(random.nextFloat()) * r;
            level().addAlwaysVisibleParticle(WildscapesParticles.RESIDUE_SWIRL.get(),
                    getX() + Mth.cos(a) * d, getY() + 0.1, getZ() + Mth.sin(a) * d,
                    (0.5 - random.nextDouble()) * 0.02, 0.015, (0.5 - random.nextDouble()) * 0.02);
        }
    }
}
