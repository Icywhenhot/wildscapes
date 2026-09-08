package com.wildscapes.entity.client;

import com.wildscapes.Wildscapes;
import com.wildscapes.entity.AbominationEntity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

public class AbominationModel extends GeoModel<AbominationEntity> {
    private static final double MOUTH_HEIGHT = 1.5D;
    private static final double TONGUE_REST_LEN = 1.75D;
    private static final float TONGUE_MAX_SCALE = 8.0F;
    private static final float PITCH_SIGN = 1.0F;

    @Override
    public ResourceLocation getModelResource(AbominationEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "geo/abomination.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AbominationEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "textures/entity/abomination.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AbominationEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "animations/abomination.animation.json");
    }

    @Override
    public void setCustomAnimations(AbominationEntity animatable, long instanceId, AnimationState<AbominationEntity> state) {
        super.setCustomAnimations(animatable, instanceId, state);

        int targetId = animatable.getTongueTargetId();
        if (targetId < 0) {
            return;
        }
        GeoBone tongue = getAnimationProcessor().getBone("tongue");
        if (tongue == null) {
            return;
        }
        Entity target = animatable.level().getEntity(targetId);
        if (target == null) {
            return;
        }

        double mouthY = animatable.getY() + MOUTH_HEIGHT;
        double dx = target.getX() - animatable.getX();
        double dz = target.getZ() - animatable.getZ();
        double horiz = Math.sqrt(dx * dx + dz * dz);
        double aimY = target.getY() + target.getBbHeight() * 0.5D;
        double dist = Math.sqrt(horiz * horiz + (aimY - mouthY) * (aimY - mouthY));

        float scaleZ = (float) Mth.clamp(dist / TONGUE_REST_LEN, 0.2D, TONGUE_MAX_SCALE);
        tongue.setScaleX(1.0F);
        tongue.setScaleZ(scaleZ);

        float pitch = (float) Math.atan2(mouthY - aimY, horiz);
        tongue.setRotX(pitch * PITCH_SIGN);
        tongue.setRotY(0.0F);
        tongue.setRotZ(0.0F);
        tongue.setPosX(0.0F);
        tongue.setPosY(0.0F);
        tongue.setPosZ(0.0F);
    }
}
