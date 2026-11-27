package com.wildscapes;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import com.wildscapes.block.PavShrooms;
import com.wildscapes.block.entity.CauldronOfSoulsBlockEntity;
import com.wildscapes.entity.Bogged;
import com.wildscapes.entity.JumpEnchantments;
import com.wildscapes.worldgen.SwampBiomes;
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


import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.block.DispenserBlock;
import com.wildscapes.registry.RegistryObject;
import com.wildscapes.registry.DeferredRegister;

public class Wildscapes implements ModInitializer {
    public static final String MODID = "wildscapes";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<CreativeModeTab> WILDSCAPES_TAB =
            CREATIVE_MODE_TABS.register("wildscapes", () -> FabricItemGroup.builder()
                    .title(Component.translatable("itemGroup.wildscapes"))
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
                        output.accept(WildscapesBlocks.CYPRESS_SHELF.get());
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
                        if (!Backports.hasBottle()) {
                            output.accept(WildscapesItems.OMINOUS_BOTTLE.get());
                        }
                        output.accept(WildscapesItems.SPELL_ARMOR_TRIM_SMITHING_TEMPLATE.get());
                        output.accept(WildscapesItems.VISCOSE_ARMOR_TRIM_SMITHING_TEMPLATE.get());
                        output.accept(WildscapesItems.SLIME_BANNER_PATTERN.get());
                        output.accept(WildscapesItems.SPIRIT_BANNER_PATTERN.get());
                        output.accept(WildscapesItems.ABOMINATION_SPAWN_EGG.get());
                        if (!Backports.hasBogged()) {
                            output.accept(WildscapesItems.BOGGED_SPAWN_EGG.get());
                        }
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

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }

    @Override
    public void onInitialize() {
        WildscapesConfig.load();
        WildscapesBlocks.register();
        WildscapesBlockEntities.register();
        WildscapesEntities.register();
        WildscapesItems.register();
        WildscapesDecorations.register();
        WildscapesEnchantments.register();
        WildscapesEffects.register();
        WildscapesPotions.register();
        WildscapesSounds.register();
        WildscapesParticles.register();
        WildscapesFeatures.register();
        WildscapesPlacementModifiers.register();
        WildscapesStructures.register();
        CREATIVE_MODE_TABS.register();
        SwampBiomes.register();

        commonSetup();
        VanillaTweaks.setup();
        SwampSpawns.registerSpawnPlacements();
        FabricDefaultAttributeRegistry.register(WildscapesEntities.BOGGED.get(), Bogged.createAttributes());
        FabricDefaultAttributeRegistry.register(WildscapesEntities.ABOMINATION.get(), AbominationEntity.createAttributes());
        WildscapesNetwork.register();
        WitchCampMap.register();
        WitchRitual.register();
        addCompatPacks();

        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((blockEntity, level) -> {
            if (blockEntity instanceof CauldronOfSoulsBlockEntity cauldron) {
                cauldron.onLoad();
            }
        });
        ServerEntityEvents.ENTITY_LOAD.register(IllusionerGoals::onJoinLevel);
        ServerEntityEvents.ENTITY_LOAD.register(WitchGoals::onJoinLevel);
        ServerEntityEvents.ENTITY_LOAD.register(SwampVariants::onEntityJoinLevel);
        EntityTrackingEvents.START_TRACKING.register(MirageSync::onStartTracking);
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(Intangibility::allowDamage);
        ServerLivingEntityEvents.AFTER_DEATH.register(SoulHarvest::onDeath);
        UseBlockCallback.EVENT.register(MudBrickDyeing::onRightClick);
        UseBlockCallback.EVENT.register(CauldronSwap::onRightClick);
        UseBlockCallback.EVENT.register(MossCarpetSwap::onBonemeal);
    }

    public static void beforeEntityTick(Entity entity) {
        Intangibility.onMobTick(entity);
        if (entity instanceof Player player) {
            SoulHarvest.onPlayerTick(player);
        }
    }

    public static void tickEntity(Entity entity) {
        WitchGoals.onEntityTick(entity);
        MirageSync.onEntityTick(entity);
        Intangibility.onPlayerTick(entity);
        SlimeMerging.onEntityTick(entity);
        SoulHarvest.onEntityTick(entity);
        if (entity instanceof Player player && !player.level().isClientSide) {
            MirelashItem.onPlayerTick(player);
            JumpEnchantments.onTick(player);
            PavShrooms.onTick(player);
        }
    }

    private void addCompatPacks() {
        if (!FabricLoader.getInstance().isModLoaded("dye_depot")) {
            return;
        }
        ModContainer mod = FabricLoader.getInstance().getModContainer(MODID).orElseThrow();
        ResourceManagerHelper.registerBuiltinResourcePack(id("dye_depot"), mod,
                Component.literal("Wildscapes x Dye Depot"), ResourcePackActivationType.ALWAYS_ENABLED);
    }

    private void commonSetup() {
        PotionBrewing.addMix(WildscapesPotions.GNASHING.get(), Items.REDSTONE, WildscapesPotions.LONG_GNASHING.get());
        PotionBrewing.addMix(WildscapesPotions.GNASHING.get(), Items.GLOWSTONE_DUST, WildscapesPotions.STRONG_GNASHING.get());
        PotionBrewing.addMix(Potions.AWKWARD, Items.ENDER_PEARL, WildscapesPotions.MIRAGE.get());
        PotionBrewing.addMix(WildscapesPotions.MIRAGE.get(), Items.REDSTONE, WildscapesPotions.LONG_MIRAGE.get());
        PotionBrewing.addMix(Potions.AWKWARD, Items.TURTLE_EGG, WildscapesPotions.RESISTANCE.get());
        PotionBrewing.addMix(WildscapesPotions.RESISTANCE.get(), Items.REDSTONE, WildscapesPotions.LONG_RESISTANCE.get());
        PotionBrewing.addMix(WildscapesPotions.RESISTANCE.get(), Items.GLOWSTONE_DUST,
                WildscapesPotions.STRONG_RESISTANCE.get());
        MudBrickDyeing.buildLookup();

        CompostingChanceRegistry compost = CompostingChanceRegistry.INSTANCE;
        compost.add(WildscapesBlocks.CYPRESS_LEAVES.get(), 0.3F);
        compost.add(WildscapesBlocks.CYPRESS_SAPLING.get(), 0.3F);
        compost.add(WildscapesBlocks.DUCKWEED.get(), 0.3F);
        compost.add(WildscapesBlocks.SHORT_RUSHES.get(), 0.3F);
        compost.add(WildscapesBlocks.RUSHES.get(), 0.5F);
        compost.add(WildscapesBlocks.LUMINOUS_MUSHROOM.get(), 0.65F);

        DispenserBlock.registerBehavior(WildscapesItems.CYPRESS_BOAT.get(), CypressBoatItem.DISPENSER);
        DispenserBlock.registerBehavior(WildscapesItems.CYPRESS_CHEST_BOAT.get(), CypressBoatItem.DISPENSER);

        FlammableBlockRegistry fire = FlammableBlockRegistry.getDefaultInstance();
        fire.add(WildscapesBlocks.CYPRESS_LOG.get(), 5, 5);
        fire.add(WildscapesBlocks.STRIPPED_CYPRESS_LOG.get(), 5, 5);
        fire.add(WildscapesBlocks.CYPRESS_WOOD.get(), 5, 5);
        fire.add(WildscapesBlocks.STRIPPED_CYPRESS_WOOD.get(), 5, 5);
        fire.add(WildscapesBlocks.CYPRESS_PLANKS.get(), 5, 20);
        fire.add(WildscapesBlocks.CYPRESS_STAIRS.get(), 5, 20);
        fire.add(WildscapesBlocks.CYPRESS_SLAB.get(), 5, 20);
        fire.add(WildscapesBlocks.CYPRESS_FENCE.get(), 5, 20);
        fire.add(WildscapesBlocks.CYPRESS_FENCE_GATE.get(), 5, 20);
        fire.add(WildscapesBlocks.CYPRESS_SHELF.get(), 30, 20);
        fire.add(WildscapesBlocks.CYPRESS_LEAVES.get(), 30, 60);
    }
}
