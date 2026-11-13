package com.wildscapes;

import java.util.ArrayList;
import java.util.List;

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
import com.wildscapes.entity.SwampVariants;
import com.wildscapes.entity.WitchRitual;
import com.wildscapes.entity.WildscapesEntities;
import com.wildscapes.entity.WitchGoals;
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

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import com.wildscapes.gametest.WildscapesTests;
import com.wildscapes.item.VanillaTweaks;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.world.item.alchemy.PotionBrewing;

public class Wildscapes implements ModInitializer {
    public static final String MODID = "wildscapes";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final CreativeModeTab WILDSCAPES_TAB = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .title(Component.translatable("itemGroup.wildscapes"))
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
                        output.accept(WildscapesItems.CYPRESS_SIGN);
                        output.accept(WildscapesItems.CYPRESS_HANGING_SIGN);
                        output.accept(WildscapesItems.CYPRESS_BOAT);
                        output.accept(WildscapesItems.CYPRESS_CHEST_BOAT);
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
                        output.accept(WildscapesItems.FROG_LEGS);
                        output.accept(WildscapesItems.FROG_LEG_STEW);
                        output.accept(WildscapesItems.LADLE);
                        output.accept(WildscapesItems.MAGIC_SOUP);
                        output.accept(WildscapesItems.ABOMINATION_TONGUE);
                        output.accept(WildscapesItems.MIRELASH);
                        output.accept(WildscapesItems.ILLUSIONIST_NECKLACE);
                        output.accept(WildscapesItems.MERCHANTS_GUIDE);
                        output.accept(WildscapesItems.INCENDIARY_BOTTLE);
                        output.accept(WildscapesItems.SPELL_ARMOR_TRIM_SMITHING_TEMPLATE);
                        output.accept(WildscapesItems.VISCOSE_ARMOR_TRIM_SMITHING_TEMPLATE);
                        output.accept(WildscapesItems.SLIME_BANNER_PATTERN);
                        output.accept(WildscapesItems.SPIRIT_BANNER_PATTERN);
                        output.accept(WildscapesItems.ABOMINATION_SPAWN_EGG);
                        output.accept(WildscapesItems.ILLUSIONER_SPAWN_EGG);
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
                        var enchantments = parameters.holders().lookupOrThrow(Registries.ENCHANTMENT);
                        List<ItemStack> books = new ArrayList<>();
                        books.add(EnchantmentHelper.createBook(new EnchantmentInstance(
                                enchantments.getOrThrow(MirelashItem.ELASTICITY), 1)));
                        books.add(EnchantmentHelper.createBook(new EnchantmentInstance(
                                enchantments.getOrThrow(MirelashItem.SLIME_SNARE), 1)));
                        for (int i = 1; i <= 2; i++) {
                            books.add(EnchantmentHelper.createBook(new EnchantmentInstance(
                                    enchantments.getOrThrow(JumpEnchantments.MULTI_JUMP), i)));
                        }
                        books.add(EnchantmentHelper.createBook(new EnchantmentInstance(
                                enchantments.getOrThrow(JumpEnchantments.EMPOWERED_JUMP), 1)));
                        List<ItemStack> added = new ArrayList<>();
                        for (ItemStack book : books) {
                            if (added.stream().noneMatch(seen -> ItemStack.isSameItemSameComponents(seen, book))) {
                                added.add(book);
                                output.accept(book);
                            }
                        }
                    }).build();

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    @Override
    public void onInitialize() {
        WildscapesBlocks.register();
        WildscapesBlockEntities.register();
        WildscapesDataComponents.register();
        WildscapesEntities.register();
        WildscapesItems.register();
        WitchCampMap.register();
        WildscapesEffects.register();
        WildscapesPotions.register();
        WildscapesSounds.register();
        WildscapesParticles.register();
        WildscapesFeatures.register();
        WildscapesPlacementModifiers.register();
        WildscapesStructures.register();
        WildscapesTests.register();
        SwampBiomes.register();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id("wildscapes"), WILDSCAPES_TAB);

        commonSetup();
        FabricDefaultAttributeRegistry.register(WildscapesEntities.ABOMINATION, AbominationEntity.createAttributes());
        MirelashAttackPayload.register();
        JumpPayload.register();
        FabricPotionBrewingBuilder.BUILD.register(this::registerBrewingRecipes);

        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((blockEntity, level) -> {
            if (blockEntity instanceof com.wildscapes.block.entity.CauldronOfSoulsBlockEntity cauldron) {
                cauldron.onLoad();
            }
        });
        ServerEntityEvents.ENTITY_LOAD.register(IllusionerGoals::onJoinLevel);
        ServerEntityEvents.ENTITY_LOAD.register(WitchGoals::onJoinLevel);
        SwampVariants.register();
        WitchRitual.register();
        EntityTrackingEvents.START_TRACKING.register(MirageSync::onStartTracking);
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(Intangibility::allowDamage);
        ServerLivingEntityEvents.AFTER_DEATH.register(SoulHarvest::onDeath);
        UseBlockCallback.EVENT.register(MudBrickDyeing::onRightClick);
        UseBlockCallback.EVENT.register(CauldronSwap::onRightClick);
        UseBlockCallback.EVENT.register(MossCarpetSwap::onBonemeal);
        VanillaTweaks.register();
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

    private void commonSetup() {
        MudBrickDyeing.buildLookup();

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
    }
}
