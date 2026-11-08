package com.wildscapes.entity.client;

import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;

public final class MobVisuals {
    public static final RenderStateDataKey<Boolean> EMPOWERED = RenderStateDataKey.create();
    public static final RenderStateDataKey<Boolean> SWAMP = RenderStateDataKey.create();
    public static final RenderStateDataKey<Boolean> MIRAGE = RenderStateDataKey.create();
    public static final RenderStateDataKey<Boolean> BUBBLE = RenderStateDataKey.create();
    public static final RenderStateDataKey<Boolean> PHASED = RenderStateDataKey.create();
    public static final RenderStateDataKey<Boolean> LOCAL = RenderStateDataKey.create();
    public static final RenderStateDataKey<Boolean> CHANTING = RenderStateDataKey.create();
    public static final RenderStateDataKey<Float> MERGE_ALPHA = RenderStateDataKey.create();

    private MobVisuals() {}
}
