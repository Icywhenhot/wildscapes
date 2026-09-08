package com.wildscapes.client;

import com.wildscapes.Wildscapes;
import com.wildscapes.entity.SwampVariants;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FogType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = Wildscapes.MODID, value = Dist.CLIENT)
public final class SwampFog {
    private SwampFog() {}

    private static final float CLEAR_RANGE = 96.0F;
    private static final float RAIN_RANGE = 40.0F;

    private static final float NEAR_FRACTION = 0.25F;

    private static final int LOW = 3;
    private static final int HIGH = 16;

    private static final float EASE = 0.025F;

    private static final float TINT_R = 0.42F;
    private static final float TINT_G = 0.47F;
    private static final float TINT_B = 0.44F;

    private static final float MAX_TINT = 0.5F;

    private static float strength;

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            strength = 0.0F;
            return;
        }
        if (minecraft.isPaused()) {
            return;
        }
        float target = targetStrength(minecraft.level, minecraft.player.blockPosition());
        strength += Mth.clamp(target - strength, -EASE, EASE);
    }

    private static float targetStrength(ClientLevel level, BlockPos pos) {
        if (!level.getBiome(pos).is(SwampVariants.SWAMP_BIOMES)) {
            return 0.0F;
        }
        float above = pos.getY() - (level.getSeaLevel() + LOW);
        float height = 1.0F - Mth.clamp(above / (HIGH - LOW), 0.0F, 1.0F);

        float weather = 0.45F + 0.55F * level.getRainLevel(1.0F);
        return height * weather;
    }

    @SubscribeEvent
    static void onRenderFog(ViewportEvent.RenderFog event) {
        ClientLevel level = Minecraft.getInstance().level;

        if (strength <= 0.001F || level == null || event.getType() != FogType.NONE
                || event.getMode() == FogRenderer.FogMode.FOG_SKY) {
            return;
        }
        float rain = level.getRainLevel((float) event.getPartialTick());
        float range = Mth.lerp(rain, CLEAR_RANGE, RAIN_RANGE);

        float far = Mth.lerp(strength, event.getFarPlaneDistance(),
                Math.min(event.getFarPlaneDistance(), range));
        event.setFarPlaneDistance(far);
        event.setNearPlaneDistance(Math.min(event.getNearPlaneDistance(), far * NEAR_FRACTION));
        event.setCanceled(true);
    }

    @SubscribeEvent
    static void onFogColor(ViewportEvent.ComputeFogColor event) {
        if (strength <= 0.001F) {
            return;
        }
        float blend = strength * MAX_TINT;
        event.setRed(Mth.lerp(blend, event.getRed(), TINT_R));
        event.setGreen(Mth.lerp(blend, event.getGreen(), TINT_G));
        event.setBlue(Mth.lerp(blend, event.getBlue(), TINT_B));
    }
}
