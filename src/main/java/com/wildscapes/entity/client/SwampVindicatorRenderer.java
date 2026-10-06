package com.wildscapes.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wildscapes.Wildscapes;
import com.wildscapes.entity.SoulHarvest;
import com.wildscapes.entity.SwampVariants;

import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Vindicator;

public class SwampVindicatorRenderer extends IllagerRenderer<Vindicator> {
    private static final ResourceLocation DEFAULT =
            ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "textures/entity/illager/vindicator.png");
    private static final ResourceLocation SWAMP =
            ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "textures/entity/illager/vindicator_swamp.png");
    private static final ResourceLocation EMPOWERED =
            ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "textures/entity/illager/vindicator_empowered.png");

    public SwampVindicatorRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerModel<>(context.bakeLayer(WildscapesModelLayers.VINDICATOR)), 0.5F);

        this.addLayer(new ItemInHandLayer<Vindicator, IllagerModel<Vindicator>>(this, context.getItemInHandRenderer()) {
            @Override
            public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Vindicator entity,
                    float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                    float netHeadYaw, float headPitch) {
                if (entity.isAggressive()) {
                    super.render(poseStack, buffer, packedLight, entity, limbSwing, limbSwingAmount,
                            partialTicks, ageInTicks, netHeadYaw, headPitch);
                }
            }
        });
        this.addLayer(new EmpoweredGlowLayer<>(this, "illager/vindicator_empowered_glow"));
    }

    @Override
    public ResourceLocation getTextureLocation(Vindicator entity) {
        if (SoulHarvest.isEmpowered(entity)) {
            return EMPOWERED;
        }
        return SwampVariants.isSwampBorn(entity) ? SWAMP : DEFAULT;
    }
}
