package com.wildscapes.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wildscapes.Wildscapes;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.IllusionerRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;

public class EmpoweredGlowLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {
    private final RenderType glow;

    public EmpoweredGlowLayer(RenderLayerParent<S, M> renderer, String texture) {
        super(renderer);
        glow = RenderTypes.entityTranslucentEmissive(Wildscapes.id("textures/entity/" + texture + ".png"));
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector nodes, int light, S state, float yaw, float pitch) {
        if (state.getDataOrDefault(MobVisuals.EMPOWERED, false) && (!state.isInvisible || state instanceof IllusionerRenderState)) {
            nodes.order(1).submitModel(getParentModel(), state, pose, glow, LightCoordsUtil.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        }
    }
}
