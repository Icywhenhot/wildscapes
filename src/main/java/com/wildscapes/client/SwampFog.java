package com.wildscapes.client;

import com.wildscapes.entity.SwampVariants;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

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

    public static void onClientTick(Minecraft minecraft) {
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

    public static void apply(FogData fog, ClientLevel level, float partialTick) {
        if (strength <= 0.001F) {
            return;
        }

        float rain = level.getRainLevel(partialTick);
        float range = Mth.lerp(rain, CLEAR_RANGE, RAIN_RANGE);
        float far = Mth.lerp(strength, fog.environmentalEnd, Math.min(fog.environmentalEnd, range));
        fog.environmentalEnd = far;
        fog.environmentalStart = Math.min(fog.environmentalStart, far * NEAR_FRACTION);

        float blend = strength * MAX_TINT;
        fog.color.set(Mth.lerp(blend, fog.color.x, TINT_R),
                Mth.lerp(blend, fog.color.y, TINT_G), Mth.lerp(blend, fog.color.z, TINT_B), fog.color.w);
    }
}
