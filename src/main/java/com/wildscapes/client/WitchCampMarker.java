package com.wildscapes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.wildscapes.Wildscapes;
import com.wildscapes.item.WitchCampMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public final class WitchCampMarker {
    private static final Map<MapItemSavedData, Integer> markers = new WeakHashMap<>();
    private static final RenderType TEXTURE = RenderType.text(ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID,
            "textures/map/decorations/witch_camp.png"));

    private WitchCampMarker() {}

    public static void remember(ItemStack stack, MapItemSavedData data) {
        if (data == null || !stack.hasTag()) {
            return;
        }
        var tags = stack.getTag().getList("Decorations", Tag.TAG_COMPOUND);
        for (int i = 0; i < tags.size(); i++) {
            var tag = tags.getCompound(i);
            if (WitchCampMap.MARKER.equals(tag.getString("id")) && tag.contains("center_x", Tag.TAG_INT)
                    && tag.contains("center_z", Tag.TAG_INT)) {
                int size = 128 << data.scale;
                int centerX = Math.floorDiv(tag.getInt("center_x") + 64, size) * size + size / 2 - 64;
                int centerZ = Math.floorDiv(tag.getInt("center_z") + 64, size) * size + size / 2 - 64;
                float x = (float) (tag.getDouble("x") - centerX) / (1 << data.scale);
                float z = (float) (tag.getDouble("z") - centerZ) / (1 << data.scale);
                byte mapX = (byte) (int) (x * 2.0F + 0.5D);
                byte mapZ = (byte) (int) (z * 2.0F + 0.5D);
                if (Math.abs(x) <= 63.0F && Math.abs(z) <= 63.0F) {
                    markers.put(data, (mapX & 255) << 8 | mapZ & 255);
                }
            }
        }
    }

    public static boolean render(MapItemSavedData data, MapDecoration icon, int index, PoseStack pose,
            MultiBufferSource buffer, int light) {
        Integer pos = markers.get(data);
        if (pos == null || icon.getType() != MapDecoration.Type.RED_X
                || pos != ((icon.getX() & 255) << 8 | icon.getY() & 255)) {
            return false;
        }
        pose.pushPose();
        pose.translate(icon.getX() / 2.0F + 64.0F, icon.getY() / 2.0F + 64.0F, -0.02F);
        pose.mulPose(Axis.ZP.rotationDegrees(icon.getRot() * 360 / 16.0F));
        pose.scale(4.0F, 4.0F, 3.0F);
        pose.translate(-0.125F, 0.125F, 0.0F);
        var matrix = pose.last().pose();
        var vc = buffer.getBuffer(TEXTURE);
        float z = index * -0.001F;
        vc.vertex(matrix, -1, 1, z).color(255, 255, 255, 255).uv(0, 0).uv2(light).endVertex();
        vc.vertex(matrix, 1, 1, z).color(255, 255, 255, 255).uv(1, 0).uv2(light).endVertex();
        vc.vertex(matrix, 1, -1, z).color(255, 255, 255, 255).uv(1, 1).uv2(light).endVertex();
        vc.vertex(matrix, -1, -1, z).color(255, 255, 255, 255).uv(0, 1).uv2(light).endVertex();
        pose.popPose();
        return true;
    }
}
