package com.wildscapes.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.wildscapes.Wildscapes;
import com.wildscapes.block.entity.CauldronOfSoulsBlockEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.AABB;

public class CauldronOfSoulsRenderer implements BlockEntityRenderer<CauldronOfSoulsBlockEntity> {
    private static final Material LID = new Material(InventoryMenu.BLOCK_ATLAS,
            ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "block/cauldron_of_souls_lid"));

    public CauldronOfSoulsRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(CauldronOfSoulsBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffer,
            int light, int overlay) {
        TextureAtlasSprite sprite = LID.sprite();
        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS));

        pose.pushPose();
        pose.translate(15.0F / 16.0F, 18.0F / 16.0F, 0.0F);
        pose.mulPose(Axis.ZP.rotationDegrees(-be.lidAngle(partialTick)));
        PoseStack.Pose last = pose.last();

        float x0 = -14.0F / 16.0F;
        float z0 = 1.0F / 16.0F;
        float z1 = 15.0F / 16.0F;
        float u0 = u(sprite, 1);
        float u1 = u(sprite, 15);
        float v0 = v(sprite, 1);
        float v1 = v(sprite, 15);
        corner(vc, last, x0, z0, u0, v0, light, overlay);
        corner(vc, last, x0, z1, u0, v1, light, overlay);
        corner(vc, last, 0.0F, z1, u1, v1, light, overlay);
        corner(vc, last, 0.0F, z0, u1, v0, light, overlay);
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
    public AABB getRenderBoundingBox(CauldronOfSoulsBlockEntity be) {
        return new AABB(be.getBlockPos()).expandTowards(0.0, 1.2, 0.0).inflate(0.1);
    }
}
