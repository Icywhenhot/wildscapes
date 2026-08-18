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

/**
 * A shallow layer of mist over the marshes: thin and close to the water on a clear day, thick
 * enough to swallow the far bank once it starts raining.
 *
 * <p>It only touches the terrain fog, never the sky fog, so the horizon and the sun keep their
 * usual colours and only the middle distance goes soft. Strength is held in one value that eases
 * towards its target every client tick, which is what keeps the fog from snapping in and out as
 * you walk over a biome edge or climb out of the valley.
 */
@EventBusSubscriber(modid = Wildscapes.MODID, value = Dist.CLIENT)
public final class SwampFog {
    private SwampFog() {}

    /** How far you can see once the fog is at full strength, clear weather / heavy rain. */
    private static final float CLEAR_RANGE = 96.0F;
    private static final float RAIN_RANGE = 40.0F;

    /** The fog starts this fraction of the way out, so nearby blocks stay crisp. */
    private static final float NEAR_FRACTION = 0.25F;

    /** Blocks above sea level where the mist begins to thin, and where it is gone entirely. */
    private static final int LOW = 3;
    private static final int HIGH = 16;

    /** Per-tick step of the eased strength: a full fade takes about two seconds. */
    private static final float EASE = 0.025F;

    /** The mist's own colour, a washed-out marsh grey-green, blended in as it thickens. */
    private static final float TINT_R = 0.42F;
    private static final float TINT_G = 0.47F;
    private static final float TINT_B = 0.44F;

    /** How far the fog colour is allowed to pull towards {@link #TINT_R} and friends. */
    private static final float MAX_TINT = 0.5F;

    private static float strength;

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            // Left the world: forget where we were, so rejoining somewhere dry starts clear.
            strength = 0.0F;
            return;
        }
        if (minecraft.isPaused()) {
            return;
        }
        float target = targetStrength(minecraft.level, minecraft.player.blockPosition());
        strength += Mth.clamp(target - strength, -EASE, EASE);
    }

    /** Thickest down at the waterline in a swamp, fading out with height and away from one. */
    private static float targetStrength(ClientLevel level, BlockPos pos) {
        if (!level.getBiome(pos).is(SwampVariants.SWAMP_BIOMES)) {
            return 0.0F;
        }
        float above = pos.getY() - (level.getSeaLevel() + LOW);
        float height = 1.0F - Mth.clamp(above / (HIGH - LOW), 0.0F, 1.0F);
        // A drizzle deepens it; a downpour closes the marsh right in.
        float weather = 0.45F + 0.55F * level.getRainLevel(1.0F);
        return height * weather;
    }

    @SubscribeEvent
    static void onRenderFog(ViewportEvent.RenderFog event) {
        ClientLevel level = Minecraft.getInstance().level;
        // Underwater and lava fog are their own thing, and the sky keeps its normal depth.
        if (strength <= 0.001F || level == null || event.getType() != FogType.NONE
                || event.getMode() == FogRenderer.FogMode.FOG_SKY) {
            return;
        }
        float rain = level.getRainLevel((float) event.getPartialTick());
        float range = Mth.lerp(rain, CLEAR_RANGE, RAIN_RANGE);
        // Never push the view further than vanilla already has it — this only ever closes in.
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
