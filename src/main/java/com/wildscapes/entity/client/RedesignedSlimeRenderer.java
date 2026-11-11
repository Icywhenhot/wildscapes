package com.wildscapes.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wildscapes.Wildscapes;
import com.wildscapes.entity.SlimeMerging;

import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.SlimeOuterLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.client.model.pipeline.VertexConsumerWrapper;
import net.minecraft.world.entity.monster.Slime;

public class RedesignedSlimeRenderer extends MobRenderer<Slime, SlimeModel<Slime>> {
    static final int[] MODELLED_SIZES = {1, 2, 4};

    private static final ResourceLocation[] TEXTURES = {
            texture("slime_small"), texture("slime_medium"), texture("slime_large"),
    };
    private static final ResourceLocation VANILLA = ResourceLocation.withDefaultNamespace("textures/entity/slime/slime.png");

    private final SlimeModel<Slime>[] innerModels;
    private final boolean vanilla;

    public RedesignedSlimeRenderer(EntityRendererProvider.Context context) {
        this(context, MobLook.vanilla(context, "slime", WildscapesModelLayers.SLIME_SMALL, VANILLA));
    }

    @SuppressWarnings("unchecked")
    private RedesignedSlimeRenderer(EntityRendererProvider.Context context, boolean vanilla) {
        super(context, new SlimeModel<>(context.bakeLayer(vanilla ? ModelLayers.SLIME
                : WildscapesModelLayers.SLIME_SMALL)), 0.25F);
        this.vanilla = vanilla;
        if (vanilla) {
            this.innerModels = new SlimeModel[] {this.model, this.model, this.model};
            this.addLayer(new SlimeOuterLayer<>(this, context.getModelSet()));
            return;
        }
        this.innerModels = new SlimeModel[] {
                this.model,
                new SlimeModel<>(context.bakeLayer(WildscapesModelLayers.SLIME_MEDIUM)),
                new SlimeModel<>(context.bakeLayer(WildscapesModelLayers.SLIME_LARGE)),
        };
        this.addLayer(new RedesignedSlimeOuterLayer(this, context.getModelSet()));
    }

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
        this.shadowRadius = 0.25F * entity.getSize() * SlimeMerging.scale(entity);
        this.model = this.innerModels[modelIndex(entity)];

        float alpha = SlimeMerging.mergeAlpha(entity);
        MultiBufferSource sink = alpha < 1.0F ? new FadingBufferSource(buffer, alpha) : buffer;
        super.render(entity, entityYaw, partialTicks, poseStack, sink, packedLight);
    }

    @Override
    protected RenderType getRenderType(Slime entity, boolean bodyVisible, boolean translucent, boolean glowing) {
        return SlimeMerging.mergeAlpha(entity) < 1.0F
                ? RenderType.entityTranslucent(this.getTextureLocation(entity))
                : super.getRenderType(entity, bodyVisible, translucent, glowing);
    }

    private record FadingBufferSource(MultiBufferSource delegate, float alpha) implements MultiBufferSource {
        @Override
        public VertexConsumer getBuffer(RenderType renderType) {
            return new FadingConsumer(this.delegate.getBuffer(renderType), this.alpha);
        }
    }

    private static class FadingConsumer extends VertexConsumerWrapper {
        private final float alpha;

        private FadingConsumer(VertexConsumer parent, float alpha) {
            super(parent);
            this.alpha = alpha;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int vertexAlpha) {
            parent.color(red, green, blue, (int) (vertexAlpha * alpha));
            return this;
        }

        @Override
        public void defaultColor(int red, int green, int blue, int vertexAlpha) {
            parent.defaultColor(red, green, blue, (int) (vertexAlpha * alpha));
        }
    }

    @Override
    protected void scale(Slime livingEntity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(0.999F, 0.999F, 0.999F);
        poseStack.translate(0.0F, 0.001F, 0.0F);
        float size = livingEntity.getSize();
        float squish = Mth.lerp(partialTickTime, livingEntity.oSquish, livingEntity.squish) / (size * 0.5F + 1.0F);
        float stretch = 1.0F / (squish + 1.0F);

        float modelled = vanilla ? 1.0F : MODELLED_SIZES[modelIndex(livingEntity)];
        float ratio = size / modelled * SlimeMerging.scale(livingEntity);
        poseStack.scale(stretch * ratio, 1.0F / stretch * ratio, stretch * ratio);
    }

    @Override
    public ResourceLocation getTextureLocation(Slime entity) {
        return vanilla ? VANILLA : TEXTURES[modelIndex(entity)];
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "textures/entity/slime/" + name + ".png");
    }
}
