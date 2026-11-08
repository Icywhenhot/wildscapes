package com.wildscapes.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import com.wildscapes.Wildscapes;

import net.minecraft.client.model.monster.illager.IllagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.world.entity.monster.illager.Vindicator;

public class SwampVindicatorRenderer extends IllagerRenderer<Vindicator, IllagerRenderState> {
    private static final Identifier DEFAULT =
            Identifier.fromNamespaceAndPath(Wildscapes.MODID, "textures/entity/illager/vindicator.png");
    private static final Identifier SWAMP =
            Identifier.fromNamespaceAndPath(Wildscapes.MODID, "textures/entity/illager/vindicator_swamp.png");
    private static final Identifier EMPOWERED =
            Identifier.fromNamespaceAndPath(Wildscapes.MODID, "textures/entity/illager/vindicator_empowered.png");

    public SwampVindicatorRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerModel<>(context.bakeLayer(WildscapesModelLayers.VINDICATOR)), 0.5F);

        this.addLayer(new ItemInHandLayer<>(this) {
            @Override
            public void submit(PoseStack pose, SubmitNodeCollector nodes, int light, IllagerRenderState state, float yaw, float pitch) {
                if (state.isAggressive) {
                    super.submit(pose, nodes, light, state, yaw, pitch);
                }
            }
        });
        this.addLayer(new EmpoweredGlowLayer<>(this, "illager/vindicator_empowered_glow"));
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
        return state.getDataOrDefault(MobVisuals.SWAMP, false) ? SWAMP : DEFAULT;
    }
}
