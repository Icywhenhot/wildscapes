package com.wildscapes.effect.client;

import com.wildscapes.effect.Intangibility;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.DeltaTracker;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import com.wildscapes.Wildscapes;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public final class PhaseVision {
    private static final int MAX_DARKEN = 0xD8;

    private static float veil;
    private static float lastVeil;

    private PhaseVision() {}

    public static void register(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> clientTick());
        modBus.addListener(RegisterGuiLayersEvent.class,
                event -> event.registerBelow(VanillaGuiLayers.CROSSHAIR, Wildscapes.id("phase_vision"), PhaseVision::renderVeil));
    }

    private static void clientTick() {
        lastVeil = veil;
        veil = Mth.clamp(veil + (insideBlock() ? 0.25F : -0.3F), 0.0F, 1.0F);
    }

    private static void renderVeil(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        float partial = delta.getGameTimeDeltaPartialTick(false);
        float strength = Mth.lerp(partial, lastVeil, veil);
        if (strength <= 0.02F) {
            return;
        }

        graphics.blurBeforeThisStratum();
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
        BlockPos pos = BlockPos.containing(minecraft.gameRenderer.mainCamera().position());
        return !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }
}
