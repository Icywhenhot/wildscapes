package com.wildscapes.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import com.wildscapes.Wildscapes;

import net.minecraft.client.model.monster.illager.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.world.entity.monster.illager.Pillager;

public class SwampPillagerRenderer extends IllagerRenderer<Pillager, IllagerRenderState> {
    private static final Identifier VANILLA =
            Identifier.withDefaultNamespace("textures/entity/illager/pillager.png");
    private static final Identifier SWAMP =
            Identifier.fromNamespaceAndPath(Wildscapes.MODID, "textures/entity/illager/pillager_swamp.png");
    private static final Identifier EMPOWERED =
            Identifier.fromNamespaceAndPath(Wildscapes.MODID, "textures/entity/illager/pillager_empowered.png");

    private final IllagerModel<IllagerRenderState> vanillaModel;
    private final IllagerModel<IllagerRenderState> swampModel;

    public SwampPillagerRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerModel<>(context.bakeLayer(ModelLayers.PILLAGER)), 0.5F);
        this.vanillaModel = this.model;
        this.swampModel = new IllagerModel<>(context.bakeLayer(WildscapesModelLayers.PILLAGER_SWAMP));
        this.addLayer(new ItemInHandLayer<>(this));
        this.addLayer(new EmpoweredGlowLayer<>(this, "illager/pillager_empowered_glow"));
    }

    @Override
    public void submit(IllagerRenderState state, PoseStack poseStack, SubmitNodeCollector nodes, CameraRenderState camera) {
        this.model = state.getDataOrDefault(MobVisuals.SWAMP, false) || state.getDataOrDefault(MobVisuals.EMPOWERED, false) ? this.swampModel : this.vanillaModel;
        super.submit(state, poseStack, nodes, camera);
    }

    @Override
    public IllagerRenderState createRenderState() {
        return new IllagerRenderState();
    }

    @Override
    public Identifier getTextureLocation(IllagerRenderState state) {
        if (state.getDataOrDefault(MobVisuals.EMPOWERED, false)) {
            return EMPOWERED;
        }
        return state.getDataOrDefault(MobVisuals.SWAMP, false) ? SWAMP : VANILLA;
    }
}
