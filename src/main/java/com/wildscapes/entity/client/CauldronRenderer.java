package com.wildscapes.entity.client;

import com.wildscapes.Wildscapes;
import com.wildscapes.block.WildscapesCauldronBlock;
import com.wildscapes.block.entity.CauldronBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class CauldronRenderer implements BlockEntityRenderer<CauldronBlockEntity, CauldronRenderer.State> {
    private static final SpriteId WATER = surface(Identifier.withDefaultNamespace("block/water_still"));
    private static final SpriteId CONCOCTION = surface(Identifier.fromNamespaceAndPath(Wildscapes.MODID, "block/concoction"));
    private static final SpriteId JUMBLE = surface(Identifier.fromNamespaceAndPath(Wildscapes.MODID, "block/jumble_concoction"));

    private static SpriteId surface(Identifier texture) {
        return new SpriteId(TextureAtlas.LOCATION_BLOCKS, texture);
    }

    private final SpriteGetter sprites;
    private final ItemModelResolver items;

    public CauldronRenderer(BlockEntityRendererProvider.Context context) {
        sprites = context.sprites();
        items = context.itemModelResolver();
    }

    public static class State extends BlockEntityRenderState {
        boolean filled;
        boolean boiling;
        boolean brew;
        boolean mixing;
        int color;
        float surface;
        float itemY;
        float itemScale;
        float spin;
        final ItemStackRenderState ingredient = new ItemStackRenderState();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(CauldronBlockEntity be, State state, float partialTick, Vec3 camera,
            ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTick, camera, crumbling);
        int fill = be.getFillLevel();
        state.filled = fill > 0 && be.getContents() != CauldronBlockEntity.Contents.EMPTY;
        state.ingredient.clear();
        if (!state.filled) {
            return;
        }
        state.boiling = be.getBlockState().getValue(WildscapesCauldronBlock.BOILING);
        state.surface = CauldronBlockEntity.surfaceHeight(state.boiling, fill);
        state.brew = be.getContents() == CauldronBlockEntity.Contents.BREW;
        state.mixing = be.isMixing();
        state.color = be.getSurfaceColor();
        extractIngredient(be, state, partialTick);
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector nodes, CameraRenderState camera) {
        if (!state.filled) {
            return;
        }
        renderSurface(state, pose, nodes);
        if (!state.ingredient.isEmpty()) {
            pose.pushPose();
            pose.translate(0.5F, state.itemY, 0.5F);
            pose.scale(state.itemScale, state.itemScale, state.itemScale);
            pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(state.spin));
            state.ingredient.submit(pose, nodes, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        }
    }

    private void renderSurface(State state, PoseStack pose, SubmitNodeCollector nodes) {
        boolean brew = state.brew;
        SpriteId material = brew ? (state.mixing ? JUMBLE : CONCOCTION) : WATER;
        TextureAtlasSprite sprite = sprites.get(material);
        int color = brew ? 0xFFFFFF : state.color;
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        float a = brew ? 1.0F : 0.9F;

        float min = state.boiling ? 0.0625F : 0.1875F;
        float max = 1.0F - min;
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();
        nodes.submitCustomGeometry(pose, RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS),
                (p, vc) -> quad(vc, p, min, max, state.surface, min, max, r, g, b, a, u0, u1, v0, v1, state.lightCoords));
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

    private void extractIngredient(CauldronBlockEntity be, State state, float partialTick) {
        ItemStack ingredient = be.getPendingIngredient();
        if (ingredient.isEmpty() || be.getLevel() == null) {
            return;
        }
        float time = be.getLevel().getGameTime() + partialTick;
        float itemY = state.surface + 0.12F;
        float scale = 0.5F;
        float spin = time * 2.0F;

        if (be.isMixing()) {
            long start = be.getClientMixStart();
            float elapsed = start < 0 ? 0.0F : time - start;
            float t = Math.min(Math.max(elapsed / 25.0F, 0.0F), 1.0F);
            if (t >= 1.0F) {
                return;
            }
            itemY = state.surface + 0.12F - t * 0.5F;
            scale = 0.5F * (1.0F - 0.35F * t);
            spin = time * 2.0F + t * 540.0F;
        } else {
            itemY += (float) Math.sin(time * 0.1F) * 0.03F;
        }

        state.itemY = itemY;
        state.itemScale = scale;
        state.spin = spin;
        items.updateForTopItem(state.ingredient, ingredient, ItemDisplayContext.GROUND, be.getLevel(), null, 0);
    }
}
