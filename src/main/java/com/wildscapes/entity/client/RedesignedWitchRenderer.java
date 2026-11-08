package com.wildscapes.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wildscapes.Wildscapes;
import com.wildscapes.entity.WitchRitual;

import net.minecraft.client.model.monster.witch.WitchModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.WitchItemLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.entity.state.WitchRenderState;
import net.minecraft.world.entity.monster.Witch;

public class RedesignedWitchRenderer extends MobRenderer<Witch, WitchRenderState, WitchModel> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Wildscapes.MODID, "textures/entity/witch.png");
    private static final Identifier EMPOWERED =
            Identifier.fromNamespaceAndPath(Wildscapes.MODID, "textures/entity/witch_empowered.png");

    public RedesignedWitchRenderer(EntityRendererProvider.Context context) {
        super(context, new ChantingWitchModel(context.bakeLayer(WildscapesModelLayers.WITCH)), 0.5F);
        this.addLayer(new WitchItemLayer(this));
        this.addLayer(new EmpoweredGlowLayer<>(this, "witch_empowered_glow"));
    }

    @Override
    public WitchRenderState createRenderState() {
        return new WitchRenderState();
    }

    @Override
    public void extractRenderState(Witch witch, WitchRenderState state, float partialTick) {
        super.extractRenderState(witch, state, partialTick);
        net.minecraft.client.renderer.entity.state.HoldingEntityRenderState.extractHoldingEntityRenderState(witch, state, itemModelResolver);
        state.entityId = witch.getId();
        state.isHoldingItem = !witch.getMainHandItem().isEmpty();
        state.isHoldingPotion = witch.getMainHandItem().is(net.minecraft.world.item.Items.POTION);
        state.setData(MobVisuals.CHANTING, WitchRitual.isChanting(witch));
    }

    @Override
    public Identifier getTextureLocation(WitchRenderState state) {
        return state.getDataOrDefault(MobVisuals.EMPOWERED, false) ? EMPOWERED : TEXTURE;
    }

    @Override
    protected void scale(WitchRenderState state, PoseStack poseStack) {
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
    }
}
