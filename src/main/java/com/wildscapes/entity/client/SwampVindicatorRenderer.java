package com.wildscapes.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wildscapes.Wildscapes;
import com.wildscapes.entity.SwampVariants;

import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Vindicator;

/** As {@link SwampPillagerRenderer}, for vindicators. */
public class SwampVindicatorRenderer extends IllagerRenderer<Vindicator> {
    private static final ResourceLocation VANILLA =
            ResourceLocation.withDefaultNamespace("textures/entity/illager/vindicator.png");
    private static final ResourceLocation SWAMP =
            ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "textures/entity/illager/vindicator_swamp.png");

    private final IllagerModel<Vindicator> vanillaModel;
    private final IllagerModel<Vindicator> swampModel;

    public SwampVindicatorRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerModel<>(context.bakeLayer(ModelLayers.VINDICATOR)), 0.5F);
        this.vanillaModel = this.model;
        this.swampModel = new IllagerModel<>(context.bakeLayer(WildscapesModelLayers.VINDICATOR_SWAMP));
        // Vanilla only draws the axe while the vindicator is aggressive; otherwise its
        // arms are crossed and there is no hand to put it in.
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
        this.model = SwampVariants.isSwampBorn(entity) ? this.swampModel : this.vanillaModel;
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(Vindicator entity) {
        return SwampVariants.isSwampBorn(entity) ? SWAMP : VANILLA;
    }
}
