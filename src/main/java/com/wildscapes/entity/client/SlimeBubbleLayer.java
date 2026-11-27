package com.wildscapes.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wildscapes.Wildscapes;
import com.wildscapes.effect.WildscapesEffects;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public final class SlimeBubbleLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID,
            "textures/entity/slime_bubble.png");
    private final ModelPart bubble;

    public SlimeBubbleLayer(LivingEntityRenderer<T, M> renderer, EntityModelSet models) {
        super(renderer);
        bubble = models.bakeLayer(WildscapesModelLayers.SLIME_BUBBLE).getChild("bubble");
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffer, int light, T entity, float limbSwing,
            float limbSwingAmount, float partialTick, float age, float headYaw, float headPitch) {
        if (!entity.hasEffect(WildscapesEffects.SLIMEBOUND)) {
            return;
        }

        float phase = (entity.tickCount % 80 + partialTick) * Mth.PI / 80.0F;
        float pulse = Mth.sin(phase) * 0.05F;
        float size = Math.max(entity.getBbWidth(), entity.getBbHeight()) * 1.15F + 0.3F;

        pose.pushPose();
        pose.translate(0.0F, 1.5F - entity.getBbHeight() * 0.5F, 0.0F);
        pose.scale(size * 0.5F * (1.0F + pulse), size * 0.5F * (1.0F - pulse),
                size * 0.5F * (1.0F + pulse));
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        bubble.render(pose, consumer, light, LivingEntityRenderer.getOverlayCoords(entity, 0.0F));
        pose.popPose();
    }
}
