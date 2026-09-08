package com.wildscapes.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wildscapes.Wildscapes;
import com.wildscapes.entity.MirelashHook;
import com.wildscapes.item.WildscapesItems;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public final class MirelashHookRenderer extends EntityRenderer<MirelashHook> {
    private static final ResourceLocation HOOK_TEXTURE = ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID,
            "textures/entity/mirelash_hook.png");
    private static final ResourceLocation SEGMENT_TEXTURE = ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID,
            "textures/entity/mirelash_segment.png");
    private static final float SEGMENT_LENGTH = 0.5F;
    private final ModelPart hookModel;
    private final ModelPart segmentModel;

    public MirelashHookRenderer(EntityRendererProvider.Context context) {
        super(context);
        hookModel = context.bakeLayer(WildscapesModelLayers.MIRELASH_HOOK).getChild("hook");
        segmentModel = context.bakeLayer(WildscapesModelLayers.MIRELASH_SEGMENT).getChild("segment");
    }

    @Override
    public boolean shouldRender(MirelashHook hook, Frustum frustum, double x, double y, double z) {
        Entity owner = hook.getOwner();
        return owner != null && frustum.isVisible(hook.getBoundingBox().minmax(owner.getBoundingBox()))
                || super.shouldRender(hook, frustum, x, y, z);
    }

    @Override
    public void render(MirelashHook hook, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource bufferSource, int packedLight) {
        Entity owner = hook.getOwner();
        if (owner == null) {
            return;
        }

        Vec3 hookPos = hook.getPosition(partialTick);
        Vec3 handPos = owner instanceof Player player ? getHandPos(player, partialTick)
                : owner.getRopeHoldPosition(partialTick);
        Vec3 rope = handPos.subtract(hookPos);
        double length = rope.length();
        if (length < 0.001) {
            return;
        }

        Vec3 step = rope.scale(1.0 / length);
        VertexConsumer segmentBuffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(SEGMENT_TEXTURE));
        int segments = Mth.ceil(length / SEGMENT_LENGTH);
        for (int i = 0; i < segments; i++) {
            double from = i * SEGMENT_LENGTH;
            double partLength = Math.min(SEGMENT_LENGTH, length - from);
            Vec3 center = step.scale(from + partLength * 0.5);
            poseStack.pushPose();
            poseStack.translate(center.x, center.y, center.z);
            poseStack.mulPose(pointAlong(step));
            poseStack.scale(1F, 1F, (float) (partLength / SEGMENT_LENGTH));
            segmentModel.render(poseStack, segmentBuffer, packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }

        poseStack.pushPose();
        poseStack.mulPose(pointAlong(step));
        VertexConsumer hookBuffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(HOOK_TEXTURE));
        hookModel.render(poseStack, hookBuffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(hook, yaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private Vec3 getHandPos(Player player, float partialTick) {
        int side = player.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.is(WildscapesItems.MIRELASH.get())) {
            side = -side;
        }

        float swing = player.getAttackAnim(partialTick);
        float swingOffset = Mth.sin(Mth.sqrt(swing) * Mth.PI);
        if (entityRenderDispatcher.options.getCameraType().isFirstPerson()
                && player == Minecraft.getInstance().player) {
            double fovScale = 960.0 / entityRenderDispatcher.options.fov().get();
            Vec3 offset = entityRenderDispatcher.camera.getNearPlane().getPointOnPlane(side * 0.6F, -1F)
                    .scale(fovScale)
                    .yRot(swingOffset * 0.25F)
                    .xRot(-swingOffset * 0.35F);
            double x = Mth.lerp(partialTick, player.xo, player.getX());
            double y = Mth.lerp(partialTick, player.yo, player.getY()) + player.getEyeHeight() * 0.4F;
            double z = Mth.lerp(partialTick, player.zo, player.getZ());
            return new Vec3(x, y, z).add(offset);
        }

        float bodyRot = Mth.lerp(partialTick, player.yBodyRotO, player.yBodyRot) * Mth.DEG_TO_RAD;
        double sin = Mth.sin(bodyRot);
        double cos = Mth.cos(bodyRot);
        float scale = player.getScale();
        double sideOffset = side * 0.35 * scale;
        double forwardOffset = 0.2 * scale;
        float crouch = player.isCrouching() ? -0.1875F : 0F;
        return player.getEyePosition(partialTick).add(-cos * sideOffset - sin * forwardOffset,
                crouch - 1.0F - player.getEyeHeight() * 0.3F, -sin * sideOffset + cos * forwardOffset);
    }

    private static Quaternionf pointAlong(Vec3 direction) {
        return new Quaternionf().rotationTo(0F, 0F, 1F,
                (float) direction.x, (float) direction.y, (float) direction.z);
    }

    @Override
    public ResourceLocation getTextureLocation(MirelashHook hook) {
        return HOOK_TEXTURE;
    }
}
