package com.wildscapes.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wildscapes.effect.WildscapesEffects;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public final class MirageLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final int COPIES = 2;
    private static final double RADIUS = 1.4;
    private static final float SPEED = 0.05F;

    public MirageLayer(LivingEntityRenderer<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffer, int light, T entity, float limbSwing,
            float limbSwingAmount, float partialTick, float age, float headYaw, float headPitch) {
        if (!entity.hasEffect(WildscapesEffects.MIRAGE)) {
            return;
        }
        float phase = (entity.tickCount + partialTick) * SPEED;

        for (int i = 0; i < COPIES; i++) {
            float angle = phase + i * Mth.PI;
            pose.pushPose();
            pose.translate(Mth.cos(angle) * RADIUS, 0.0, Mth.sin(angle) * RADIUS);
            renderColoredCutoutModel(getParentModel(), getTextureLocation(entity), pose, buffer, light, entity, -1);
            pose.popPose();
        }
    }
}
