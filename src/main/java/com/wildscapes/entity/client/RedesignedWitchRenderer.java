package com.wildscapes.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wildscapes.Wildscapes;

import net.minecraft.client.model.WitchModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.WitchItemLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Witch;

public class RedesignedWitchRenderer extends MobRenderer<Witch, WitchModel<Witch>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "textures/entity/witch.png");

    public RedesignedWitchRenderer(EntityRendererProvider.Context context) {
        super(context, new WitchModel<>(context.bakeLayer(WildscapesModelLayers.WITCH)), 0.5F);
        this.addLayer(new WitchItemLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public void render(Witch entity, float entityYaw, float partialTicks, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        this.model.setHoldingItem(!entity.getMainHandItem().isEmpty());
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(Witch entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(Witch livingEntity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
    }
}
