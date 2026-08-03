package com.wildscapes.entity.client;

import com.wildscapes.Wildscapes;
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

/**
 * Draws the dynamic part of a boiling/enhanced cauldron that the baked block model can't: the
 * coloured liquid surface (plain or dyed water, a stored potion's colour, or the bubbling
 * concoction — churning while it is being stirred) and the ingredient bobbing inside while it
 * waits for a stir.
 */
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

        float y = 0.42F + 0.16F * fill;
        renderSurface(be, pose, buffer, packedLight, y);
        renderPendingIngredient(be, partialTick, pose, buffer, packedLight, packedOverlay, y);
    }

    private void renderSurface(CauldronBlockEntity be, PoseStack pose, MultiBufferSource buffer, int light, float y) {
        boolean brew = be.getContents() == CauldronBlockEntity.Contents.BREW;
        Material material = brew ? (be.isMixing() ? JUMBLE : CONCOCTION) : WATER;
        TextureAtlasSprite sprite = material.sprite();
        VertexConsumer vc = buffer.getBuffer(RenderType.translucent());

        // Brew textures are drawn as-is; water/potion is tinted to its colour.
        int color = brew ? 0xFFFFFF : be.getSurfaceColor();
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        float a = brew ? 1.0F : 0.9F;

        float min = 0.125F;
        float max = 0.875F;
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
        float time = (be.getLevel().getGameTime() + partialTick);
        float bob = (float) Math.sin(time * 0.1F) * 0.03F;

        pose.pushPose();
        pose.translate(0.5F, y + 0.12F + bob, 0.5F);
        pose.scale(0.5F, 0.5F, 0.5F);
        pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(time * 2.0F));
        minecraft.getItemRenderer().renderStatic(ingredient, ItemDisplayContext.GROUND, light, overlay,
                pose, buffer, be.getLevel(), 0);
        pose.popPose();
    }
}
