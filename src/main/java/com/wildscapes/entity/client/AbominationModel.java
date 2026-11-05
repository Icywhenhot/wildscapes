package com.wildscapes.entity.client;

import com.wildscapes.Wildscapes;
import com.wildscapes.entity.AbominationEntity;
import com.wildscapes.entity.SoulHarvest;

import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class AbominationModel extends GeoModel<AbominationEntity> {
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

    public static Identifier glowFrame(AbominationEntity animatable) {
        Identifier[] glow = SoulHarvest.isEmpowered(animatable) ? EMPOWERED_GLOW : GLOW;
        return glow[phase(animatable) * glow.length / CYCLE];
    }

    @Override
    public Identifier getModelResource(AbominationEntity animatable) {
        return Identifier.fromNamespaceAndPath(Wildscapes.MODID, "geo/abomination.geo.json");
    }

    @Override
    public Identifier getTextureResource(AbominationEntity animatable) {
        if (SoulHarvest.isEmpowered(animatable)) {
            return EMPOWERED[phase(animatable) * EMPOWERED.length / CYCLE];
        }
        return FRAMES[phase(animatable) / FRAME_TICKS];
    }

    @Override
    public Identifier getAnimationResource(AbominationEntity animatable) {
        return Identifier.fromNamespaceAndPath(Wildscapes.MODID, "animations/abomination.animation.json");
    }

    @Override
    public void setCustomAnimations(AbominationEntity animatable, long instanceId, AnimationState<AbominationEntity> state) {
        super.setCustomAnimations(animatable, instanceId, state);

        GeoBone head = getAnimationProcessor().getBone("head");
        EntityModelData data = state.getData(DataTickets.ENTITY_MODEL_DATA);
        if (head != null && data != null) {
            head.setRotX(head.getRotX() + data.headPitch() * Mth.DEG_TO_RAD);
            head.setRotY(head.getRotY() + data.netHeadYaw() * Mth.DEG_TO_RAD);
        }

        GeoBone tongue = getAnimationProcessor().getBone("tongue");
        if (tongue != null) {
            tongue.setHidden(animatable.getTongueTicks() >= 0);
        }
    }
}
