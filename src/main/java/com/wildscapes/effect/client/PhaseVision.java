package com.wildscapes.effect.client;

import com.wildscapes.Wildscapes;
import com.wildscapes.effect.Intangibility;
import com.wildscapes.mixin.client.PostChainAccessor;
import java.io.IOException;
import java.io.UncheckedIOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = Wildscapes.MODID, value = Dist.CLIENT)
public final class PhaseVision {
    private static final float MAX_BLUR = 11.0F;
    private static final int MAX_DARKEN = 0xD8;

    private static float veil;
    private static float lastVeil;
    private static PostChain blur;
    private static int width;
    private static int height;

    private PhaseVision() {}

    public static void reload() {
        if (blur != null) {
            blur.close();
            blur = null;
        }
        veil = 0.0F;
        lastVeil = 0.0F;
    }

    @SubscribeEvent
    static void clientTick(ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        lastVeil = veil;
        veil = Mth.clamp(veil + (insideBlock() ? 0.25F : -0.3F), 0.0F, 1.0F);
    }

    @SubscribeEvent
    static void renderVeil(RenderGuiEvent.Pre event) {
        float partial = event.getPartialTick();
        float strength = Mth.lerp(partial, lastVeil, veil);
        if (strength <= 0.02F) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (blur == null) {
            try {
                blur = new PostChain(minecraft.getTextureManager(), minecraft.getResourceManager(),
                        minecraft.getMainRenderTarget(), ResourceLocation.withDefaultNamespace("shaders/post/blur.json"));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            width = 0;
        }
        int w = minecraft.getWindow().getWidth();
        int h = minecraft.getWindow().getHeight();
        if (width != w || height != h) {
            blur.resize(w, h);
            width = w;
            height = h;
        }
        for (var pass : ((PostChainAccessor) blur).wildscapes$passes()) {
            pass.getEffect().safeGetUniform("Radius").set(1.0F + MAX_BLUR * strength);
        }
        blur.process(partial);
        minecraft.getMainRenderTarget().bindWrite(false);

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
