package com.wildscapes;

import com.wildscapes.block.entity.WildscapesBlockEntities;
import com.wildscapes.client.SwampFog;
import com.wildscapes.client.SwampMossModel;
import com.wildscapes.effect.client.PhaseVision;
import com.wildscapes.entity.WildscapesEntities;
import com.wildscapes.item.CypressBoat;
import com.wildscapes.client.WildscapesItemCondition;
import com.wildscapes.entity.client.AbominationModels;
import com.wildscapes.entity.client.AbominationRenderer;
import com.wildscapes.entity.client.CauldronOfSoulsRenderer;
import com.wildscapes.entity.client.CauldronRenderer;
import com.wildscapes.entity.client.EmpoweredEvokerRenderer;
import com.wildscapes.entity.client.MirelashHookRenderer;
import com.wildscapes.entity.client.MirelashModels;
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
import com.wildscapes.particle.WildscapesParticles;
import com.wildscapes.particle.client.BrewBubbleParticle;
import com.wildscapes.particle.client.ResidueParticle;
import com.wildscapes.particle.client.IngredientSteamParticle;
import com.wildscapes.particle.client.SoulTrailParticle;
import com.wildscapes.particle.client.MultiJumpParticle;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.entity.EntityTypes;

public final class WildscapesClient implements ClientModInitializer {
    private static final ModelLayerLocation CYPRESS_BOAT = new ModelLayerLocation(Wildscapes.id("boat/cypress"), "main");
    private static final ModelLayerLocation CYPRESS_CHEST_BOAT = new ModelLayerLocation(Wildscapes.id("chest_boat/cypress"), "main");

    @Override
    public void onInitializeClient() {
        registerLayerDefinitions();
        registerRenderers();
        registerParticleProviders();
        WildscapesItemCondition.register();
        PhaseVision.register();
        SwampMossModel.register();
        ClientTickEvents.END_CLIENT_TICK.register(SwampFog::onClientTick);
    }

    private static void registerLayerDefinitions() {
        ModelLayerRegistry.registerModelLayer(WildscapesModelLayers.PILLAGER_SWAMP, SwampIllagerModels::createPillagerLayer);
        ModelLayerRegistry.registerModelLayer(WildscapesModelLayers.VINDICATOR, SwampIllagerModels::createVindicatorLayer);
        ModelLayerRegistry.registerModelLayer(WildscapesModelLayers.ILLUSIONER, RedesignedIllusionerModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(WildscapesModelLayers.WITCH, RedesignedWitchModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(WildscapesModelLayers.BOGGED, RedesignedBoggedModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(WildscapesModelLayers.SLIME_SMALL, RedesignedSlimeModels::createSmallInnerLayer);
        ModelLayerRegistry.registerModelLayer(WildscapesModelLayers.SLIME_SMALL_OUTER, RedesignedSlimeModels::createSmallOuterLayer);
        ModelLayerRegistry.registerModelLayer(WildscapesModelLayers.SLIME_MEDIUM, RedesignedSlimeModels::createMediumInnerLayer);
        ModelLayerRegistry.registerModelLayer(WildscapesModelLayers.SLIME_MEDIUM_OUTER, RedesignedSlimeModels::createMediumOuterLayer);
        ModelLayerRegistry.registerModelLayer(WildscapesModelLayers.SLIME_LARGE, RedesignedSlimeModels::createLargeInnerLayer);
        ModelLayerRegistry.registerModelLayer(WildscapesModelLayers.SLIME_LARGE_OUTER, RedesignedSlimeModels::createLargeOuterLayer);
        ModelLayerRegistry.registerModelLayer(WildscapesModelLayers.MIRELASH_HOOK, MirelashModels::createHookLayer);
        ModelLayerRegistry.registerModelLayer(WildscapesModelLayers.MIRELASH_SEGMENT, MirelashModels::createSegmentLayer);
        ModelLayerRegistry.registerModelLayer(WildscapesModelLayers.SLIME_BUBBLE, MirelashModels::createSlimeBubbleLayer);
        ModelLayerRegistry.registerModelLayer(WildscapesModelLayers.TONGUE_SEGMENT, AbominationModels::createTongueSegment);
        ModelLayerRegistry.registerModelLayer(WildscapesModelLayers.TONGUE_TIP, AbominationModels::createTongueTip);
        ModelLayerRegistry.registerModelLayer(WildscapesModelLayers.ABOMINATION_GRASP, AbominationModels::createGrasp);
        ModelLayerRegistry.registerModelLayer(CYPRESS_BOAT, BoatModel::createBoatModel);
        ModelLayerRegistry.registerModelLayer(CYPRESS_CHEST_BOAT, BoatModel::createChestBoatModel);
    }

    private static void registerRenderers() {
        EntityRendererRegistry.register(WildscapesEntities.ABOMINATION, AbominationRenderer::new);
        EntityRendererRegistry.register(WildscapesEntities.MIRELASH_HOOK, MirelashHookRenderer::new);
        EntityRendererRegistry.register(WildscapesEntities.RESIDUE_CLOUD, NoopRenderer::new);
        EntityRendererRegistry.register(WildscapesEntities.INCENDIARY_BOTTLE, ThrownItemRenderer::new);

        EntityRendererRegistry.register(EntityTypes.PILLAGER, SwampPillagerRenderer::new);
        EntityRendererRegistry.register(EntityTypes.VINDICATOR, SwampVindicatorRenderer::new);
        EntityRendererRegistry.register(EntityTypes.ILLUSIONER, RedesignedIllusionerRenderer::new);
        EntityRendererRegistry.register(EntityTypes.WITCH, RedesignedWitchRenderer::new);
        EntityRendererRegistry.register(EntityTypes.BOGGED, RedesignedBoggedRenderer::new);
        EntityRendererRegistry.register(EntityTypes.EVOKER, EmpoweredEvokerRenderer::new);
        EntityRendererRegistry.register(EntityTypes.SLIME, RedesignedSlimeRenderer::new);

        BlockEntityRendererRegistry.register(WildscapesBlockEntities.CAULDRON, CauldronRenderer::new);
        BlockEntityRendererRegistry.register(WildscapesBlockEntities.CAULDRON_OF_SOULS, CauldronOfSoulsRenderer::new);
        EntityRendererRegistry.register(CypressBoat.BOAT, context -> new BoatRenderer(context, CYPRESS_BOAT));
        EntityRendererRegistry.register(CypressBoat.CHEST_BOAT, context -> new BoatRenderer(context, CYPRESS_CHEST_BOAT));
    }

    private static void registerParticleProviders() {
        var particles = ParticleProviderRegistry.getInstance();
        particles.register(WildscapesParticles.BREW_BUBBLE, BrewBubbleParticle.Provider::new);
        particles.register(WildscapesParticles.INGREDIENT_STEAM, IngredientSteamParticle.Provider::new);
        particles.register(WildscapesParticles.RESIDUE_SPLAT,
                sprites -> new ResidueParticle.Provider(sprites, 11, 2.4F, 0.82F, 0.02F));
        particles.register(WildscapesParticles.RESIDUE_WISP,
                sprites -> new ResidueParticle.Provider(sprites, 18, 0.9F, 0.95F, 0.01F));
        particles.register(WildscapesParticles.RESIDUE_SWIRL,
                sprites -> new ResidueParticle.Provider(sprites, 14, 1.3F, 0.93F, 0.02F));
        particles.register(WildscapesParticles.SOUL_TRAIL, SoulTrailParticle.Provider::new);
        particles.register(WildscapesParticles.MULTI_JUMP, MultiJumpParticle.Provider::new);
        particles.register(WildscapesParticles.SOUL_HARVEST_SPARK,
                sprites -> new ResidueParticle.Provider(sprites, 16, 0.5F, 0.92F, 0.01F));
        particles.register(WildscapesParticles.EMPOWERED_WISP,
                sprites -> new ResidueParticle.Provider(sprites, 14, 1.1F, 0.94F, 0.0F));
        particles.register(WildscapesParticles.EMPOWERED_RING,
                sprites -> new ResidueParticle.Provider(sprites, 14, 1.2F, 0.94F, 0.0F));
        particles.register(WildscapesParticles.EMPOWERED_SOUL,
                sprites -> new ResidueParticle.Provider(sprites, 16, 0.9F, 0.94F, 0.01F));
        particles.register(WildscapesParticles.SOUL_MOTE,
                sprites -> new ResidueParticle.Provider(sprites, 20, 0.35F, 0.9F, 0.01F));
        particles.register(WildscapesParticles.LOST_SOUL,
                sprites -> new ResidueParticle.Provider(sprites, 14, 1.4F, 0.9F, 0.02F));
        particles.register(WildscapesParticles.BONFIRE_EMBER,
                sprites -> new ResidueParticle.Provider(sprites, 18, 0.3F, 0.96F, 0.0F));
        particles.register(WildscapesParticles.CAULDRON_SWIRL,
                sprites -> new ResidueParticle.Provider(sprites, 16, 1.2F, 0.95F, 0.01F));
        particles.register(WildscapesParticles.CAULDRON_REWARD,
                sprites -> new ResidueParticle.Provider(sprites, 14, 1.5F, 0.9F, 0.02F));
        particles.register(WildscapesParticles.SUMMON_WRAITH,
                sprites -> new ResidueParticle.Provider(sprites, 16, 1.8F, 0.92F, 0.02F));
        particles.register(WildscapesParticles.SUMMON_RISE,
                sprites -> new ResidueParticle.Provider(sprites, 16, 1.6F, 0.9F, 0.03F));
        particles.register(WildscapesParticles.SUMMON_MOTE,
                sprites -> new ResidueParticle.Provider(sprites, 18, 0.35F, 0.92F, 0.02F));
    }
}
