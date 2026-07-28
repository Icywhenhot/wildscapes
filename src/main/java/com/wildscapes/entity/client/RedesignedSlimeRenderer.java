package com.wildscapes.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wildscapes.Wildscapes;

import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Slime;

/**
 * Replaces vanilla's slime renderer everywhere. Vanilla bakes one 1-block slime and
 * multiplies it by {@link Slime#getSize()}; the redesign ships a separately modelled and
 * textured slime for each of the three natural sizes, so this picks the closest one and
 * only scales for off-size slimes (the ones commands and datapacks can make).
 *
 * <p>The squish-on-landing wobble is vanilla's, reproduced verbatim.
 */
public class RedesignedSlimeRenderer extends MobRenderer<Slime, SlimeModel<Slime>> {
    /** Slime sizes the three redesigned models were drawn at: small, medium, big. */
    static final int[] MODELLED_SIZES = {1, 2, 4};

    private static final ResourceLocation[] TEXTURES = {
            texture("slime_small"), texture("slime_medium"), texture("slime_large"),
    };

    private final SlimeModel<Slime>[] innerModels;

    @SuppressWarnings("unchecked")
    public RedesignedSlimeRenderer(EntityRendererProvider.Context context) {
        super(context, new SlimeModel<>(context.bakeLayer(WildscapesModelLayers.SLIME_SMALL)), 0.25F);
        this.innerModels = new SlimeModel[] {
                this.model,
                new SlimeModel<>(context.bakeLayer(WildscapesModelLayers.SLIME_MEDIUM)),
                new SlimeModel<>(context.bakeLayer(WildscapesModelLayers.SLIME_LARGE)),
        };
        this.addLayer(new RedesignedSlimeOuterLayer(this, context.getModelSet()));
    }

    /** Which of the three redesigned models a slime of this size should use. */
    static int modelIndex(Slime slime) {
        int size = slime.getSize();
        if (size <= 1) {
            return 0;
        }
        return size <= 3 ? 1 : 2;
    }

    @Override
    public void render(Slime entity, float entityYaw, float partialTicks, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        this.shadowRadius = 0.25F * entity.getSize();
        this.model = this.innerModels[modelIndex(entity)];
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    protected void scale(Slime livingEntity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(0.999F, 0.999F, 0.999F);
        poseStack.translate(0.0F, 0.001F, 0.0F);
        float size = livingEntity.getSize();
        float squish = Mth.lerp(partialTickTime, livingEntity.oSquish, livingEntity.squish) / (size * 0.5F + 1.0F);
        float stretch = 1.0F / (squish + 1.0F);
        // The model is already the right size for its tier, so only the leftover ratio
        // needs scaling — 1.0 for the three natural sizes.
        float ratio = size / MODELLED_SIZES[modelIndex(livingEntity)];
        poseStack.scale(stretch * ratio, 1.0F / stretch * ratio, stretch * ratio);
    }

    @Override
    public ResourceLocation getTextureLocation(Slime entity) {
        return TEXTURES[modelIndex(entity)];
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "textures/entity/slime/" + name + ".png");
    }
}
