package com.wildscapes.entity.client;

import com.wildscapes.Wildscapes;
import com.wildscapes.entity.AbominationEntity;
import com.wildscapes.entity.SoulHarvest;

import net.minecraft.resources.Identifier;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.model.GeoModel;

public class AbominationModel extends GeoModel<AbominationEntity> {
    private static final DataTicket<Integer> PHASE = DataTicket.create("wildscapes_phase", Integer.class);
    private static final DataTicket<Boolean> EMPOWERED_STATE = DataTicket.create("wildscapes_empowered", Boolean.class);
    private static final int FRAME_TICKS = 3;
    private static final Identifier[] FRAMES = strip("abomination_", 10);
    private static final Identifier[] GLOW = strip("abomination_glow_", 10);
    private static final Identifier[] EMPOWERED = strip("abomination_empowered_", 10);
    private static final Identifier[] EMPOWERED_GLOW = strip("abomination_empowered_glow_", 10);
    private static final int CYCLE = FRAMES.length * FRAME_TICKS;

    private static Identifier[] strip(String prefix, int count) {
        Identifier[] out = new Identifier[count];
        for (int i = 0; i < count; i++) {
            out[i] = Identifier.fromNamespaceAndPath(Wildscapes.MODID,
                    "textures/entity/" + prefix + i + ".png");
        }
        return out;
    }

    private static int phase(AbominationEntity animatable) {
        int t = (animatable.tickCount + animatable.getId() * FRAME_TICKS) % CYCLE;
        return t < 0 ? t + CYCLE : t;
    }

    @Override
    public void addAdditionalStateData(AbominationEntity entity, Object unused, GeoRenderState state) {
        state.addGeckolibData(PHASE, phase(entity));
        state.addGeckolibData(EMPOWERED_STATE, SoulHarvest.isEmpowered(entity));
    }

    public static Identifier glowFrame(GeoRenderState state) {
        Identifier[] glow = state.getOrDefaultGeckolibData(EMPOWERED_STATE, false) ? EMPOWERED_GLOW : GLOW;
        return glow[state.getOrDefaultGeckolibData(PHASE, 0) * glow.length / CYCLE];
    }

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return Wildscapes.id("abomination");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        int phase = state.getOrDefaultGeckolibData(PHASE, 0);
        if (state.getOrDefaultGeckolibData(EMPOWERED_STATE, false)) {
            return EMPOWERED[phase * EMPOWERED.length / CYCLE];
        }
        return FRAMES[phase / FRAME_TICKS];
    }

    @Override
    public Identifier getAnimationResource(AbominationEntity animatable) {
        return Wildscapes.id("abomination");
    }
}
