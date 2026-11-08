package com.wildscapes.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.wildscapes.Wildscapes;
import com.wildscapes.block.entity.CauldronOfSoulsBlockEntity;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;

public class CauldronOfSoulsRenderer implements BlockEntityRenderer<CauldronOfSoulsBlockEntity, CauldronOfSoulsRenderer.State> {
    private static final SpriteId LID = new SpriteId(TextureAtlas.LOCATION_BLOCKS,
            Wildscapes.id("block/cauldron_of_souls_lid"));

    private final SpriteGetter sprites;

    public CauldronOfSoulsRenderer(BlockEntityRendererProvider.Context context) {
        sprites = context.sprites();
    }

    public static class State extends BlockEntityRenderState {
        float lidAngle;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(CauldronOfSoulsBlockEntity be, State state, float partialTick, Vec3 camera,
            ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTick, camera, crumbling);
        state.lidAngle = be.lidAngle(partialTick);
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector nodes, CameraRenderState camera) {
        TextureAtlasSprite sprite = sprites.get(LID);
        int light = state.lightCoords;
        int overlay = OverlayTexture.NO_OVERLAY;

        pose.pushPose();
        pose.translate(15.0F / 16.0F, 18.0F / 16.0F, 0.0F);
        pose.mulPose(Axis.ZP.rotationDegrees(-state.lidAngle));

        float x0 = -14.0F / 16.0F;
        float z0 = 1.0F / 16.0F;
        float z1 = 15.0F / 16.0F;
        float u0 = u(sprite, 1);
        float u1 = u(sprite, 15);
        float v0 = v(sprite, 1);
        float v1 = v(sprite, 15);
        nodes.submitCustomGeometry(pose, RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS), (last, vc) -> {
            corner(vc, last, x0, z0, u0, v0, light, overlay);
            corner(vc, last, x0, z1, u0, v1, light, overlay);
            corner(vc, last, 0.0F, z1, u1, v1, light, overlay);
            corner(vc, last, 0.0F, z0, u1, v0, light, overlay);
        });
        pose.popPose();
    }

    private static void corner(VertexConsumer vc, PoseStack.Pose pose, float x, float z, float u, float v,
            int light, int overlay) {
        vc.addVertex(pose, x, 0.0F, z).setColor(-1).setUv(u, v).setOverlay(overlay).setLight(light)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    private static float u(TextureAtlasSprite sprite, int px) {
        return sprite.getU0() + (sprite.getU1() - sprite.getU0()) * px / 16.0F;
    }

    private static float v(TextureAtlasSprite sprite, int px) {
        return sprite.getV0() + (sprite.getV1() - sprite.getV0()) * px / 16.0F;
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }
}
