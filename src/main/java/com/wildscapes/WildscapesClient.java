package com.wildscapes;

import com.wildscapes.entity.WildscapesEntities;
import com.wildscapes.entity.client.AbominationRenderer;
import com.wildscapes.entity.client.RedesignedSlimeModels;
import com.wildscapes.entity.client.RedesignedSlimeRenderer;
import com.wildscapes.entity.client.RedesignedWitchModel;
import com.wildscapes.entity.client.RedesignedWitchRenderer;
import com.wildscapes.entity.client.SwampIllagerModels;
import com.wildscapes.entity.client.SwampPillagerRenderer;
import com.wildscapes.entity.client.SwampVindicatorRenderer;
import com.wildscapes.entity.client.WildscapesModelLayers;

import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = Wildscapes.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class WildscapesClient {
    private WildscapesClient() {}

    @SubscribeEvent
    static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(WildscapesModelLayers.PILLAGER_SWAMP, SwampIllagerModels::createPillagerLayer);
        event.registerLayerDefinition(WildscapesModelLayers.VINDICATOR_SWAMP, SwampIllagerModels::createVindicatorLayer);
        event.registerLayerDefinition(WildscapesModelLayers.WITCH, RedesignedWitchModel::createBodyLayer);
        event.registerLayerDefinition(WildscapesModelLayers.SLIME_SMALL, RedesignedSlimeModels::createSmallInnerLayer);
        event.registerLayerDefinition(WildscapesModelLayers.SLIME_SMALL_OUTER, RedesignedSlimeModels::createSmallOuterLayer);
        event.registerLayerDefinition(WildscapesModelLayers.SLIME_MEDIUM, RedesignedSlimeModels::createMediumInnerLayer);
        event.registerLayerDefinition(WildscapesModelLayers.SLIME_MEDIUM_OUTER, RedesignedSlimeModels::createMediumOuterLayer);
        event.registerLayerDefinition(WildscapesModelLayers.SLIME_LARGE, RedesignedSlimeModels::createLargeInnerLayer);
        event.registerLayerDefinition(WildscapesModelLayers.SLIME_LARGE_OUTER, RedesignedSlimeModels::createLargeOuterLayer);
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(WildscapesEntities.ABOMINATION.get(), AbominationRenderer::new);

        // Re-registering a vanilla entity type replaces its renderer. Pillagers and
        // vindicators still fall back to vanilla unless they were born in a swamp;
        // witches and slimes are redesigned everywhere.
        event.registerEntityRenderer(EntityType.PILLAGER, SwampPillagerRenderer::new);
        event.registerEntityRenderer(EntityType.VINDICATOR, SwampVindicatorRenderer::new);
        event.registerEntityRenderer(EntityType.WITCH, RedesignedWitchRenderer::new);
        event.registerEntityRenderer(EntityType.SLIME, RedesignedSlimeRenderer::new);
    }
}
