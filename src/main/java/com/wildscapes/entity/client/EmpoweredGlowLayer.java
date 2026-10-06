package com.wildscapes.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wildscapes.Wildscapes;
import com.wildscapes.entity.SoulHarvest;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Illusioner;

public class EmpoweredGlowLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private final RenderType glow;

    public EmpoweredGlowLayer(RenderLayerParent<T, M> renderer, String texture) {
        super(renderer);
        glow = RenderType.entityTranslucentEmissive(ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID,
                "textures/entity/" + texture + ".png"));
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffer, int light, T entity, float limbSwing,
            float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!SoulHarvest.isEmpowered(entity) || entity.isInvisible() && !(entity instanceof Illusioner)) {
            return;
        }
        getParentModel().renderToBuffer(pose, buffer.getBuffer(glow), LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY, -1);
    }
}
