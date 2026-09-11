package com.wildscapes;

import com.wildscapes.block.entity.WildscapesBlockEntities;
import com.wildscapes.entity.WildscapesEntities;
import com.wildscapes.entity.client.AbominationRenderer;
import com.wildscapes.entity.client.CauldronRenderer;
import com.wildscapes.entity.client.MirageLayer;
import com.wildscapes.entity.client.MirelashHookRenderer;
import com.wildscapes.entity.client.MirelashModels;
import com.wildscapes.entity.client.SlimeBubbleLayer;
import com.wildscapes.entity.client.RedesignedIllusionerModel;
import com.wildscapes.entity.client.RedesignedIllusionerRenderer;
import com.wildscapes.entity.client.RedesignedSlimeModels;
import com.wildscapes.entity.client.RedesignedSlimeRenderer;
import com.wildscapes.entity.client.RedesignedWitchModel;
import com.wildscapes.entity.client.RedesignedWitchRenderer;
import com.wildscapes.entity.client.SwampIllagerModels;
import com.wildscapes.entity.client.SwampPillagerRenderer;
import com.wildscapes.entity.client.SwampVindicatorRenderer;
import com.wildscapes.entity.client.WildscapesModelLayers;
import com.wildscapes.item.MirelashItem;
import com.wildscapes.item.WildscapesItems;
import com.wildscapes.particle.WildscapesParticles;
import com.wildscapes.particle.client.BrewBubbleParticle;
import com.wildscapes.particle.client.IngredientSteamParticle;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = Wildscapes.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class WildscapesClient {
    private WildscapesClient() {}

    @SubscribeEvent
    static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(WildscapesItems.MIRELASH.get(),
                    ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "active"),
                    (stack, level, holder, seed) -> level != null && holder != null
                            && MirelashItem.isHookOut(level, holder) ? 1F : 0F);
            ItemProperties.register(Items.ENCHANTED_BOOK,
                    ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "elasticity"),
                    (stack, level, holder, seed) -> hasStoredEnchantment(stack, MirelashItem.ELASTICITY) ? 1F : 0F);
            ItemProperties.register(Items.ENCHANTED_BOOK,
                    ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "slime_snare"),
                    (stack, level, holder, seed) -> hasStoredEnchantment(stack, MirelashItem.SLIME_SNARE) ? 1F : 0F);
        });
    }

    private static boolean hasStoredEnchantment(ItemStack stack, ResourceKey<Enchantment> key) {
        var enchantments = stack.get(DataComponents.STORED_ENCHANTMENTS);
        return enchantments != null && enchantments.keySet().stream().anyMatch(enchantment -> enchantment.is(key));
    }

    @SubscribeEvent
    static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(WildscapesModelLayers.PILLAGER_SWAMP, SwampIllagerModels::createPillagerLayer);
        event.registerLayerDefinition(WildscapesModelLayers.VINDICATOR_SWAMP, SwampIllagerModels::createVindicatorLayer);
        event.registerLayerDefinition(WildscapesModelLayers.ILLUSIONER, RedesignedIllusionerModel::createBodyLayer);
        event.registerLayerDefinition(WildscapesModelLayers.WITCH, RedesignedWitchModel::createBodyLayer);
        event.registerLayerDefinition(WildscapesModelLayers.SLIME_SMALL, RedesignedSlimeModels::createSmallInnerLayer);
        event.registerLayerDefinition(WildscapesModelLayers.SLIME_SMALL_OUTER, RedesignedSlimeModels::createSmallOuterLayer);
        event.registerLayerDefinition(WildscapesModelLayers.SLIME_MEDIUM, RedesignedSlimeModels::createMediumInnerLayer);
        event.registerLayerDefinition(WildscapesModelLayers.SLIME_MEDIUM_OUTER, RedesignedSlimeModels::createMediumOuterLayer);
        event.registerLayerDefinition(WildscapesModelLayers.SLIME_LARGE, RedesignedSlimeModels::createLargeInnerLayer);
        event.registerLayerDefinition(WildscapesModelLayers.SLIME_LARGE_OUTER, RedesignedSlimeModels::createLargeOuterLayer);
        event.registerLayerDefinition(WildscapesModelLayers.MIRELASH_HOOK, MirelashModels::createHookLayer);
        event.registerLayerDefinition(WildscapesModelLayers.MIRELASH_SEGMENT, MirelashModels::createSegmentLayer);
        event.registerLayerDefinition(WildscapesModelLayers.SLIME_BUBBLE, MirelashModels::createSlimeBubbleLayer);
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(WildscapesEntities.ABOMINATION.get(), AbominationRenderer::new);
        event.registerEntityRenderer(WildscapesEntities.MIRELASH_HOOK.get(), MirelashHookRenderer::new);

        event.registerEntityRenderer(EntityType.PILLAGER, SwampPillagerRenderer::new);
        event.registerEntityRenderer(EntityType.VINDICATOR, SwampVindicatorRenderer::new);
        event.registerEntityRenderer(EntityType.ILLUSIONER, RedesignedIllusionerRenderer::new);
        event.registerEntityRenderer(EntityType.WITCH, RedesignedWitchRenderer::new);
        event.registerEntityRenderer(EntityType.SLIME, RedesignedSlimeRenderer::new);

        event.registerBlockEntityRenderer(WildscapesBlockEntities.CAULDRON.get(), CauldronRenderer::new);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @SubscribeEvent
    static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (EntityType<?> type : event.getEntityTypes()) {
            EntityRenderer<?> renderer = event.getRenderer(type);
            if (renderer instanceof LivingEntityRenderer living) {
                living.addLayer(new MirageLayer(living));
                living.addLayer(new SlimeBubbleLayer(living, event.getContext().getModelSet()));
            }
        }
    }

    @SubscribeEvent
    static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(WildscapesParticles.BREW_BUBBLE.get(), BrewBubbleParticle.Provider::new);
        event.registerSpriteSet(WildscapesParticles.INGREDIENT_STEAM.get(), IngredientSteamParticle.Provider::new);
    }
}
