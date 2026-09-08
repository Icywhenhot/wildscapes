package com.wildscapes;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.wildscapes.block.CauldronSwap;
import com.wildscapes.block.MossCarpetSwap;
import com.wildscapes.block.MudBrickBlocks;
import com.wildscapes.block.MudBrickDyeing;
import com.wildscapes.block.WildscapesBlocks;
import com.wildscapes.block.entity.WildscapesBlockEntities;
import com.wildscapes.effect.MirageSync;
import com.wildscapes.effect.WildscapesEffects;
import com.wildscapes.effect.WildscapesPotions;
import com.wildscapes.entity.AbominationEntity;
import com.wildscapes.entity.IllusionerGoals;
import com.wildscapes.entity.SlimeMerging;
import com.wildscapes.entity.SwampSpawns;
import com.wildscapes.entity.SwampVariants;
import com.wildscapes.entity.WildscapesEntities;
import com.wildscapes.entity.WitchGoals;
import com.wildscapes.item.WildscapesDataComponents;
import com.wildscapes.item.MirelashItem;
import com.wildscapes.item.WildscapesItems;
import com.wildscapes.particle.WildscapesParticles;
import com.wildscapes.network.MirelashAttackPayload;
import com.wildscapes.sound.WildscapesSounds;
import com.wildscapes.worldgen.WildscapesFeatures;
import com.wildscapes.worldgen.WildscapesPlacementModifiers;
import com.wildscapes.worldgen.WildscapesStructures;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(Wildscapes.MODID)
public class Wildscapes {
    public static final String MODID = "wildscapes";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> WILDSCAPES_TAB =
            CREATIVE_MODE_TABS.register("wildscapes", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.wildscapes"))
                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                    .icon(() -> WildscapesBlocks.CYPRESS_SAPLING.get().asItem().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(WildscapesBlocks.CYPRESS_LOG.get());
                        output.accept(WildscapesBlocks.STRIPPED_CYPRESS_LOG.get());
                        output.accept(WildscapesBlocks.CYPRESS_WOOD.get());
                        output.accept(WildscapesBlocks.STRIPPED_CYPRESS_WOOD.get());
                        output.accept(WildscapesBlocks.CYPRESS_PLANKS.get());
                        output.accept(WildscapesBlocks.CYPRESS_STAIRS.get());
                        output.accept(WildscapesBlocks.CYPRESS_SLAB.get());
                        output.accept(WildscapesBlocks.CYPRESS_FENCE.get());
                        output.accept(WildscapesBlocks.CYPRESS_FENCE_GATE.get());
                        output.accept(WildscapesBlocks.CYPRESS_LEAVES.get());
                        output.accept(WildscapesBlocks.CYPRESS_SAPLING.get());
                        output.accept(WildscapesBlocks.CYPRESS_DOOR.get());
                        output.accept(WildscapesBlocks.CYPRESS_TRAPDOOR.get());
                        output.accept(WildscapesBlocks.DUCKWEED.get());
                        output.accept(WildscapesBlocks.RUSHES.get());
                        output.accept(WildscapesBlocks.SHORT_RUSHES.get());
                        output.accept(WildscapesBlocks.BONFIRE.get());
                        output.accept(WildscapesBlocks.SOUL_BONFIRE.get());
                        output.accept(WildscapesBlocks.WITCH_CAULDRON.get());
                        output.accept(MudBrickBlocks.CHISELED_MUD_BRICKS.get());
                        for (DyeColor color : DyeColor.values()) {
                            output.accept(MudBrickBlocks.BRICKS.get(color).get());
                            output.accept(MudBrickBlocks.CHISELED.get(color).get());
                            output.accept(MudBrickBlocks.STAIRS.get(color).get());
                            output.accept(MudBrickBlocks.SLABS.get(color).get());
                            output.accept(MudBrickBlocks.WALLS.get(color).get());
                        }
                        output.accept(WildscapesItems.FROG_LEGS.get());
                        output.accept(WildscapesItems.LADLE.get());
                        output.accept(WildscapesItems.MAGIC_SOUP.get());
                        output.accept(WildscapesItems.ABOMINATION_TONGUE.get());
                        output.accept(WildscapesItems.MIRELASH.get());
                        output.accept(WildscapesItems.ABOMINATION_SPAWN_EGG.get());
                    }).build());

    public Wildscapes(IEventBus modEventBus) {
        WildscapesBlocks.register(modEventBus);
        WildscapesBlockEntities.register(modEventBus);
        WildscapesItems.register(modEventBus);
        WildscapesDataComponents.register(modEventBus);
        WildscapesEntities.register(modEventBus);
        WildscapesEffects.register(modEventBus);
        WildscapesPotions.register(modEventBus);
        SwampVariants.register(modEventBus);
        WildscapesSounds.register(modEventBus);
        WildscapesParticles.register(modEventBus);
        WildscapesFeatures.register(modEventBus);
        WildscapesPlacementModifiers.register(modEventBus);
        WildscapesStructures.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerEntityAttributes);
        modEventBus.addListener(MirelashAttackPayload::register);
        NeoForge.EVENT_BUS.addListener(this::registerBrewingRecipes);

        NeoForge.EVENT_BUS.addListener(this::onLivingDrops);
        NeoForge.EVENT_BUS.addListener(IllusionerGoals::onJoinLevel);
        NeoForge.EVENT_BUS.addListener(WitchGoals::onJoinLevel);
        NeoForge.EVENT_BUS.addListener(WitchGoals::onEntityTick);
        NeoForge.EVENT_BUS.addListener(MirageSync::onEntityTick);
        NeoForge.EVENT_BUS.addListener(MirageSync::onStartTracking);
        NeoForge.EVENT_BUS.addListener(MirelashItem::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(MudBrickDyeing::onRightClick);
        NeoForge.EVENT_BUS.addListener(SlimeMerging::onEntityTick);
        NeoForge.EVENT_BUS.addListener(CauldronSwap::onRightClick);
        NeoForge.EVENT_BUS.addListener(CauldronSwap::onPlace);
        NeoForge.EVENT_BUS.addListener(CauldronSwap::onNeighborNotify);
        NeoForge.EVENT_BUS.addListener(MossCarpetSwap::onPlace);
        NeoForge.EVENT_BUS.addListener(MossCarpetSwap::onBonemeal);

        modEventBus.addListener(SwampSpawns::registerSpawnPlacements);
    }

    private void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getType() != EntityType.FROG) {
            return;
        }
        int count = 1 + entity.getRandom().nextInt(2);
        ItemStack stack = new ItemStack(WildscapesItems.FROG_LEGS.get(), count);
        event.getDrops().add(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), stack));
    }

    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(WildscapesEntities.ABOMINATION.get(), AbominationEntity.createAttributes().build());
    }

    private void registerBrewingRecipes(RegisterBrewingRecipesEvent event) {
        event.getBuilder().addMix(Potions.AWKWARD, Items.ENDER_PEARL, WildscapesPotions.MIRAGE);
        event.getBuilder().addMix(WildscapesPotions.MIRAGE, Items.REDSTONE, WildscapesPotions.LONG_MIRAGE);
        event.getBuilder().addMix(Potions.AWKWARD, Items.TURTLE_EGG, WildscapesPotions.RESISTANCE);
        event.getBuilder().addMix(WildscapesPotions.RESISTANCE, Items.REDSTONE, WildscapesPotions.LONG_RESISTANCE);
        event.getBuilder().addMix(WildscapesPotions.RESISTANCE, Items.GLOWSTONE_DUST,
                WildscapesPotions.STRONG_RESISTANCE);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            MudBrickDyeing.buildLookup();

            FireBlock fire = (FireBlock) Blocks.FIRE;
            fire.setFlammable(WildscapesBlocks.CYPRESS_LOG.get(), 5, 5);
            fire.setFlammable(WildscapesBlocks.STRIPPED_CYPRESS_LOG.get(), 5, 5);
            fire.setFlammable(WildscapesBlocks.CYPRESS_WOOD.get(), 5, 5);
            fire.setFlammable(WildscapesBlocks.STRIPPED_CYPRESS_WOOD.get(), 5, 5);
            fire.setFlammable(WildscapesBlocks.CYPRESS_PLANKS.get(), 5, 20);
            fire.setFlammable(WildscapesBlocks.CYPRESS_LEAVES.get(), 30, 60);
        });
    }
}
