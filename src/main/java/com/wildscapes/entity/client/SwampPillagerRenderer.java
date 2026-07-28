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
import net.minecraft.world.entity.monster.Pillager;

/**
 * Renders pillagers, swapping in the swamp geometry and texture for the ones that were
 * born in a marsh. Everything else — the crossbow poses, the held item, the head layer —
 * is vanilla {@link IllagerRenderer} behaviour on whichever model is currently selected.
 */
public class SwampPillagerRenderer extends IllagerRenderer<Pillager> {
    private static final ResourceLocation VANILLA =
            ResourceLocation.withDefaultNamespace("textures/entity/illager/pillager.png");
    private static final ResourceLocation SWAMP =
            ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "textures/entity/illager/pillager_swamp.png");

    private final IllagerModel<Pillager> vanillaModel;
    private final IllagerModel<Pillager> swampModel;

    public SwampPillagerRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerModel<>(context.bakeLayer(ModelLayers.PILLAGER)), 0.5F);
        this.vanillaModel = this.model;
        this.swampModel = new IllagerModel<>(context.bakeLayer(WildscapesModelLayers.PILLAGER_SWAMP));
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public void render(Pillager entity, float entityYaw, float partialTicks, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        // Swapped before super.render so the render layers, which read getParentModel(),
        // pose the same model the body is drawn with.
        this.model = SwampVariants.isSwampBorn(entity) ? this.swampModel : this.vanillaModel;
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(Pillager entity) {
        return SwampVariants.isSwampBorn(entity) ? SWAMP : VANILLA;
    }
}
