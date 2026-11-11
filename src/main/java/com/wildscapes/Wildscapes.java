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
import com.wildscapes.entity.SlimeMerging;
import com.wildscapes.entity.SwampSpawns;
import com.wildscapes.entity.SwampVariants;
import com.wildscapes.entity.WildscapesEntities;
import com.wildscapes.entity.WitchGoals;
import com.wildscapes.entity.WitchRitual;
import com.wildscapes.item.CypressBoatItem;
import com.wildscapes.item.WildscapesEnchantments;
import com.wildscapes.item.VanillaTweaks;
import com.wildscapes.item.MirelashItem;
import com.wildscapes.item.WildscapesItems;
import com.wildscapes.item.WitchCampMap;
import com.wildscapes.particle.WildscapesParticles;
import com.wildscapes.network.WildscapesNetwork;
import com.wildscapes.sound.WildscapesSounds;
import com.wildscapes.worldgen.WildscapesFeatures;
import com.wildscapes.worldgen.WildscapesPlacementModifiers;
import com.wildscapes.worldgen.WildscapesStructures;

import net.minecraft.core.dispenser.BoatDispenseItemBehavior;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.common.BasicItemListing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

@Mod(Wildscapes.MODID)
public class Wildscapes {
    public static final String MODID = "wildscapes";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<CreativeModeTab> WILDSCAPES_TAB =
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
                        output.accept(WildscapesBlocks.CYPRESS_PRESSURE_PLATE.get());
                        output.accept(WildscapesBlocks.CYPRESS_BUTTON.get());
                        output.accept(WildscapesItems.CYPRESS_SIGN.get());
                        output.accept(WildscapesItems.CYPRESS_HANGING_SIGN.get());
                        output.accept(WildscapesItems.CYPRESS_BOAT.get());
                        output.accept(WildscapesItems.CYPRESS_CHEST_BOAT.get());
                        output.accept(WildscapesBlocks.DUCKWEED.get());
                        output.accept(WildscapesBlocks.RUSHES.get());
                        output.accept(WildscapesBlocks.SHORT_RUSHES.get());
                        output.accept(WildscapesBlocks.LUMINOUS_MUSHROOM.get());
                        output.accept(WildscapesBlocks.BONFIRE.get());
                        output.accept(WildscapesBlocks.SOUL_BONFIRE.get());
                        output.accept(WildscapesBlocks.CAULDRON_OF_SOULS.get());
                        output.accept(WildscapesBlocks.SUMMONING_BONFIRE.get());
                        output.accept(MudBrickBlocks.CHISELED_MUD_BRICKS.get());
                        for (DyeColor color : DyeColor.values()) {
                            output.accept(MudBrickBlocks.BRICKS.get(color).get());
                            output.accept(MudBrickBlocks.CHISELED.get(color).get());
                            output.accept(MudBrickBlocks.STAIRS.get(color).get());
                            output.accept(MudBrickBlocks.SLABS.get(color).get());
                            output.accept(MudBrickBlocks.WALLS.get(color).get());
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
                        output.accept(WildscapesItems.OMINOUS_BOTTLE.get());
                        output.accept(WildscapesItems.SPELL_ARMOR_TRIM_SMITHING_TEMPLATE.get());
                        output.accept(WildscapesItems.VISCOSE_ARMOR_TRIM_SMITHING_TEMPLATE.get());
                        output.accept(WildscapesItems.SLIME_BANNER_PATTERN.get());
                        output.accept(WildscapesItems.SPIRIT_BANNER_PATTERN.get());
                        output.accept(WildscapesItems.ABOMINATION_SPAWN_EGG.get());
                        output.accept(WildscapesItems.BOGGED_SPAWN_EGG.get());
                        output.accept(WildscapesItems.ILLUSIONER_SPAWN_EGG.get());
                        output.accept(PotionUtils.setPotion(new ItemStack(Items.POTION), WildscapesPotions.MIRAGE.get()));
                        output.accept(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), WildscapesPotions.MIRAGE.get()));
                        output.accept(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), WildscapesPotions.MIRAGE.get()));
                        output.accept(PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), WildscapesPotions.MIRAGE.get()));
                        output.accept(PotionUtils.setPotion(new ItemStack(Items.POTION), WildscapesPotions.LONG_MIRAGE.get()));
                        output.accept(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), WildscapesPotions.LONG_MIRAGE.get()));
                        output.accept(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), WildscapesPotions.LONG_MIRAGE.get()));
                        output.accept(PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), WildscapesPotions.LONG_MIRAGE.get()));
                        output.accept(PotionUtils.setPotion(new ItemStack(Items.POTION), WildscapesPotions.RESISTANCE.get()));
                        output.accept(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), WildscapesPotions.RESISTANCE.get()));
                        output.accept(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), WildscapesPotions.RESISTANCE.get()));
                        output.accept(PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), WildscapesPotions.RESISTANCE.get()));
                        output.accept(PotionUtils.setPotion(new ItemStack(Items.POTION), WildscapesPotions.LONG_RESISTANCE.get()));
                        output.accept(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), WildscapesPotions.LONG_RESISTANCE.get()));
                        output.accept(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), WildscapesPotions.LONG_RESISTANCE.get()));
                        output.accept(PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), WildscapesPotions.LONG_RESISTANCE.get()));
                        output.accept(PotionUtils.setPotion(new ItemStack(Items.POTION), WildscapesPotions.STRONG_RESISTANCE.get()));
                        output.accept(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), WildscapesPotions.STRONG_RESISTANCE.get()));
                        output.accept(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), WildscapesPotions.STRONG_RESISTANCE.get()));
                        output.accept(PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), WildscapesPotions.STRONG_RESISTANCE.get()));
                        output.accept(PotionUtils.setPotion(new ItemStack(Items.POTION), WildscapesPotions.GNASHING.get()));
                        output.accept(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), WildscapesPotions.GNASHING.get()));
                        output.accept(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), WildscapesPotions.GNASHING.get()));
                        output.accept(PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), WildscapesPotions.GNASHING.get()));
                        output.accept(PotionUtils.setPotion(new ItemStack(Items.POTION), WildscapesPotions.LONG_GNASHING.get()));
                        output.accept(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), WildscapesPotions.LONG_GNASHING.get()));
                        output.accept(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), WildscapesPotions.LONG_GNASHING.get()));
                        output.accept(PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), WildscapesPotions.LONG_GNASHING.get()));
                        output.accept(PotionUtils.setPotion(new ItemStack(Items.POTION), WildscapesPotions.STRONG_GNASHING.get()));
                        output.accept(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), WildscapesPotions.STRONG_GNASHING.get()));
                        output.accept(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), WildscapesPotions.STRONG_GNASHING.get()));
                        output.accept(PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), WildscapesPotions.STRONG_GNASHING.get()));
                        output.accept(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), WildscapesPotions.INCENDIARY.get()));
                        output.accept(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), WildscapesPotions.INCENDIARY.get()));
                        output.accept(PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), WildscapesPotions.INCENDIARY.get()));
                        output.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(
                                WildscapesEnchantments.ELASTICITY.get(), 1)));
                        output.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(
                                WildscapesEnchantments.SLIME_SNARE.get(), 1)));
                        for (int i = 1; i <= 2; i++) {
                            output.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(
                                    WildscapesEnchantments.MULTI_JUMP.get(), i)));
                        }
                        output.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(
                                WildscapesEnchantments.EMPOWERED_JUMP.get(), 1)));
                    }).build());

    public Wildscapes(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        context.registerConfig(ModConfig.Type.SERVER, WildscapesConfig.SERVER_SPEC);
        context.registerConfig(ModConfig.Type.CLIENT, WildscapesConfig.CLIENT_SPEC);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            WildscapesClient.registerConfigScreen(context);
        }
        WildscapesBlocks.register(modEventBus);
        WildscapesBlockEntities.register(modEventBus);
        WildscapesItems.register(modEventBus);
        WildscapesDecorations.register(modEventBus);
        WildscapesEnchantments.register(modEventBus);
        WildscapesEntities.register(modEventBus);
        WildscapesEffects.register(modEventBus);
        WildscapesPotions.register(modEventBus);
        WildscapesSounds.register(modEventBus);
        WildscapesParticles.register(modEventBus);
        WildscapesFeatures.register(modEventBus);
        WildscapesPlacementModifiers.register(modEventBus);
        WildscapesStructures.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(VanillaTweaks::setup);
        modEventBus.addListener(this::registerEntityAttributes);
        WildscapesNetwork.register();

        MinecraftForge.EVENT_BUS.addListener(this::onLivingDrops);
        MinecraftForge.EVENT_BUS.addListener(this::onWandererTrades);
        MinecraftForge.EVENT_BUS.addListener(WitchCampMap::onVillagerTrades);
        MinecraftForge.EVENT_BUS.addListener(IllusionerGoals::onJoinLevel);
        MinecraftForge.EVENT_BUS.addListener(WitchGoals::onJoinLevel);
        MinecraftForge.EVENT_BUS.addListener(MirageSync::onStartTracking);
        MinecraftForge.EVENT_BUS.addListener(Intangibility::onMobTick);
        MinecraftForge.EVENT_BUS.addListener(Intangibility::onDamage);
        MinecraftForge.EVENT_BUS.addListener(MirelashItem::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(MudBrickDyeing::onRightClick);
        MinecraftForge.EVENT_BUS.addListener(CauldronSwap::onRightClick);
        MinecraftForge.EVENT_BUS.addListener(CauldronSwap::onPlace);
        MinecraftForge.EVENT_BUS.addListener(CauldronSwap::onNeighborNotify);
        MinecraftForge.EVENT_BUS.addListener(MossCarpetSwap::onPlace);
        MinecraftForge.EVENT_BUS.addListener(MossCarpetSwap::onBonemeal);

        modEventBus.addListener(SwampSpawns::registerSpawnPlacements);
    }

    public static void tickEntity(net.minecraft.world.entity.Entity entity) {
        WitchGoals.onEntityTick(entity);
        MirageSync.onEntityTick(entity);
        Intangibility.onPlayerTick(entity);
        SlimeMerging.onEntityTick(entity);
        SoulHarvest.onEntityTick(entity);
    }

    private void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getType() == EntityType.ILLUSIONER) {
            if (entity.getRandom().nextDouble() >= WildscapesConfig.get(WildscapesConfig.NECKLACE_DROP_CHANCE)) {
                return;
            }
            ItemStack stack = new ItemStack(WildscapesItems.ILLUSIONIST_NECKLACE.get());
            event.getDrops().add(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), stack));
            return;
        }
        if (entity.getType() != EntityType.FROG || !WildscapesConfig.get(WildscapesConfig.FROG_LEGS)) {
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
        event.put(WildscapesEntities.BOGGED.get(), com.wildscapes.entity.Bogged.createAttributes().build());
        event.put(WildscapesEntities.ABOMINATION.get(), AbominationEntity.createAttributes().build());
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            PotionBrewing.addMix(WildscapesPotions.GNASHING.get(), Items.REDSTONE, WildscapesPotions.LONG_GNASHING.get());
            PotionBrewing.addMix(WildscapesPotions.GNASHING.get(), Items.GLOWSTONE_DUST, WildscapesPotions.STRONG_GNASHING.get());
            PotionBrewing.addMix(Potions.AWKWARD, Items.ENDER_PEARL, WildscapesPotions.MIRAGE.get());
            PotionBrewing.addMix(WildscapesPotions.MIRAGE.get(), Items.REDSTONE, WildscapesPotions.LONG_MIRAGE.get());
            PotionBrewing.addMix(Potions.AWKWARD, Items.TURTLE_EGG, WildscapesPotions.RESISTANCE.get());
            PotionBrewing.addMix(WildscapesPotions.RESISTANCE.get(), Items.REDSTONE, WildscapesPotions.LONG_RESISTANCE.get());
            PotionBrewing.addMix(WildscapesPotions.RESISTANCE.get(), Items.GLOWSTONE_DUST,
                    WildscapesPotions.STRONG_RESISTANCE.get());
            MudBrickDyeing.buildLookup();
            ComposterBlock.COMPOSTABLES.put(WildscapesBlocks.CYPRESS_LEAVES.get().asItem(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(WildscapesBlocks.CYPRESS_SAPLING.get().asItem(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(WildscapesBlocks.DUCKWEED.get().asItem(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(WildscapesBlocks.SHORT_RUSHES.get().asItem(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(WildscapesBlocks.RUSHES.get().asItem(), 0.5F);
            ComposterBlock.COMPOSTABLES.put(WildscapesBlocks.LUMINOUS_MUSHROOM.get().asItem(), 0.65F);
            DispenserBlock.registerBehavior(WildscapesItems.CYPRESS_BOAT.get(),
                    CypressBoatItem.DISPENSER);
            DispenserBlock.registerBehavior(WildscapesItems.CYPRESS_CHEST_BOAT.get(),
                    CypressBoatItem.DISPENSER);

            FireBlock fire = (FireBlock) Blocks.FIRE;
            fire.setFlammable(WildscapesBlocks.CYPRESS_LOG.get(), 5, 5);
            fire.setFlammable(WildscapesBlocks.STRIPPED_CYPRESS_LOG.get(), 5, 5);
            fire.setFlammable(WildscapesBlocks.CYPRESS_WOOD.get(), 5, 5);
            fire.setFlammable(WildscapesBlocks.STRIPPED_CYPRESS_WOOD.get(), 5, 5);
            fire.setFlammable(WildscapesBlocks.CYPRESS_PLANKS.get(), 5, 20);
            fire.setFlammable(WildscapesBlocks.CYPRESS_STAIRS.get(), 5, 20);
            fire.setFlammable(WildscapesBlocks.CYPRESS_SLAB.get(), 5, 20);
            fire.setFlammable(WildscapesBlocks.CYPRESS_FENCE.get(), 5, 20);
            fire.setFlammable(WildscapesBlocks.CYPRESS_FENCE_GATE.get(), 5, 20);
            fire.setFlammable(WildscapesBlocks.CYPRESS_LEAVES.get(), 30, 60);
        });
    }
}
