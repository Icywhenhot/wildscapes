package com.wildscapes.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.wildscapes.block.CypressShelfBlock;
import com.wildscapes.block.entity.CypressShelfBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;

public class CypressShelfRenderer implements BlockEntityRenderer<CypressShelfBlockEntity> {
    public CypressShelfRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(CypressShelfBlockEntity shelf, float partialTick, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(-shelf.getBlockState().getValue(CypressShelfBlock.FACING).toYRot()));
        for (int i = 0; i < 3; i++) {
            var stack = shelf.getItem(i);
            if (stack.isEmpty()) continue;
            pose.pushPose();
            pose.translate((i - 1) * 0.3125, shelf.getAlignItemsToBottom() ? -0.125 : 0, 0.25);
            pose.scale(0.375F, 0.375F, 0.375F);
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, overlay,
                    pose, buffers, shelf.getLevel(), i);
            pose.popPose();
        }
        pose.popPose();
    }
}
