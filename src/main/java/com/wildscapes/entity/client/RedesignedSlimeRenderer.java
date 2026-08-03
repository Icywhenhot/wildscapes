package com.wildscapes.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wildscapes.Wildscapes;
import com.wildscapes.entity.SlimeMerging;

import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
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

        // Merging slimes fade out as they shrink into each other, and the one they turn
        // into fades in. Wrapping the buffer dims every layer, the outer shell included,
        // without having to reimplement the vanilla render path.
        float alpha = SlimeMerging.mergeAlpha(entity);
        MultiBufferSource sink = alpha < 1.0F ? new FadingBufferSource(buffer, alpha) : buffer;
        super.render(entity, entityYaw, partialTicks, poseStack, sink, packedLight);
    }

    @Override
    protected RenderType getRenderType(Slime entity, boolean bodyVisible, boolean translucent, boolean glowing) {
        // The inner body is a cutout normally, and cutouts cannot be faded.
        return SlimeMerging.mergeAlpha(entity) < 1.0F
                ? RenderType.entityTranslucent(this.getTextureLocation(entity))
                : super.getRenderType(entity, bodyVisible, translucent, glowing);
    }

    /** Passes every buffer through {@link FadingConsumer}. */
    private record FadingBufferSource(MultiBufferSource delegate, float alpha) implements MultiBufferSource {
        @Override
        public VertexConsumer getBuffer(RenderType renderType) {
            return new FadingConsumer(this.delegate.getBuffer(renderType), this.alpha);
        }
    }

    /** Forwards vertices untouched apart from scaling their alpha. */
    private record FadingConsumer(VertexConsumer delegate, float alpha) implements VertexConsumer {
        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            this.delegate.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int vertexAlpha) {
            this.delegate.setColor(red, green, blue, (int) (vertexAlpha * this.alpha));
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            this.delegate.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            this.delegate.setUv1(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            this.delegate.setUv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
            this.delegate.setNormal(normalX, normalY, normalZ);
            return this;
        }
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
