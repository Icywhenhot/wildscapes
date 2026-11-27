package com.wildscapes.entity.client;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wildscapes.Wildscapes;
import com.wildscapes.entity.AbominationEntity;
import com.wildscapes.entity.TonguePath;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import org.joml.Quaternionf;

import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class AbominationRenderer extends GeoEntityRenderer<AbominationEntity> {
    private static final ResourceLocation SEGMENT = ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID,
            "textures/entity/tongue_segment.png");
    private static final ResourceLocation TIP = ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID,
            "textures/entity/tongue_tip.png");
    private static final ResourceLocation GRASP = ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID,
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
        addRenderLayer(new PotionGlowLayer(this));
    }

    @Override
    public boolean shouldRender(AbominationEntity abomination, Frustum frustum, double x, double y, double z) {
        if (super.shouldRender(abomination, frustum, x, y, z)) {
            return true;
        }
        Entity target = abomination.level().getEntity(abomination.getTongueTargetId());
        return target != null && frustum.isVisible(abomination.getBoundingBox().minmax(target.getBoundingBox()));
    }

    @Override
    public void render(AbominationEntity abomination, float yaw, float partialTick, PoseStack pose,
            MultiBufferSource buffer, int light) {
        super.render(abomination, yaw, partialTick, pose, buffer, light);
        renderTongue(abomination, partialTick, pose, buffer, light);
        renderGrasp(abomination, partialTick, pose, buffer, light);
    }

    private void renderTongue(AbominationEntity abomination, float partialTick, PoseStack pose,
            MultiBufferSource buffer, int light) {
        float reach = abomination.tongueReach(partialTick);
        if (reach <= 0.001F) {
            return;
        }
        List<Vec3> stored = abomination.tonguePath();
        if (stored.size() < 2) {
            return;
        }

        List<Vec3> pts = new ArrayList<>(stored);
        pts.set(0, abomination.mouthPos(partialTick));
        double total = 0.0D;
        for (int i = 1; i < pts.size(); i++) {
            total += pts.get(i).distanceTo(pts.get(i - 1));
        }
        if (total < 1.0E-4D) {
            return;
        }

        Vec3 base = abomination.getPosition(partialTick);
        double want = total * reach;
        int count = Math.max(1, Mth.ceil(want / SEG));
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(SEGMENT));
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
            segment.render(pose, consumer, light, OverlayTexture.NO_OVERLAY);
            pose.popPose();
        }

        if (dir.lengthSqr() < 1.0E-8D) {
            return;
        }
        Vec3 head = last.subtract(base);
        pose.pushPose();
        pose.translate(head.x, head.y, head.z);
        pose.mulPose(pointAlong(dir));
        tip.render(pose, buffer.getBuffer(RenderType.entityCutoutNoCull(TIP)), light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }

    private void renderGrasp(AbominationEntity abomination, float partialTick, PoseStack pose,
            MultiBufferSource buffer, int light) {
        if (!abomination.isGrasping()
                || !(abomination.level().getEntity(abomination.getTongueTargetId()) instanceof LivingEntity target)) {
            return;
        }
        Vec3 at = target.getPosition(partialTick).subtract(abomination.getPosition(partialTick));
        float bodyYaw = Mth.rotLerp(partialTick, target.yBodyRotO, target.yBodyRot);

        pose.pushPose();
        pose.translate(at.x, at.y, at.z);
        pose.mulPose(new Quaternionf().rotateY((180.0F - bodyYaw) * Mth.DEG_TO_RAD));
        pose.scale(-1.0F, -1.0F, 1.0F);
        pose.scale(target.getBbWidth() / 0.6F, target.getBbHeight() / 1.8F, target.getBbWidth() / 0.6F);
        grasp.render(pose, buffer.getBuffer(RenderType.entityCutoutNoCull(GRASP)), light,
                LivingEntityRenderer.getOverlayCoords(target, 0.0F));
        pose.popPose();
    }

    private static Quaternionf pointAlong(Vec3 direction) {
        return new Quaternionf().rotationTo(0F, 0F, 1F,
                (float) direction.x, (float) direction.y, (float) direction.z);
    }
}
