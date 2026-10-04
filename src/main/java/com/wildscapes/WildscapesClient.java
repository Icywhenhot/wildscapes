package com.wildscapes;

import com.wildscapes.block.entity.WildscapesBlockEntities;
import com.wildscapes.entity.WildscapesEntities;
import com.wildscapes.entity.client.AbominationModels;
import com.wildscapes.entity.client.AbominationRenderer;
import com.wildscapes.entity.client.CauldronRenderer;
import com.wildscapes.entity.client.EmpoweredEvokerRenderer;
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
import com.wildscapes.effect.Intangibility;
import com.wildscapes.item.MirelashItem;
import com.wildscapes.item.WildscapesItems;
import com.wildscapes.particle.WildscapesParticles;
import com.wildscapes.particle.client.BrewBubbleParticle;
import com.wildscapes.particle.client.ResidueParticle;
import com.wildscapes.particle.client.IngredientSteamParticle;
import com.wildscapes.particle.client.SoulTrailParticle;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
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
            ItemProperties.register(WildscapesItems.ILLUSIONIST_NECKLACE.get(),
                    ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "active"),
                    (stack, level, holder, seed) -> holder instanceof Player player
                            && (player.getMainHandItem() == stack || player.getOffhandItem() == stack)
                            && Intangibility.isActive(player) ? 1F : 0F);
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
    static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, layer) -> 0xFFFFFFFF, WildscapesItems.ILLUSIONER_SPAWN_EGG.get(),
                Items.WITCH_SPAWN_EGG);
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
        event.registerLayerDefinition(WildscapesModelLayers.TONGUE_SEGMENT, AbominationModels::createTongueSegment);
        event.registerLayerDefinition(WildscapesModelLayers.TONGUE_TIP, AbominationModels::createTongueTip);
        event.registerLayerDefinition(WildscapesModelLayers.ABOMINATION_GRASP, AbominationModels::createGrasp);
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(WildscapesEntities.ABOMINATION.get(), AbominationRenderer::new);
        event.registerEntityRenderer(WildscapesEntities.MIRELASH_HOOK.get(), MirelashHookRenderer::new);
        event.registerEntityRenderer(WildscapesEntities.RESIDUE_CLOUD.get(), NoopRenderer::new);
        event.registerEntityRenderer(WildscapesEntities.INCENDIARY_BOTTLE.get(), ThrownItemRenderer::new);

        event.registerEntityRenderer(EntityType.PILLAGER, SwampPillagerRenderer::new);
        event.registerEntityRenderer(EntityType.VINDICATOR, SwampVindicatorRenderer::new);
        event.registerEntityRenderer(EntityType.ILLUSIONER, RedesignedIllusionerRenderer::new);
        event.registerEntityRenderer(EntityType.WITCH, RedesignedWitchRenderer::new);
        event.registerEntityRenderer(EntityType.EVOKER, EmpoweredEvokerRenderer::new);
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
        event.registerSpriteSet(WildscapesParticles.RESIDUE_SPLAT.get(),
                sprites -> new ResidueParticle.Provider(sprites, 11, 2.4F, 0.82F, 0.02F));
        event.registerSpriteSet(WildscapesParticles.RESIDUE_WISP.get(),
                sprites -> new ResidueParticle.Provider(sprites, 18, 0.9F, 0.95F, 0.01F));
        event.registerSpriteSet(WildscapesParticles.RESIDUE_SWIRL.get(),
                sprites -> new ResidueParticle.Provider(sprites, 14, 1.3F, 0.93F, 0.02F));
        event.registerSpriteSet(WildscapesParticles.SOUL_TRAIL.get(), SoulTrailParticle.Provider::new);
        event.registerSpriteSet(WildscapesParticles.INCURSION_SPARK.get(),
                sprites -> new ResidueParticle.Provider(sprites, 16, 0.5F, 0.92F, 0.01F));
        event.registerSpriteSet(WildscapesParticles.EMPOWERED_WISP.get(),
                sprites -> new ResidueParticle.Provider(sprites, 14, 1.1F, 0.94F, 0.0F));
        event.registerSpriteSet(WildscapesParticles.EMPOWERED_RING.get(),
                sprites -> new ResidueParticle.Provider(sprites, 14, 1.2F, 0.94F, 0.0F));
        event.registerSpriteSet(WildscapesParticles.EMPOWERED_SOUL.get(),
                sprites -> new ResidueParticle.Provider(sprites, 16, 0.9F, 0.94F, 0.01F));
        event.registerSpriteSet(WildscapesParticles.SOUL_MOTE.get(),
                sprites -> new ResidueParticle.Provider(sprites, 20, 0.35F, 0.9F, 0.01F));
        event.registerSpriteSet(WildscapesParticles.LOST_SOUL.get(),
                sprites -> new ResidueParticle.Provider(sprites, 14, 1.4F, 0.9F, 0.02F));
        event.registerSpriteSet(WildscapesParticles.BONFIRE_EMBER.get(),
                sprites -> new ResidueParticle.Provider(sprites, 18, 0.3F, 0.96F, 0.0F));
        event.registerSpriteSet(WildscapesParticles.CAULDRON_SWIRL.get(),
                sprites -> new ResidueParticle.Provider(sprites, 16, 1.2F, 0.95F, 0.01F));
        event.registerSpriteSet(WildscapesParticles.CAULDRON_REWARD.get(),
                sprites -> new ResidueParticle.Provider(sprites, 14, 1.5F, 0.9F, 0.02F));
        event.registerSpriteSet(WildscapesParticles.SUMMON_WRAITH.get(),
                sprites -> new ResidueParticle.Provider(sprites, 16, 1.8F, 0.92F, 0.02F));
        event.registerSpriteSet(WildscapesParticles.SUMMON_RISE.get(),
                sprites -> new ResidueParticle.Provider(sprites, 16, 1.6F, 0.9F, 0.03F));
        event.registerSpriteSet(WildscapesParticles.SUMMON_MOTE.get(),
                sprites -> new ResidueParticle.Provider(sprites, 18, 0.35F, 0.92F, 0.02F));
    }
}
