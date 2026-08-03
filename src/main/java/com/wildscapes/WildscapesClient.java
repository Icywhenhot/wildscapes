package com.wildscapes;

import com.wildscapes.block.CauldronSoups;
import com.wildscapes.block.entity.WildscapesBlockEntities;
import com.wildscapes.entity.WildscapesEntities;
import com.wildscapes.entity.client.AbominationRenderer;
import com.wildscapes.entity.client.CauldronRenderer;
import com.wildscapes.entity.client.RedesignedSlimeModels;
import com.wildscapes.entity.client.RedesignedSlimeRenderer;
import com.wildscapes.entity.client.RedesignedWitchModel;
import com.wildscapes.entity.client.RedesignedWitchRenderer;
import com.wildscapes.entity.client.SwampIllagerModels;
import com.wildscapes.entity.client.SwampPillagerRenderer;
import com.wildscapes.entity.client.SwampVindicatorRenderer;
import com.wildscapes.entity.client.WildscapesModelLayers;
import com.wildscapes.item.SoupContents;
import com.wildscapes.item.WildscapesDataComponents;
import com.wildscapes.item.WildscapesItems;

import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

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

        event.registerBlockEntityRenderer(WildscapesBlockEntities.CAULDRON.get(), CauldronRenderer::new);
    }

    @SubscribeEvent
    static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        // Tints the broth layer (tintindex 1) of a magic soup by its blended effect colour.
        event.register((stack, tintIndex) -> {
            if (tintIndex != 1) {
                return 0xFFFFFFFF;
            }
            SoupContents soup = stack.get(WildscapesDataComponents.SOUP_CONTENTS.get());
            int rgb = soup == null ? 0x8B5A2B : CauldronSoups.colorOf(soup.effects());
            return 0xFF000000 | rgb;
        }, WildscapesItems.MAGIC_SOUP.get());
    }
}
