package com.wildscapes.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.monster.slime.SlimeModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;

public class RedesignedSlimeOuterLayer extends RenderLayer<SlimeRenderState, SlimeModel> {
    private final SlimeModel[] outerModels;
    private final RedesignedSlimeRenderer renderer;

    public RedesignedSlimeOuterLayer(RedesignedSlimeRenderer renderer, EntityModelSet modelSet) {
        super(renderer);
        this.renderer = renderer;
        this.outerModels = new SlimeModel[] {
                new SlimeModel(modelSet.bakeLayer(WildscapesModelLayers.SLIME_SMALL_OUTER)),
                new SlimeModel(modelSet.bakeLayer(WildscapesModelLayers.SLIME_MEDIUM_OUTER)),
                new SlimeModel(modelSet.bakeLayer(WildscapesModelLayers.SLIME_LARGE_OUTER)),
        };
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector nodes, int light, SlimeRenderState state, float yaw, float pitch) {
        boolean glowing = state.appearsGlowing() && state.isInvisible;
        if (state.isInvisible && !glowing) {
            return;
        }
        var texture = renderer.getTextureLocation(state);
        RenderType type = glowing ? RenderTypes.outline(texture) : RenderTypes.entityTranslucent(texture);
        SlimeModel outer = outerModels[RedesignedSlimeRenderer.modelIndex(state)];
        nodes.order(1).submitModel(outer, state, pose, type, light, LivingEntityRenderer.getOverlayCoords(state, 0.0F),
                renderer.getModelTint(state), null, state.outlineColor, null);
    }
}
