package com.wildscapes.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wildscapes.Wildscapes;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public final class SlimeBubbleLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Wildscapes.MODID,
            "textures/entity/slime_bubble.png");
    private final ModelPart bubble;

    public SlimeBubbleLayer(LivingEntityRenderer<?, S, M> renderer, EntityModelSet models) {
        super(renderer);
        bubble = models.bakeLayer(WildscapesModelLayers.SLIME_BUBBLE).getChild("bubble");
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector nodes, int light, S state, float yaw, float pitch) {
        if (!state.getDataOrDefault(MobVisuals.BUBBLE, false)) {
            return;
        }

        float phase = state.ageInTicks % 80 * Mth.PI / 80.0F;
        float pulse = Mth.sin(phase) * 0.05F;
        float size = Math.max(state.boundingBoxWidth, state.boundingBoxHeight) * 1.15F + 0.3F;

        pose.pushPose();
        pose.translate(0.0F, 1.5F - state.boundingBoxHeight * 0.5F, 0.0F);
        pose.scale(size * 0.5F * (1.0F + pulse), size * 0.5F * (1.0F - pulse),
                size * 0.5F * (1.0F + pulse));
        nodes.order(1).submitModelPart(bubble, pose, RenderTypes.entityTranslucent(TEXTURE), light,
                LivingEntityRenderer.getOverlayCoords(state, 0.0F), null);
        pose.popPose();
    }
}
