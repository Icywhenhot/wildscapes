package com.wildscapes.entity.client;

import com.wildscapes.Wildscapes;
import com.wildscapes.entity.AbominationEntity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class AbominationModel extends GeoModel<AbominationEntity> {
    private static final int FRAME_TICKS = 3;
    private static final ResourceLocation[] FRAMES = strip("abomination_", 16);
    private static final ResourceLocation[] GLOW = strip("abomination_glow_", 16);
    private static final int CYCLE = FRAMES.length * FRAME_TICKS;

    private static ResourceLocation[] strip(String prefix, int count) {
        ResourceLocation[] out = new ResourceLocation[count];
        for (int i = 0; i < count; i++) {
            out[i] = ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID,
                    "textures/entity/" + prefix + i + ".png");
        }
        return out;
    }

    private static int phase(AbominationEntity animatable) {
        int t = (animatable.tickCount + animatable.getId() * FRAME_TICKS) % CYCLE;
        return t < 0 ? t + CYCLE : t;
    }

    public static ResourceLocation glowFrame(AbominationEntity animatable) {
        return GLOW[phase(animatable) * GLOW.length / CYCLE];
    }

    @Override
    public ResourceLocation getModelResource(AbominationEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "geo/abomination.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AbominationEntity animatable) {
        return FRAMES[phase(animatable) / FRAME_TICKS];
    }

    @Override
    public ResourceLocation getAnimationResource(AbominationEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "animations/abomination.animation.json");
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
