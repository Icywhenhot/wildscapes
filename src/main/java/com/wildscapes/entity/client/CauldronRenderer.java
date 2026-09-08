package com.wildscapes.entity.client;

import com.wildscapes.Wildscapes;
import com.wildscapes.block.WildscapesCauldronBlock;
import com.wildscapes.block.entity.CauldronBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class CauldronRenderer implements BlockEntityRenderer<CauldronBlockEntity> {
    private static final Material WATER = surface(ResourceLocation.withDefaultNamespace("block/water_still"));
    private static final Material CONCOCTION = surface(ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "block/concoction"));
    private static final Material JUMBLE = surface(ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "block/jumble_concoction"));

    private static Material surface(ResourceLocation texture) {
        return new Material(InventoryMenu.BLOCK_ATLAS, texture);
    }

    private final Minecraft minecraft = Minecraft.getInstance();

    public CauldronRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(CauldronBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        int fill = be.getFillLevel();
        if (fill <= 0 || be.getContents() == CauldronBlockEntity.Contents.EMPTY) {
            return;
        }

        boolean boiling = be.getBlockState().getValue(WildscapesCauldronBlock.BOILING);
        float y = CauldronBlockEntity.surfaceHeight(boiling, fill);
        renderSurface(be, pose, buffer, packedLight, y, boiling);
        renderPendingIngredient(be, partialTick, pose, buffer, packedLight, packedOverlay, y);
    }

    private void renderSurface(CauldronBlockEntity be, PoseStack pose, MultiBufferSource buffer, int light,
            float y, boolean boiling) {
        boolean brew = be.getContents() == CauldronBlockEntity.Contents.BREW;
        Material material = brew ? (be.isMixing() ? JUMBLE : CONCOCTION) : WATER;
        TextureAtlasSprite sprite = material.sprite();
        VertexConsumer vc = buffer.getBuffer(RenderType.translucent());

        int color = brew ? 0xFFFFFF : be.getSurfaceColor();
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        float a = brew ? 1.0F : 0.9F;

        float min = boiling ? 0.0625F : 0.1875F;
        float max = 1.0F - min;
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();
        PoseStack.Pose p = pose.last();

        quad(vc, p, min, max, y, min, max, r, g, b, a, u0, u1, v0, v1, light);
    }

    private static void quad(VertexConsumer vc, PoseStack.Pose p, float x0, float x1, float y, float z0, float z1,
            float r, float g, float b, float a, float u0, float u1, float v0, float v1, int light) {
        vertex(vc, p, x0, y, z0, r, g, b, a, u0, v0, light);
        vertex(vc, p, x0, y, z1, r, g, b, a, u0, v1, light);
        vertex(vc, p, x1, y, z1, r, g, b, a, u1, v1, light);
        vertex(vc, p, x1, y, z0, r, g, b, a, u1, v0, light);
    }

    private static void vertex(VertexConsumer vc, PoseStack.Pose p, float x, float y, float z,
            float r, float g, float b, float a, float u, float v, int light) {
        vc.addVertex(p, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(p, 0.0F, 1.0F, 0.0F);
    }

    private void renderPendingIngredient(CauldronBlockEntity be, float partialTick, PoseStack pose,
            MultiBufferSource buffer, int light, int overlay, float y) {
        ItemStack ingredient = be.getPendingIngredient();
        if (ingredient.isEmpty() || be.getLevel() == null) {
            return;
        }
        float time = be.getLevel().getGameTime() + partialTick;
        float itemY = y + 0.12F;
        float scale = 0.5F;
        float spin = time * 2.0F;

        if (be.isMixing()) {
            long start = be.getClientMixStart();
            float elapsed = start < 0 ? 0.0F : time - start;
            float t = Math.min(Math.max(elapsed / 25.0F, 0.0F), 1.0F);
            if (t >= 1.0F) {
                return;
            }
            itemY = y + 0.12F - t * 0.5F;
            scale = 0.5F * (1.0F - 0.35F * t);
            spin = time * 2.0F + t * 540.0F;
        } else {
            itemY += (float) Math.sin(time * 0.1F) * 0.03F;
        }

        pose.pushPose();
        pose.translate(0.5F, itemY, 0.5F);
        pose.scale(scale, scale, scale);
        pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(spin));
        minecraft.getItemRenderer().renderStatic(ingredient, ItemDisplayContext.GROUND, light, overlay,
                pose, buffer, be.getLevel(), 0);
        pose.popPose();
    }
}
