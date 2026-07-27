package com.wildscapes;

import com.wildscapes.entity.WildscapesEntities;
import com.wildscapes.entity.client.AbominationRenderer;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = Wildscapes.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class WildscapesClient {
    private WildscapesClient() {}

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(WildscapesEntities.ABOMINATION.get(), AbominationRenderer::new);
    }
}
