package com.wildscapes.entity.client;

import com.wildscapes.Wildscapes;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EvokerRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.entity.state.EvokerRenderState;
import net.minecraft.world.entity.monster.illager.Evoker;

public class EmpoweredEvokerRenderer extends EvokerRenderer<Evoker> {
    private static final Identifier EMPOWERED =
            Identifier.fromNamespaceAndPath(Wildscapes.MODID, "textures/entity/illager/evoker_empowered.png");

    public EmpoweredEvokerRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.addLayer(new EmpoweredGlowLayer<>(this, "illager/evoker_empowered_glow"));
    }

    @Override
    public Identifier getTextureLocation(EvokerRenderState state) {
        return state.getDataOrDefault(MobVisuals.EMPOWERED, false) ? EMPOWERED : super.getTextureLocation(state);
    }
}
