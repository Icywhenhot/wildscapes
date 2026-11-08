package com.wildscapes.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public final class MirageLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {
    private static final int COPIES = 2;
    private static final double RADIUS = 1.4;
    private static final float SPEED = 0.05F;

    private final LivingEntityRenderer<?, S, M> renderer;

    public MirageLayer(LivingEntityRenderer<?, S, M> renderer) {
        super(renderer);
        this.renderer = renderer;
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector nodes, int light, S state, float yaw, float pitch) {
        if (!state.getDataOrDefault(MobVisuals.MIRAGE, false)) {
            return;
        }
        float phase = state.ageInTicks * SPEED;

        for (int i = 0; i < COPIES; i++) {
            float angle = phase + i * Mth.PI;
            pose.pushPose();
            pose.translate(Mth.cos(angle) * RADIUS, 0.0, Mth.sin(angle) * RADIUS);
            renderColoredCutoutModel(getParentModel(), renderer.getTextureLocation(state), pose, nodes, light, state, -1, 0);
            pose.popPose();
        }
    }
}
