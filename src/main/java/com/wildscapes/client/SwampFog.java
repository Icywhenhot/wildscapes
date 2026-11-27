package com.wildscapes.client;

import org.joml.Vector4f;

import com.wildscapes.WildscapesConfig;
import com.wildscapes.entity.SwampVariants;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.level.material.FogType;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

public final class SwampFog {
    private SwampFog() {}

    private static final float CLEAR_RANGE = 96.0F;
    private static final float RAIN_RANGE = 40.0F;

    private static final float NEAR_FRACTION = 0.25F;

    private static final int LOW = 6;
    private static final int HIGH = 48;

    private static final float EASE = 0.0125F;

    private static final float TINT_R = 0.42F;
    private static final float TINT_G = 0.47F;
    private static final float TINT_B = 0.44F;

    private static final float MAX_TINT = 0.5F;

    private static float strength;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(SwampFog::onClientTick);
    }

    private static void onClientTick(Minecraft minecraft) {
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
        if (!WildscapesConfig.SWAMP_FOG.get() || !level.getBiome(pos).is(SwampVariants.SWAMP_BIOMES)) {
            return 0.0F;
        }
        float above = pos.getY() - (level.getSeaLevel() + LOW);
        float t = Mth.clamp(above / (HIGH - LOW), 0.0F, 1.0F);
        float height = 1.0F - t * t * (3.0F - 2.0F * t);

        float weather = 0.45F + 0.55F * level.getRainLevel(1.0F);
        return height * weather * WildscapesConfig.SWAMP_FOG_STRENGTH.get().floatValue();
    }

    public static void apply(Camera camera, FogRenderer.FogMode mode, float partialTick) {
        ClientLevel level = Minecraft.getInstance().level;
        if (strength <= 0.001F || level == null || camera.getFluidInCamera() != FogType.NONE
                || mode == FogRenderer.FogMode.FOG_SKY) {
            return;
        }

        float rain = level.getRainLevel(partialTick);
        float range = Mth.lerp(rain, CLEAR_RANGE, RAIN_RANGE);
        float vanillaFar = RenderSystem.getShaderFogEnd();
        float vanillaNear = RenderSystem.getShaderFogStart();
        float thick = Math.min(vanillaFar, range);
        float far = 1.0F / Mth.lerp(strength, 1.0F / vanillaFar, 1.0F / thick);
        float near = Mth.lerp(strength, vanillaNear, Math.min(vanillaNear, thick * NEAR_FRACTION));
        RenderSystem.setShaderFogEnd(far);
        RenderSystem.setShaderFogStart(Math.min(near, far));
    }

    public static void tint(Vector4f color) {
        float blend = strength * MAX_TINT;
        color.set(Mth.lerp(blend, color.x, TINT_R),
                Mth.lerp(blend, color.y, TINT_G), Mth.lerp(blend, color.z, TINT_B), color.w);
    }
}
