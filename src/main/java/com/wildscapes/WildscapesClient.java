package com.wildscapes;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.LivingEntity;
import com.wildscapes.entity.client.CypressShelfRenderer;
import com.wildscapes.client.ClientPackets;
import com.wildscapes.client.SwampFog;
import com.wildscapes.client.SwampMossModel;
import com.wildscapes.item.WildscapesEnchantments;
import com.wildscapes.block.WildscapesBlocks;
import com.wildscapes.block.entity.WildscapesBlockEntities;
import com.wildscapes.entity.WildscapesEntities;
import com.wildscapes.entity.client.AbominationModels;
import com.wildscapes.entity.client.CypressBoatRenderer;
import com.wildscapes.effect.client.PhaseVision;
import com.wildscapes.entity.client.AbominationRenderer;
import com.wildscapes.entity.client.CauldronOfSoulsRenderer;
import com.wildscapes.entity.client.CauldronRenderer;
import com.wildscapes.entity.client.EmpoweredEvokerRenderer;
import com.wildscapes.entity.client.MirageLayer;
import com.wildscapes.entity.client.MirelashHookRenderer;
import com.wildscapes.entity.client.MirelashModels;
import com.wildscapes.entity.client.SlimeBubbleLayer;
import com.wildscapes.entity.client.RedesignedBoggedModel;
import com.wildscapes.entity.client.RedesignedBoggedRenderer;
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
import com.wildscapes.item.WitchCampMap;
import com.wildscapes.particle.WildscapesParticles;
import com.wildscapes.particle.client.BrewBubbleParticle;
import com.wildscapes.particle.client.ResidueParticle;
import com.wildscapes.particle.client.IngredientSteamParticle;
import com.wildscapes.particle.client.SoulTrailParticle;
import com.wildscapes.particle.client.MultiJumpParticle;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

public final class WildscapesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        registerItemProperties();
        registerRenderLayers();
        registerLayerDefinitions();
        registerRenderers();
        registerParticleProviders();
        ColorProviderRegistry.ITEM.register((stack, layer) -> 0xFFFFFFFF, WildscapesItems.ILLUSIONER_SPAWN_EGG.get(),
                Items.WITCH_SPAWN_EGG);
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register(WildscapesClient::addLayers);
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new PhaseVisionReset());
        ClientPackets.register();
        PhaseVision.register();
        SwampFog.register();
        SwampMossModel.register();
    }

    private static void registerItemProperties() {
        ItemProperties.register(Items.FILLED_MAP, Wildscapes.id("witch_camp"),
                (stack, level, holder, seed) -> WitchCampMap.isWitchCampMap(stack) ? 1F : 0F);
        ItemProperties.register(WildscapesItems.MIRELASH.get(), Wildscapes.id("active"),
                (stack, level, holder, seed) -> level != null && holder != null
                        && MirelashItem.isHookOut(level, holder) ? 1F : 0F);
        ItemProperties.register(WildscapesItems.ILLUSIONIST_NECKLACE.get(), Wildscapes.id("active"),
                (stack, level, holder, seed) -> holder instanceof Player player
                        && (player.getMainHandItem() == stack || player.getOffhandItem() == stack)
                        && Intangibility.isActive(player) ? 1F : 0F);
        ItemProperties.register(Items.ENCHANTED_BOOK, Wildscapes.id("elasticity"),
                (stack, level, holder, seed) -> hasStoredEnchantment(stack, WildscapesEnchantments.ELASTICITY.get()) ? 1F : 0F);
        ItemProperties.register(Items.ENCHANTED_BOOK, Wildscapes.id("slime_snare"),
                (stack, level, holder, seed) -> hasStoredEnchantment(stack, WildscapesEnchantments.SLIME_SNARE.get()) ? 1F : 0F);
        ItemProperties.register(Items.ENCHANTED_BOOK, Wildscapes.id("multi_jump"),
                (stack, level, holder, seed) -> hasStoredEnchantment(stack, WildscapesEnchantments.MULTI_JUMP.get()) ? 1F : 0F);
        ItemProperties.register(Items.ENCHANTED_BOOK, Wildscapes.id("empowered_jump"),
                (stack, level, holder, seed) -> hasStoredEnchantment(stack, WildscapesEnchantments.EMPOWERED_JUMP.get()) ? 1F : 0F);
    }

    private static boolean hasStoredEnchantment(ItemStack stack, Enchantment enchantment) {
        return EnchantmentHelper.getEnchantments(stack).containsKey(enchantment);
    }

    private static void registerRenderLayers() {
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(),
                WildscapesBlocks.CYPRESS_SAPLING.get(), WildscapesBlocks.CYPRESS_DOOR.get(),
                WildscapesBlocks.CYPRESS_TRAPDOOR.get(), WildscapesBlocks.DUCKWEED.get(), WildscapesBlocks.RUSHES.get(),
                WildscapesBlocks.SHORT_RUSHES.get(), WildscapesBlocks.LUMINOUS_MUSHROOM.get(),
                WildscapesBlocks.SHELF_LUMINOUS_MUSHROOM.get(), WildscapesBlocks.MOSS_CARPET.get(),
                WildscapesBlocks.HANGING_MOSS.get(), WildscapesBlocks.BONFIRE.get(), WildscapesBlocks.SOUL_BONFIRE.get(),
                WildscapesBlocks.SUMMONING_BONFIRE.get(), WildscapesBlocks.INCENDIARY_FIRE.get(),
                WildscapesBlocks.CAULDRON.get(), WildscapesBlocks.CAULDRON_OF_SOULS.get());
    }

    private static void registerLayerDefinitions() {
        EntityModelLayerRegistry.registerModelLayer(WildscapesModelLayers.PILLAGER_SWAMP, SwampIllagerModels::createPillagerLayer);
        EntityModelLayerRegistry.registerModelLayer(WildscapesModelLayers.VINDICATOR, SwampIllagerModels::createVindicatorLayer);
        EntityModelLayerRegistry.registerModelLayer(WildscapesModelLayers.ILLUSIONER, RedesignedIllusionerModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(WildscapesModelLayers.WITCH, RedesignedWitchModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(WildscapesModelLayers.BOGGED, RedesignedBoggedModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(WildscapesModelLayers.SLIME_SMALL, RedesignedSlimeModels::createSmallInnerLayer);
        EntityModelLayerRegistry.registerModelLayer(WildscapesModelLayers.SLIME_SMALL_OUTER, RedesignedSlimeModels::createSmallOuterLayer);
        EntityModelLayerRegistry.registerModelLayer(WildscapesModelLayers.SLIME_MEDIUM, RedesignedSlimeModels::createMediumInnerLayer);
        EntityModelLayerRegistry.registerModelLayer(WildscapesModelLayers.SLIME_MEDIUM_OUTER, RedesignedSlimeModels::createMediumOuterLayer);
        EntityModelLayerRegistry.registerModelLayer(WildscapesModelLayers.SLIME_LARGE, RedesignedSlimeModels::createLargeInnerLayer);
        EntityModelLayerRegistry.registerModelLayer(WildscapesModelLayers.SLIME_LARGE_OUTER, RedesignedSlimeModels::createLargeOuterLayer);
        EntityModelLayerRegistry.registerModelLayer(WildscapesModelLayers.MIRELASH_HOOK, MirelashModels::createHookLayer);
        EntityModelLayerRegistry.registerModelLayer(WildscapesModelLayers.MIRELASH_SEGMENT, MirelashModels::createSegmentLayer);
        EntityModelLayerRegistry.registerModelLayer(WildscapesModelLayers.SLIME_BUBBLE, MirelashModels::createSlimeBubbleLayer);
        EntityModelLayerRegistry.registerModelLayer(WildscapesModelLayers.TONGUE_SEGMENT, AbominationModels::createTongueSegment);
        EntityModelLayerRegistry.registerModelLayer(WildscapesModelLayers.TONGUE_TIP, AbominationModels::createTongueTip);
        EntityModelLayerRegistry.registerModelLayer(WildscapesModelLayers.ABOMINATION_GRASP, AbominationModels::createGrasp);
    }

    private static void registerRenderers() {
        EntityRendererRegistry.register(WildscapesEntities.CYPRESS_BOAT.get(), ctx -> new CypressBoatRenderer(ctx, false));
        EntityRendererRegistry.register(WildscapesEntities.CYPRESS_CHEST_BOAT.get(), ctx -> new CypressBoatRenderer(ctx, true));
        EntityRendererRegistry.register(WildscapesEntities.ABOMINATION.get(), AbominationRenderer::new);
        EntityRendererRegistry.register(WildscapesEntities.MIRELASH_HOOK.get(), MirelashHookRenderer::new);
        EntityRendererRegistry.register(WildscapesEntities.RESIDUE_CLOUD.get(), NoopRenderer::new);
        EntityRendererRegistry.register(WildscapesEntities.INCENDIARY_BOTTLE.get(), ThrownItemRenderer::new);

        EntityRendererRegistry.register(EntityType.PILLAGER, SwampPillagerRenderer::new);
        EntityRendererRegistry.register(EntityType.VINDICATOR, SwampVindicatorRenderer::new);
        EntityRendererRegistry.register(EntityType.ILLUSIONER, RedesignedIllusionerRenderer::new);
        EntityRendererRegistry.register(EntityType.WITCH, RedesignedWitchRenderer::new);
        EntityRendererRegistry.register(WildscapesEntities.BOGGED.get(), RedesignedBoggedRenderer::new);
        EntityRendererRegistry.register(EntityType.EVOKER, EmpoweredEvokerRenderer::new);
        EntityRendererRegistry.register(EntityType.SLIME, RedesignedSlimeRenderer::new);

        BlockEntityRenderers.register(WildscapesBlockEntities.CAULDRON.get(), CauldronRenderer::new);
        BlockEntityRenderers.register(WildscapesBlockEntities.CAULDRON_OF_SOULS.get(), CauldronOfSoulsRenderer::new);
        BlockEntityRenderers.register(WildscapesBlockEntities.CYPRESS_SHELF.get(), CypressShelfRenderer::new);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void addLayers(EntityType<? extends LivingEntity> type, LivingEntityRenderer<?, ?> renderer,
            LivingEntityFeatureRendererRegistrationCallback.RegistrationHelper helper, EntityRendererProvider.Context context) {
        LivingEntityRenderer living = renderer;
        helper.register(new MirageLayer(living));
        helper.register(new SlimeBubbleLayer(living, context.getModelSet()));
    }

    private static void registerParticleProviders() {
        var particles = ParticleFactoryRegistry.getInstance();
        particles.register(WildscapesParticles.BREW_BUBBLE.get(), BrewBubbleParticle.Provider::new);
        particles.register(WildscapesParticles.INGREDIENT_STEAM.get(), IngredientSteamParticle.Provider::new);
        particles.register(WildscapesParticles.RESIDUE_SPLAT.get(),
                sprites -> new ResidueParticle.Provider(sprites, 11, 2.4F, 0.82F, 0.02F));
        particles.register(WildscapesParticles.RESIDUE_WISP.get(),
                sprites -> new ResidueParticle.Provider(sprites, 18, 0.9F, 0.95F, 0.01F));
        particles.register(WildscapesParticles.RESIDUE_SWIRL.get(),
                sprites -> new ResidueParticle.Provider(sprites, 14, 1.3F, 0.93F, 0.02F));
        particles.register(WildscapesParticles.SOUL_TRAIL.get(), SoulTrailParticle.Provider::new);
        particles.register(WildscapesParticles.MULTI_JUMP.get(), MultiJumpParticle.Provider::new);
        particles.register(WildscapesParticles.SOUL_HARVEST_SPARK.get(),
                sprites -> new ResidueParticle.Provider(sprites, 16, 0.5F, 0.92F, 0.01F));
        particles.register(WildscapesParticles.EMPOWERED_WISP.get(),
                sprites -> new ResidueParticle.Provider(sprites, 14, 1.1F, 0.94F, 0.0F));
        particles.register(WildscapesParticles.EMPOWERED_RING.get(),
                sprites -> new ResidueParticle.Provider(sprites, 14, 1.2F, 0.94F, 0.0F));
        particles.register(WildscapesParticles.EMPOWERED_SOUL.get(),
                sprites -> new ResidueParticle.Provider(sprites, 16, 0.9F, 0.94F, 0.01F));
        particles.register(WildscapesParticles.SOUL_MOTE.get(),
                sprites -> new ResidueParticle.Provider(sprites, 20, 0.35F, 0.9F, 0.01F));
        particles.register(WildscapesParticles.LOST_SOUL.get(),
                sprites -> new ResidueParticle.Provider(sprites, 14, 1.4F, 0.9F, 0.02F));
        particles.register(WildscapesParticles.BONFIRE_EMBER.get(),
                sprites -> new ResidueParticle.Provider(sprites, 18, 0.3F, 0.96F, 0.0F));
        particles.register(WildscapesParticles.CAULDRON_SWIRL.get(),
                sprites -> new ResidueParticle.Provider(sprites, 16, 1.2F, 0.95F, 0.01F));
        particles.register(WildscapesParticles.CAULDRON_REWARD.get(),
                sprites -> new ResidueParticle.Provider(sprites, 14, 1.5F, 0.9F, 0.02F));
        particles.register(WildscapesParticles.SUMMON_WRAITH.get(),
                sprites -> new ResidueParticle.Provider(sprites, 16, 1.8F, 0.92F, 0.02F));
        particles.register(WildscapesParticles.SUMMON_RISE.get(),
                sprites -> new ResidueParticle.Provider(sprites, 16, 1.6F, 0.9F, 0.03F));
        particles.register(WildscapesParticles.SUMMON_MOTE.get(),
                sprites -> new ResidueParticle.Provider(sprites, 18, 0.35F, 0.92F, 0.02F));
    }

    private static final class PhaseVisionReset implements IdentifiableResourceReloadListener {
        @Override
        public ResourceLocation getFabricId() {
            return Wildscapes.id("phase_vision");
        }

        @Override
        public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier barrier, ResourceManager manager,
                ProfilerFiller preparations, ProfilerFiller reload, Executor background, Executor game) {
            return barrier.wait(null).thenRunAsync(PhaseVision::reload, game);
        }
    }
}
