package com.wildscapes.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wildscapes.Wildscapes;
import com.wildscapes.entity.SlimeMerging;

import net.minecraft.client.model.monster.slime.SlimeModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.AbstractCubeMobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.cubemob.Slime;

public class RedesignedSlimeRenderer extends AbstractCubeMobRenderer<Slime, SlimeRenderState, SlimeModel> {
    static final int[] MODELLED_SIZES = {1, 2, 4};

    private static final Identifier[] TEXTURES = {
            texture("slime_small"), texture("slime_medium"), texture("slime_large"),
    };

    private final SlimeModel[] innerModels;

    public RedesignedSlimeRenderer(EntityRendererProvider.Context context) {
        super(context, new SlimeModel(context.bakeLayer(WildscapesModelLayers.SLIME_SMALL)));
        this.innerModels = new SlimeModel[] {
                this.model,
                new SlimeModel(context.bakeLayer(WildscapesModelLayers.SLIME_MEDIUM)),
                new SlimeModel(context.bakeLayer(WildscapesModelLayers.SLIME_LARGE)),
        };
        this.addLayer(new RedesignedSlimeOuterLayer(this, context.getModelSet()));
    }

    static int modelIndex(SlimeRenderState state) {
        int size = state.size;
        if (size <= 1) {
            return 0;
        }
        return size <= 3 ? 1 : 2;
    }

    @Override
    public SlimeRenderState createRenderState() {
        return new SlimeRenderState();
    }

    @Override
    public void extractRenderState(Slime slime, SlimeRenderState state, float partialTick) {
        super.extractRenderState(slime, state, partialTick);
        state.setData(MobVisuals.MERGE_ALPHA, SlimeMerging.mergeAlpha(slime));
    }

    @Override
    public void submit(SlimeRenderState state, PoseStack pose, SubmitNodeCollector nodes, CameraRenderState camera) {
        this.model = innerModels[modelIndex(state)];
        super.submit(state, pose, nodes, camera);
    }

    @Override
    protected float getShadowRadius(SlimeRenderState state) {
        return 0.25F * state.size;
    }

    @Override
    protected int getModelTint(SlimeRenderState state) {
        return ((int) (255 * state.getDataOrDefault(MobVisuals.MERGE_ALPHA, 1.0F)) << 24) | 0xFFFFFF;
    }

    @Override
    protected RenderType getRenderType(SlimeRenderState state, boolean bodyVisible, boolean translucent, boolean glowing) {
        return state.getDataOrDefault(MobVisuals.MERGE_ALPHA, 1.0F) < 1.0F
                ? RenderTypes.entityTranslucent(getTextureLocation(state))
                : super.getRenderType(state, bodyVisible, translucent, glowing);
    }

    @Override
    protected void scale(SlimeRenderState state, PoseStack poseStack) {
        poseStack.scale(0.999F, 0.999F, 0.999F);
        poseStack.translate(0.0F, 0.001F, 0.0F);
        float size = state.size;
        float squish = state.squish / (size * 0.5F + 1.0F);
        float stretch = 1.0F / (squish + 1.0F);

        float ratio = size / MODELLED_SIZES[modelIndex(state)];
        poseStack.scale(stretch * ratio, 1.0F / stretch * ratio, stretch * ratio);
    }

    @Override
    public Identifier getTextureLocation(SlimeRenderState state) {
        return TEXTURES[modelIndex(state)];
    }

    private static Identifier texture(String name) {
        return Identifier.fromNamespaceAndPath(Wildscapes.MODID, "textures/entity/slime/" + name + ".png");
    }
}
