package com.wildscapes.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.monster.Slime;

public class RedesignedSlimeOuterLayer extends RenderLayer<Slime, SlimeModel<Slime>> {
    private final SlimeModel<Slime>[] outerModels;

    @SuppressWarnings("unchecked")
    public RedesignedSlimeOuterLayer(RenderLayerParent<Slime, SlimeModel<Slime>> renderer, EntityModelSet modelSet) {
        super(renderer);
        this.outerModels = new SlimeModel[] {
                new SlimeModel<>(modelSet.bakeLayer(WildscapesModelLayers.SLIME_SMALL_OUTER)),
                new SlimeModel<>(modelSet.bakeLayer(WildscapesModelLayers.SLIME_MEDIUM_OUTER)),
                new SlimeModel<>(modelSet.bakeLayer(WildscapesModelLayers.SLIME_LARGE_OUTER)),
        };
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Slime slime,
            float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
            float netHeadYaw, float headPitch) {
        boolean glowing = Minecraft.getInstance().shouldEntityAppearGlowing(slime) && slime.isInvisible();
        if (slime.isInvisible() && !glowing) {
            return;
        }
        SlimeModel<Slime> outer = this.outerModels[RedesignedSlimeRenderer.modelIndex(slime)];
        VertexConsumer consumer = glowing
                ? buffer.getBuffer(RenderType.outline(this.getTextureLocation(slime)))
                : buffer.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(slime)));
        this.getParentModel().copyPropertiesTo(outer);
        outer.prepareMobModel(slime, limbSwing, limbSwingAmount, partialTicks);
        outer.setupAnim(slime, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        outer.renderToBuffer(poseStack, consumer, packedLight, LivingEntityRenderer.getOverlayCoords(slime, 0.0F));
    }
}
