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

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.entity.EntityTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterConditionalItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = Wildscapes.MODID, dist = Dist.CLIENT)
public final class WildscapesClient {
    private static final ModelLayerLocation CYPRESS_BOAT = new ModelLayerLocation(Wildscapes.id("boat/cypress"), "main");
    private static final ModelLayerLocation CYPRESS_CHEST_BOAT = new ModelLayerLocation(Wildscapes.id("chest_boat/cypress"), "main");

    public WildscapesClient(IEventBus modBus) {
        modBus.addListener(WildscapesClient::registerLayerDefinitions);
        modBus.addListener(WildscapesClient::registerRenderers);
        modBus.addListener(WildscapesClient::registerParticleProviders);
        modBus.addListener(RegisterConditionalItemModelPropertyEvent.class, WildscapesItemCondition::register);
        modBus.addListener(ModelEvent.ModifyBakingResult.class, SwampMossModel::onModifyBakingResult);
        PhaseVision.register(modBus);
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> SwampFog.onClientTick(Minecraft.getInstance()));
    }

    private static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(WildscapesModelLayers.PILLAGER_SWAMP, SwampIllagerModels::createPillagerLayer);
        event.registerLayerDefinition(WildscapesModelLayers.VINDICATOR, SwampIllagerModels::createVindicatorLayer);
        event.registerLayerDefinition(WildscapesModelLayers.ILLUSIONER, RedesignedIllusionerModel::createBodyLayer);
        event.registerLayerDefinition(WildscapesModelLayers.WITCH, RedesignedWitchModel::createBodyLayer);
        event.registerLayerDefinition(WildscapesModelLayers.BOGGED, RedesignedBoggedModel::createBodyLayer);
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
        event.registerLayerDefinition(CYPRESS_BOAT, BoatModel::createBoatModel);
        event.registerLayerDefinition(CYPRESS_CHEST_BOAT, BoatModel::createChestBoatModel);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(WildscapesEntities.ABOMINATION, AbominationRenderer::new);
        event.registerEntityRenderer(WildscapesEntities.MIRELASH_HOOK, MirelashHookRenderer::new);
        event.registerEntityRenderer(WildscapesEntities.RESIDUE_CLOUD, NoopRenderer::new);
        event.registerEntityRenderer(WildscapesEntities.INCENDIARY_BOTTLE, ThrownItemRenderer::new);

        event.registerEntityRenderer(EntityTypes.PILLAGER, SwampPillagerRenderer::new);
        event.registerEntityRenderer(EntityTypes.VINDICATOR, SwampVindicatorRenderer::new);
        event.registerEntityRenderer(EntityTypes.ILLUSIONER, RedesignedIllusionerRenderer::new);
        event.registerEntityRenderer(EntityTypes.WITCH, RedesignedWitchRenderer::new);
        event.registerEntityRenderer(EntityTypes.BOGGED, RedesignedBoggedRenderer::new);
        event.registerEntityRenderer(EntityTypes.EVOKER, EmpoweredEvokerRenderer::new);
        event.registerEntityRenderer(EntityTypes.SLIME, RedesignedSlimeRenderer::new);

        event.registerBlockEntityRenderer(WildscapesBlockEntities.CAULDRON, CauldronRenderer::new);
        event.registerBlockEntityRenderer(WildscapesBlockEntities.CAULDRON_OF_SOULS, CauldronOfSoulsRenderer::new);
        event.registerEntityRenderer(CypressBoat.BOAT, context -> new BoatRenderer(context, CYPRESS_BOAT));
        event.registerEntityRenderer(CypressBoat.CHEST_BOAT, context -> new BoatRenderer(context, CYPRESS_CHEST_BOAT));
    }

    private static void registerParticleProviders(RegisterParticleProvidersEvent particles) {
        particles.registerSpriteSet(WildscapesParticles.BREW_BUBBLE, BrewBubbleParticle.Provider::new);
        particles.registerSpriteSet(WildscapesParticles.INGREDIENT_STEAM, IngredientSteamParticle.Provider::new);
        particles.registerSpriteSet(WildscapesParticles.RESIDUE_SPLAT,
                sprites -> new ResidueParticle.Provider(sprites, 11, 2.4F, 0.82F, 0.02F));
        particles.registerSpriteSet(WildscapesParticles.RESIDUE_WISP,
                sprites -> new ResidueParticle.Provider(sprites, 18, 0.9F, 0.95F, 0.01F));
        particles.registerSpriteSet(WildscapesParticles.RESIDUE_SWIRL,
                sprites -> new ResidueParticle.Provider(sprites, 14, 1.3F, 0.93F, 0.02F));
        particles.registerSpriteSet(WildscapesParticles.SOUL_TRAIL, SoulTrailParticle.Provider::new);
        particles.registerSpriteSet(WildscapesParticles.MULTI_JUMP, MultiJumpParticle.Provider::new);
        particles.registerSpriteSet(WildscapesParticles.SOUL_HARVEST_SPARK,
                sprites -> new ResidueParticle.Provider(sprites, 16, 0.5F, 0.92F, 0.01F));
        particles.registerSpriteSet(WildscapesParticles.EMPOWERED_WISP,
                sprites -> new ResidueParticle.Provider(sprites, 14, 1.1F, 0.94F, 0.0F));
        particles.registerSpriteSet(WildscapesParticles.EMPOWERED_RING,
                sprites -> new ResidueParticle.Provider(sprites, 14, 1.2F, 0.94F, 0.0F));
        particles.registerSpriteSet(WildscapesParticles.EMPOWERED_SOUL,
                sprites -> new ResidueParticle.Provider(sprites, 16, 0.9F, 0.94F, 0.01F));
        particles.registerSpriteSet(WildscapesParticles.SOUL_MOTE,
                sprites -> new ResidueParticle.Provider(sprites, 20, 0.35F, 0.9F, 0.01F));
        particles.registerSpriteSet(WildscapesParticles.LOST_SOUL,
                sprites -> new ResidueParticle.Provider(sprites, 14, 1.4F, 0.9F, 0.02F));
        particles.registerSpriteSet(WildscapesParticles.BONFIRE_EMBER,
                sprites -> new ResidueParticle.Provider(sprites, 18, 0.3F, 0.96F, 0.0F));
        particles.registerSpriteSet(WildscapesParticles.CAULDRON_SWIRL,
                sprites -> new ResidueParticle.Provider(sprites, 16, 1.2F, 0.95F, 0.01F));
        particles.registerSpriteSet(WildscapesParticles.CAULDRON_REWARD,
                sprites -> new ResidueParticle.Provider(sprites, 14, 1.5F, 0.9F, 0.02F));
        particles.registerSpriteSet(WildscapesParticles.SUMMON_WRAITH,
                sprites -> new ResidueParticle.Provider(sprites, 16, 1.8F, 0.92F, 0.02F));
        particles.registerSpriteSet(WildscapesParticles.SUMMON_RISE,
                sprites -> new ResidueParticle.Provider(sprites, 16, 1.6F, 0.9F, 0.03F));
        particles.registerSpriteSet(WildscapesParticles.SUMMON_MOTE,
                sprites -> new ResidueParticle.Provider(sprites, 18, 0.35F, 0.92F, 0.02F));
    }
}
