package com.wildscapes.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wildscapes.Wildscapes;
import com.wildscapes.entity.Incursion;
import com.wildscapes.entity.SwampVariants;

import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Vindicator;

public class SwampVindicatorRenderer extends IllagerRenderer<Vindicator> {
    private static final ResourceLocation VANILLA =
            ResourceLocation.withDefaultNamespace("textures/entity/illager/vindicator.png");
    private static final ResourceLocation SWAMP =
            ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "textures/entity/illager/vindicator_swamp.png");
    private static final ResourceLocation EMPOWERED =
            ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "textures/entity/illager/vindicator_empowered.png");

    private final IllagerModel<Vindicator> vanillaModel;
    private final IllagerModel<Vindicator> swampModel;

    public SwampVindicatorRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerModel<>(context.bakeLayer(ModelLayers.VINDICATOR)), 0.5F);
        this.vanillaModel = this.model;
        this.swampModel = new IllagerModel<>(context.bakeLayer(WildscapesModelLayers.VINDICATOR_SWAMP));

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
    }

    @Override
    public void render(Vindicator entity, float entityYaw, float partialTicks, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        this.model = SwampVariants.isSwampBorn(entity) || Incursion.isEmpowered(entity) ? this.swampModel : this.vanillaModel;
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(Vindicator entity) {
        if (Incursion.isEmpowered(entity)) {
            return EMPOWERED;
        }
        return SwampVariants.isSwampBorn(entity) ? SWAMP : VANILLA;
    }
}
