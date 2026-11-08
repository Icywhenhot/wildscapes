package com.wildscapes.entity.client;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wildscapes.Wildscapes;
import com.wildscapes.entity.AbominationEntity;
import com.wildscapes.entity.TonguePath;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.base.BoneSnapshots;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import org.joml.Quaternionf;

import com.geckolib.renderer.GeoEntityRenderer;

public class AbominationRenderer extends GeoEntityRenderer<AbominationEntity, AbominationRenderer.State> {
    private static final Identifier SEGMENT = Identifier.fromNamespaceAndPath(Wildscapes.MODID,
            "textures/entity/tongue_segment.png");
    private static final Identifier TIP = Identifier.fromNamespaceAndPath(Wildscapes.MODID,
            "textures/entity/tongue_tip.png");
    private static final Identifier GRASP = Identifier.fromNamespaceAndPath(Wildscapes.MODID,
            "textures/entity/abomination_grasp.png");
    private static final double SEG = 0.5D;

    private final ModelPart segment;
    private final ModelPart tip;
    private final ModelPart grasp;

    public AbominationRenderer(EntityRendererProvider.Context context) {
        super(context, new AbominationModel());
        segment = context.bakeLayer(WildscapesModelLayers.TONGUE_SEGMENT).getChild("segment");
        tip = context.bakeLayer(WildscapesModelLayers.TONGUE_TIP).getChild("tip");
        grasp = context.bakeLayer(WildscapesModelLayers.ABOMINATION_GRASP).getChild("grasp");
        withRenderLayer(new PotionGlowLayer(this));
    }

    @Override
    public boolean shouldRender(AbominationEntity abomination, Frustum frustum, double x, double y, double z) {
        if (super.shouldRender(abomination, frustum, x, y, z)) {
            return true;
        }
        Entity target = abomination.level().getEntity(abomination.getTongueTargetId());
        return target != null && frustum.isVisible(abomination.getBoundingBox().minmax(target.getBoundingBox()));
    }

    public static class State extends LivingEntityRenderState {
        List<Vec3> tongue = List.of();
        Vec3 base;
        float reach;
        Vec3 grasp;
        float targetYaw;
        float targetWidth;
        float targetHeight;
        boolean hideTongue;
    }

    @Override
    public State createRenderState(AbominationEntity entity, Void unused) {
        return new State();
    }

    @Override
    public void extractRenderState(AbominationEntity entity, State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.base = entity.getPosition(partialTick);
        state.reach = entity.tongueReach(partialTick);
        List<Vec3> path = entity.tonguePath();
        state.tongue = path.isEmpty() ? List.of() : new ArrayList<>(path);
        if (!state.tongue.isEmpty()) {
            state.tongue.set(0, entity.mouthPos(partialTick));
        }
        state.hideTongue = entity.getTongueTicks() >= 0;
        state.grasp = null;
        if (entity.isGrasping() && entity.level().getEntity(entity.getTongueTargetId()) instanceof LivingEntity target) {
            state.grasp = target.getPosition(partialTick).subtract(state.base);
            state.targetYaw = Mth.rotLerp(partialTick, target.yBodyRotO, target.yBodyRot);
            state.targetWidth = target.getBbWidth();
            state.targetHeight = target.getBbHeight();
        }
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<State> pass, BoneSnapshots bones) {
        State state = pass.renderState();
        bones.ifPresent("head", head -> {
            head.setRotX(head.getRotX() + state.xRot * Mth.DEG_TO_RAD);
            head.setRotY(head.getRotY() + state.yRot * Mth.DEG_TO_RAD);
        });
        bones.ifPresent("tongue", tongue -> tongue.skipRender(state.hideTongue).skipChildrenRender(state.hideTongue));
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector nodes, CameraRenderState camera) {
        super.submit(state, pose, nodes, camera);
        renderTongue(state, pose, nodes);
        renderGrasp(state, pose, nodes);
    }

    private void renderTongue(State state, PoseStack pose, SubmitNodeCollector nodes) {
        List<Vec3> pts = state.tongue;
        if (state.reach <= 0.001F || pts.size() < 2) {
            return;
        }

        double total = 0.0D;
        for (int i = 1; i < pts.size(); i++) {
            total += pts.get(i).distanceTo(pts.get(i - 1));
        }
        if (total < 1.0E-4D) {
            return;
        }

        Vec3 base = state.base;
        double want = total * state.reach;
        int count = Math.max(1, Mth.ceil(want / SEG));
        Vec3 last = pts.get(0);
        Vec3 dir = Vec3.ZERO;
        for (int i = 0; i < count; i++) {
            double from = Math.min(i * SEG, want);
            double to = Math.min((i + 1) * SEG, want);
            Vec3 a = TonguePath.pointAt(pts, from / total);
            Vec3 b = TonguePath.pointAt(pts, to / total);
            Vec3 step = b.subtract(a);
            double len = step.length();
            if (len < 1.0E-5D) {
                continue;
            }
            dir = step.scale(1.0D / len);
            last = b;
            Vec3 mid = a.add(b).scale(0.5D).subtract(base);

            pose.pushPose();
            pose.translate(mid.x, mid.y, mid.z);
            pose.mulPose(pointAlong(dir));
            pose.scale(1.0F, 1.0F, (float) (len / SEG));
            nodes.submitModelPart(segment, pose, RenderTypes.entityCutout(SEGMENT), state.lightCoords, OverlayTexture.NO_OVERLAY, null);
            pose.popPose();
        }

        if (dir.lengthSqr() < 1.0E-8D) {
            return;
        }
        Vec3 head = last.subtract(base);
        pose.pushPose();
        pose.translate(head.x, head.y, head.z);
        pose.mulPose(pointAlong(dir));
        nodes.submitModelPart(tip, pose, RenderTypes.entityCutout(TIP), state.lightCoords, OverlayTexture.NO_OVERLAY, null);
        pose.popPose();
    }

    private void renderGrasp(State state, PoseStack pose, SubmitNodeCollector nodes) {
        if (state.grasp == null) {
            return;
        }

        pose.pushPose();
        pose.translate(state.grasp.x, state.grasp.y, state.grasp.z);
        pose.mulPose(new Quaternionf().rotateY((180.0F - state.targetYaw) * Mth.DEG_TO_RAD));
        pose.scale(-1.0F, -1.0F, 1.0F);
        pose.scale(state.targetWidth / 0.6F, state.targetHeight / 1.8F, state.targetWidth / 0.6F);
        nodes.submitModelPart(grasp, pose, RenderTypes.entityCutout(GRASP), state.lightCoords, OverlayTexture.NO_OVERLAY, null);
        pose.popPose();
    }

    private static Quaternionf pointAlong(Vec3 direction) {
        return new Quaternionf().rotationTo(0F, 0F, 1F,
                (float) direction.x, (float) direction.y, (float) direction.z);
    }
}
