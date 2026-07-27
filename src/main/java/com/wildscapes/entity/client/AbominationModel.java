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
    // The tongue bone is a ~1.75-block bar pointing along local -Z. The baked "thounge"
    // animation reveals it (scaleY 0→1→0) and gives it its lash pose; while the tongue
    // attack is active we compose on top of that — pitching it at the grabbed entity and
    // stretching it along its length to reach, so it retracts naturally as the victim is
    // reeled in. Tune these while watching it in-game.
    private static final double MOUTH_HEIGHT = 1.5D;    // tongue root height above the mob's feet (blocks)
    private static final double TONGUE_REST_LEN = 1.75D; // bone length at scaleZ = 1 (28px / 16)
    private static final float TONGUE_MAX_SCALE = 8.0F;   // don't stretch beyond this
    private static final float PITCH_SIGN = 1.0F;         // flip if the tongue aims up instead of down

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
            return; // not lashing — leave the tongue as the baked animation left it
        }
        GeoBone tongue = getAnimationProcessor().getBone("tongue");
        if (tongue == null) {
            return;
        }
        Entity target = animatable.level().getEntity(targetId);
        if (target == null) {
            return;
        }

        // Vector from the mouth to the target's midriff.
        double mouthY = animatable.getY() + MOUTH_HEIGHT;
        double dx = target.getX() - animatable.getX();
        double dz = target.getZ() - animatable.getZ();
        double horiz = Math.sqrt(dx * dx + dz * dz);
        double aimY = target.getY() + target.getBbHeight() * 0.5D;
        double dist = Math.sqrt(horiz * horiz + (aimY - mouthY) * (aimY - mouthY));

        // Compose on top of the baked "thounge" reveal. We leave scaleY alone — the
        // animation fades the tongue in and out (scaleY 0→1→0) as the mouth opens and the
        // lash retracts — and drive the rest: stretch it along its length (scaleZ) to reach
        // the target and pitch it so the tip lands on them. The result is a tongue that
        // visibly snaps out and reels the victim in as it retracts. The body already faces
        // the target, so only pitch is needed (down toward a lower target); the root is
        // held at the pivot (pos = 0) so it lines up with the aim math (MOUTH_HEIGHT).
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
