package com.wildscapes.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wildscapes.Wildscapes;
import com.wildscapes.entity.WitchRitual;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.WitchRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.entity.state.WitchRenderState;
import net.minecraft.world.entity.monster.Witch;

public class RedesignedWitchRenderer extends WitchRenderer {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Wildscapes.MODID, "textures/entity/witch.png");
    private static final Identifier EMPOWERED =
            Identifier.fromNamespaceAndPath(Wildscapes.MODID, "textures/entity/witch_empowered.png");
    private static final Identifier VANILLA = Identifier.withDefaultNamespace("textures/entity/witch/witch.png");

    private final boolean vanilla;

    public RedesignedWitchRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.vanilla = MobLook.vanilla(context, "witch", WildscapesModelLayers.WITCH, VANILLA);
        if (!vanilla) {
            this.model = new ChantingWitchModel(context.bakeLayer(WildscapesModelLayers.WITCH));
            this.addLayer(new EmpoweredGlowLayer<>(this, "witch_empowered_glow"));
        }
    }

    @Override
    public void extractRenderState(Witch witch, WitchRenderState state, float partialTick) {
        super.extractRenderState(witch, state, partialTick);
        state.setRenderData(MobVisuals.CHANTING, WitchRitual.isChanting(witch));
    }

    @Override
    public Identifier getTextureLocation(WitchRenderState state) {
        if (vanilla) {
            return VANILLA;
        }
        return state.getRenderDataOrDefault(MobVisuals.EMPOWERED, false) ? EMPOWERED : TEXTURE;
    }

    @Override
    protected void scale(WitchRenderState state, PoseStack poseStack) {
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
    }
}
