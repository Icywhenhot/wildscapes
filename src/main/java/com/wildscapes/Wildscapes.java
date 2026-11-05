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
import com.wildscapes.effect.Intangibility;
import com.wildscapes.effect.WildscapesEffects;
import com.wildscapes.effect.WildscapesPotions;
import com.wildscapes.entity.AbominationEntity;
import com.wildscapes.entity.IllusionerGoals;
import com.wildscapes.entity.SoulHarvest;
import com.wildscapes.entity.JumpEnchantments;
import com.wildscapes.entity.SlimeMerging;
import com.wildscapes.entity.SwampSpawns;
import com.wildscapes.entity.SwampVariants;
import com.wildscapes.entity.WildscapesEntities;
import com.wildscapes.entity.WitchGoals;
import com.wildscapes.item.CypressBoat;
import com.wildscapes.item.WildscapesDataComponents;
import com.wildscapes.item.MirelashItem;
import com.wildscapes.item.WildscapesItems;
import com.wildscapes.item.WitchCampMap;
import com.wildscapes.particle.WildscapesParticles;
import com.wildscapes.network.MirelashAttackPayload;
import com.wildscapes.network.JumpPayload;
import com.wildscapes.sound.WildscapesSounds;
import com.wildscapes.worldgen.SwampBiomes;
import com.wildscapes.worldgen.WildscapesFeatures;
import com.wildscapes.worldgen.WildscapesPlacementModifiers;
import com.wildscapes.worldgen.WildscapesStructures;

import net.minecraft.core.dispenser.BoatDispenseItemBehavior;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.common.NeoForge;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.village.WandererTradesEvent;
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
                    .icon(() -> WildscapesBlocks.CYPRESS_SAPLING.asItem().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(WildscapesBlocks.CYPRESS_LOG);
                        output.accept(WildscapesBlocks.STRIPPED_CYPRESS_LOG);
                        output.accept(WildscapesBlocks.CYPRESS_WOOD);
                        output.accept(WildscapesBlocks.STRIPPED_CYPRESS_WOOD);
                        output.accept(WildscapesBlocks.CYPRESS_PLANKS);
                        output.accept(WildscapesBlocks.CYPRESS_STAIRS);
                        output.accept(WildscapesBlocks.CYPRESS_SLAB);
                        output.accept(WildscapesBlocks.CYPRESS_FENCE);
                        output.accept(WildscapesBlocks.CYPRESS_FENCE_GATE);
                        output.accept(WildscapesBlocks.CYPRESS_LEAVES);
                        output.accept(WildscapesBlocks.CYPRESS_SAPLING);
                        output.accept(WildscapesBlocks.CYPRESS_DOOR);
                        output.accept(WildscapesBlocks.CYPRESS_TRAPDOOR);
                        output.accept(WildscapesBlocks.CYPRESS_PRESSURE_PLATE);
                        output.accept(WildscapesBlocks.CYPRESS_BUTTON);
                        output.accept(WildscapesItems.CYPRESS_SIGN.get());
                        output.accept(WildscapesItems.CYPRESS_HANGING_SIGN.get());
                        output.accept(WildscapesItems.CYPRESS_BOAT.get());
                        output.accept(WildscapesItems.CYPRESS_CHEST_BOAT.get());
                        output.accept(WildscapesBlocks.DUCKWEED);
                        output.accept(WildscapesBlocks.RUSHES);
                        output.accept(WildscapesBlocks.SHORT_RUSHES);
                        output.accept(WildscapesBlocks.LUMINOUS_MUSHROOM);
                        output.accept(WildscapesBlocks.BONFIRE);
                        output.accept(WildscapesBlocks.SOUL_BONFIRE);
                        output.accept(WildscapesBlocks.CAULDRON_OF_SOULS);
                        output.accept(WildscapesBlocks.SUMMONING_BONFIRE);
                        output.accept(MudBrickBlocks.CHISELED_MUD_BRICKS);
                        for (DyeColor color : DyeColor.values()) {
                            output.accept(MudBrickBlocks.BRICKS.get(color));
                            output.accept(MudBrickBlocks.CHISELED.get(color));
                            output.accept(MudBrickBlocks.STAIRS.get(color));
                            output.accept(MudBrickBlocks.SLABS.get(color));
                            output.accept(MudBrickBlocks.WALLS.get(color));
                        }
                        output.accept(WildscapesItems.FROG_LEGS.get());
                        output.accept(WildscapesItems.FROG_LEG_STEW.get());
                        output.accept(WildscapesItems.LADLE.get());
                        output.accept(WildscapesItems.MAGIC_SOUP.get());
                        output.accept(WildscapesItems.ABOMINATION_TONGUE.get());
                        output.accept(WildscapesItems.MIRELASH.get());
                        output.accept(WildscapesItems.ILLUSIONIST_NECKLACE.get());
                        output.accept(WildscapesItems.MERCHANTS_GUIDE.get());
                        output.accept(WildscapesItems.INCENDIARY_BOTTLE.get());
                        output.accept(WildscapesItems.SPELL_ARMOR_TRIM_SMITHING_TEMPLATE.get());
                        output.accept(WildscapesItems.VISCOSE_ARMOR_TRIM_SMITHING_TEMPLATE.get());
                        output.accept(WildscapesItems.SLIME_BANNER_PATTERN.get());
                        output.accept(WildscapesItems.SPIRIT_BANNER_PATTERN.get());
                        output.accept(WildscapesItems.ABOMINATION_SPAWN_EGG.get());
                        output.accept(WildscapesItems.ILLUSIONER_SPAWN_EGG.get());
                        output.accept(PotionContents.createItemStack(Items.POTION, WildscapesPotions.MIRAGE));
                        output.accept(PotionContents.createItemStack(Items.SPLASH_POTION, WildscapesPotions.MIRAGE));
                        output.accept(PotionContents.createItemStack(Items.LINGERING_POTION, WildscapesPotions.MIRAGE));
                        output.accept(PotionContents.createItemStack(Items.TIPPED_ARROW, WildscapesPotions.MIRAGE));
                        output.accept(PotionContents.createItemStack(Items.POTION, WildscapesPotions.LONG_MIRAGE));
                        output.accept(PotionContents.createItemStack(Items.SPLASH_POTION, WildscapesPotions.LONG_MIRAGE));
                        output.accept(PotionContents.createItemStack(Items.LINGERING_POTION, WildscapesPotions.LONG_MIRAGE));
                        output.accept(PotionContents.createItemStack(Items.TIPPED_ARROW, WildscapesPotions.LONG_MIRAGE));
                        output.accept(PotionContents.createItemStack(Items.POTION, WildscapesPotions.RESISTANCE));
                        output.accept(PotionContents.createItemStack(Items.SPLASH_POTION, WildscapesPotions.RESISTANCE));
                        output.accept(PotionContents.createItemStack(Items.LINGERING_POTION, WildscapesPotions.RESISTANCE));
                        output.accept(PotionContents.createItemStack(Items.TIPPED_ARROW, WildscapesPotions.RESISTANCE));
                        output.accept(PotionContents.createItemStack(Items.POTION, WildscapesPotions.LONG_RESISTANCE));
                        output.accept(PotionContents.createItemStack(Items.SPLASH_POTION, WildscapesPotions.LONG_RESISTANCE));
                        output.accept(PotionContents.createItemStack(Items.LINGERING_POTION, WildscapesPotions.LONG_RESISTANCE));
                        output.accept(PotionContents.createItemStack(Items.TIPPED_ARROW, WildscapesPotions.LONG_RESISTANCE));
                        output.accept(PotionContents.createItemStack(Items.POTION, WildscapesPotions.STRONG_RESISTANCE));
                        output.accept(PotionContents.createItemStack(Items.SPLASH_POTION, WildscapesPotions.STRONG_RESISTANCE));
                        output.accept(PotionContents.createItemStack(Items.LINGERING_POTION, WildscapesPotions.STRONG_RESISTANCE));
                        output.accept(PotionContents.createItemStack(Items.TIPPED_ARROW, WildscapesPotions.STRONG_RESISTANCE));
                        output.accept(PotionContents.createItemStack(Items.POTION, WildscapesPotions.GNASHING));
                        output.accept(PotionContents.createItemStack(Items.SPLASH_POTION, WildscapesPotions.GNASHING));
                        output.accept(PotionContents.createItemStack(Items.LINGERING_POTION, WildscapesPotions.GNASHING));
                        output.accept(PotionContents.createItemStack(Items.TIPPED_ARROW, WildscapesPotions.GNASHING));
                        output.accept(PotionContents.createItemStack(Items.POTION, WildscapesPotions.LONG_GNASHING));
                        output.accept(PotionContents.createItemStack(Items.SPLASH_POTION, WildscapesPotions.LONG_GNASHING));
                        output.accept(PotionContents.createItemStack(Items.LINGERING_POTION, WildscapesPotions.LONG_GNASHING));
                        output.accept(PotionContents.createItemStack(Items.TIPPED_ARROW, WildscapesPotions.LONG_GNASHING));
                        output.accept(PotionContents.createItemStack(Items.POTION, WildscapesPotions.STRONG_GNASHING));
                        output.accept(PotionContents.createItemStack(Items.SPLASH_POTION, WildscapesPotions.STRONG_GNASHING));
                        output.accept(PotionContents.createItemStack(Items.LINGERING_POTION, WildscapesPotions.STRONG_GNASHING));
                        output.accept(PotionContents.createItemStack(Items.TIPPED_ARROW, WildscapesPotions.STRONG_GNASHING));
                        output.accept(PotionContents.createItemStack(Items.SPLASH_POTION, WildscapesPotions.INCENDIARY));
                        output.accept(PotionContents.createItemStack(Items.LINGERING_POTION, WildscapesPotions.INCENDIARY));
                        output.accept(PotionContents.createItemStack(Items.TIPPED_ARROW, WildscapesPotions.INCENDIARY));
                        output.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(
                                parameters.holders().lookupOrThrow(Registries.ENCHANTMENT)
                                        .getOrThrow(MirelashItem.ELASTICITY), 1)));
                        output.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(
                                parameters.holders().lookupOrThrow(Registries.ENCHANTMENT)
                                        .getOrThrow(MirelashItem.SLIME_SNARE), 1)));
                        for (int i = 1; i <= 2; i++) {
                            output.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(
                                    parameters.holders().lookupOrThrow(Registries.ENCHANTMENT)
                                            .getOrThrow(JumpEnchantments.MULTI_JUMP), i)));
                        }
                        output.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(
                                parameters.holders().lookupOrThrow(Registries.ENCHANTMENT)
                                        .getOrThrow(JumpEnchantments.EMPOWERED_JUMP), 1)));
                    }).build());

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    public Wildscapes(IEventBus modEventBus) {
        WildscapesBlocks.register();
        WildscapesBlockEntities.register();
        WildscapesItems.register(modEventBus);
        WitchCampMap.register(modEventBus);
        WildscapesDataComponents.register();
        WildscapesEntities.register();
        WildscapesEffects.register();
        WildscapesPotions.register();
        SwampVariants.register(modEventBus);
        SoulHarvest.register(modEventBus);
        WildscapesSounds.register();
        WildscapesParticles.register();
        WildscapesFeatures.register();
        WildscapesPlacementModifiers.register();
        WildscapesStructures.register();
        SwampBiomes.register();
        CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerEntityAttributes);
        MirelashAttackPayload.register();
        JumpPayload.register();
        FabricPotionBrewingBuilder.BUILD.register(this::registerBrewingRecipes);

        NeoForge.EVENT_BUS.addListener(this::onLivingDrops);
        NeoForge.EVENT_BUS.addListener(this::onWandererTrades);
        NeoForge.EVENT_BUS.addListener(WitchCampMap::onVillagerTrades);
        NeoForge.EVENT_BUS.addListener(IllusionerGoals::onJoinLevel);
        NeoForge.EVENT_BUS.addListener(WitchGoals::onJoinLevel);
        NeoForge.EVENT_BUS.addListener(WitchGoals::onEntityTick);
        NeoForge.EVENT_BUS.addListener(MirageSync::onEntityTick);
        NeoForge.EVENT_BUS.addListener(MirageSync::onStartTracking);
        NeoForge.EVENT_BUS.addListener(Intangibility::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(Intangibility::onMobTick);
        NeoForge.EVENT_BUS.addListener(Intangibility::onDamage);
        NeoForge.EVENT_BUS.addListener(MirelashItem::onPlayerTick);
        UseBlockCallback.EVENT.register(MudBrickDyeing::onRightClick);
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
        if (entity.getType() == EntityType.ILLUSIONER) {
            ItemStack stack = new ItemStack(WildscapesItems.ILLUSIONIST_NECKLACE.get());
            event.getDrops().add(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), stack));
            return;
        }
        if (entity.getType() != EntityType.FROG) {
            return;
        }
        int count = 1 + entity.getRandom().nextInt(2);
        ItemStack stack = new ItemStack(WildscapesItems.FROG_LEGS.get(), count);
        event.getDrops().add(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), stack));
    }

    private void onWandererTrades(WandererTradesEvent event) {
        event.getGenericTrades().add(new BasicItemListing(8, new ItemStack(WildscapesItems.MERCHANTS_GUIDE.get()), 1, 1, 0.05f));
    }

    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(WildscapesEntities.ABOMINATION, AbominationEntity.createAttributes().build());
    }

    private void registerBrewingRecipes(PotionBrewing.Builder builder) {
        builder.addMix(WildscapesPotions.GNASHING, Items.REDSTONE, WildscapesPotions.LONG_GNASHING);
        builder.addMix(WildscapesPotions.GNASHING, Items.GLOWSTONE_DUST, WildscapesPotions.STRONG_GNASHING);
        builder.addMix(Potions.AWKWARD, Items.ENDER_PEARL, WildscapesPotions.MIRAGE);
        builder.addMix(WildscapesPotions.MIRAGE, Items.REDSTONE, WildscapesPotions.LONG_MIRAGE);
        builder.addMix(Potions.AWKWARD, Items.TURTLE_EGG, WildscapesPotions.RESISTANCE);
        builder.addMix(WildscapesPotions.RESISTANCE, Items.REDSTONE, WildscapesPotions.LONG_RESISTANCE);
        builder.addMix(WildscapesPotions.RESISTANCE, Items.GLOWSTONE_DUST,
                WildscapesPotions.STRONG_RESISTANCE);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            MudBrickDyeing.buildLookup();
            DispenserBlock.registerBehavior(WildscapesItems.CYPRESS_BOAT.get(),
                    new BoatDispenseItemBehavior(CypressBoat.TYPE.getValue()));
            DispenserBlock.registerBehavior(WildscapesItems.CYPRESS_CHEST_BOAT.get(),
                    new BoatDispenseItemBehavior(CypressBoat.TYPE.getValue(), true));

            FlammableBlockRegistry fire = FlammableBlockRegistry.getDefaultInstance();
            fire.add(WildscapesBlocks.CYPRESS_LOG, 5, 5);
            fire.add(WildscapesBlocks.STRIPPED_CYPRESS_LOG, 5, 5);
            fire.add(WildscapesBlocks.CYPRESS_WOOD, 5, 5);
            fire.add(WildscapesBlocks.STRIPPED_CYPRESS_WOOD, 5, 5);
            fire.add(WildscapesBlocks.CYPRESS_PLANKS, 5, 20);
            fire.add(WildscapesBlocks.CYPRESS_STAIRS, 5, 20);
            fire.add(WildscapesBlocks.CYPRESS_SLAB, 5, 20);
            fire.add(WildscapesBlocks.CYPRESS_FENCE, 5, 20);
            fire.add(WildscapesBlocks.CYPRESS_FENCE_GATE, 5, 20);
            fire.add(WildscapesBlocks.CYPRESS_LEAVES, 30, 60);
        });
    }
}
