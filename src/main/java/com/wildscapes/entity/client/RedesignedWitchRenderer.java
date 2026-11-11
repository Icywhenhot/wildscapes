package com.wildscapes.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wildscapes.Wildscapes;
import com.wildscapes.entity.SoulHarvest;

import net.minecraft.client.model.WitchModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.WitchItemLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Witch;

public class RedesignedWitchRenderer extends MobRenderer<Witch, WitchModel<Witch>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "textures/entity/witch.png");
    private static final ResourceLocation EMPOWERED =
            ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "textures/entity/witch_empowered.png");
    private static final ResourceLocation VANILLA = ResourceLocation.withDefaultNamespace("textures/entity/witch.png");

    private final boolean vanilla;

    public RedesignedWitchRenderer(EntityRendererProvider.Context context) {
        this(context, MobLook.vanilla(context, "witch", WildscapesModelLayers.WITCH, VANILLA));
    }

    private RedesignedWitchRenderer(EntityRendererProvider.Context context, boolean vanilla) {
        super(context, vanilla ? new WitchModel<>(context.bakeLayer(ModelLayers.WITCH))
                : new ChantingWitchModel(context.bakeLayer(WildscapesModelLayers.WITCH)), 0.5F);
        this.vanilla = vanilla;
        this.addLayer(new WitchItemLayer<>(this, context.getItemInHandRenderer()));
        if (!vanilla) {
            this.addLayer(new EmpoweredGlowLayer<>(this, "witch_empowered_glow"));
        }
    }

    @Override
    public void render(Witch entity, float entityYaw, float partialTicks, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        this.model.setHoldingItem(!entity.getMainHandItem().isEmpty());
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(Witch entity) {
        if (vanilla) {
            return VANILLA;
        }
        return SoulHarvest.isEmpowered(entity) ? EMPOWERED : TEXTURE;
    }

    @Override
    protected void scale(Witch livingEntity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
    }
}
