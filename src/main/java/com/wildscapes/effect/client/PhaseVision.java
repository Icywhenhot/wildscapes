package com.wildscapes.effect.client;

import com.wildscapes.Wildscapes;
import com.wildscapes.effect.Intangibility;
import com.wildscapes.mixin.client.GameRendererAccessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = Wildscapes.MODID, value = Dist.CLIENT)
public final class PhaseVision {
    private static final float MAX_BLUR = 11.0F;
    private static final int MAX_DARKEN = 0xD8;

    private static float veil;
    private static float lastVeil;

    private PhaseVision() {}

    @SubscribeEvent
    static void clientTick(ClientTickEvent.Post event) {
        lastVeil = veil;
        veil = Mth.clamp(veil + (insideBlock() ? 0.25F : -0.3F), 0.0F, 1.0F);
    }

    @SubscribeEvent
    static void renderVeil(RenderGuiEvent.Pre event) {
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float strength = Mth.lerp(partial, lastVeil, veil);
        if (strength <= 0.02F) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        PostChain blur = ((GameRendererAccessor) minecraft.gameRenderer).wildscapes$blurEffect();
        if (blur != null) {
            blur.setUniform("Radius", 1.0F + MAX_BLUR * strength);
            blur.process(partial);
            minecraft.getMainRenderTarget().bindWrite(false);
        }

        GuiGraphics graphics = event.getGuiGraphics();
        int alpha = (int) (MAX_DARKEN * strength);
        graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), alpha << 24);
    }

    private static boolean insideBlock() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || !Intangibility.isActive(player)) {
            return false;
        }
        Level level = player.level();
        BlockPos pos = BlockPos.containing(minecraft.gameRenderer.getMainCamera().getPosition());
        return !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }
}
