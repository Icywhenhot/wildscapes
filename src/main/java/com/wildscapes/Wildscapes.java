package com.wildscapes;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
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

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

@Mod(Wildscapes.MODID)
public class Wildscapes {
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
                        output.accept(WildscapesBlocks.CYPRESS_SHELF);
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

    public Wildscapes(IEventBus modBus) {
        WildscapesConfig.load();
        modBus.addListener(Wildscapes::registerAll);
        modBus.addListener(FMLCommonSetupEvent.class, event -> event.enqueueWork(Wildscapes::commonSetup));
        modBus.addListener(EntityAttributeCreationEvent.class,
                event -> event.put(WildscapesEntities.ABOMINATION, AbominationEntity.createAttributes().build()));
        modBus.addListener(RegisterPayloadHandlersEvent.class, event -> {
            MirelashAttackPayload.register(event);
            JumpPayload.register(event);
        });
        modBus.addListener(BlockEntityTypeAddBlocksEvent.class, WildscapesBlocks::addSignBlocks);
        modBus.addListener(ModifyDefaultComponentsEvent.class, WildscapesBlocks::compostables);
        modBus.addListener(ModifyDefaultComponentsEvent.class, VanillaTweaks::modifyComponents);

        NeoForge.EVENT_BUS.addListener(EntityJoinLevelEvent.class, event -> {
            if (event.getLevel() instanceof ServerLevel level) {
                IllusionerGoals.onJoinLevel(event.getEntity(), level);
                WitchGoals.onJoinLevel(event.getEntity(), level);
                SwampVariants.onEntityJoinLevel(event.getEntity(), level);
            }
        });
        NeoForge.EVENT_BUS.addListener(PlayerEvent.StartTracking.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                MirageSync.onStartTracking(event.getTarget(), player);
            }
        });
        NeoForge.EVENT_BUS.addListener(LivingIncomingDamageEvent.class, event -> {
            if (!Intangibility.allowDamage(event.getEntity(), event.getSource(), event.getAmount())) {
                event.setCanceled(true);
            }
        });
        NeoForge.EVENT_BUS.addListener(LivingDeathEvent.class,
                event -> SoulHarvest.onDeath(event.getEntity(), event.getSource()));
        NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.RightClickBlock.class, Wildscapes::onRightClickBlock);
        WitchRitual.register();
    }

    private static void registerAll(RegisterEvent event) {
        if (!event.getRegistryKey().equals(Registries.BLOCK)) {
            return;
        }
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
        SoulHarvest.registerAttachments();
        SwampVariants.registerAttachments();
        WitchRitual.registerAttachments();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id("wildscapes"), WILDSCAPES_TAB);
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        for (UseBlockHandler handler : new UseBlockHandler[] {
                MudBrickDyeing::onRightClick, CauldronSwap::onRightClick, MossCarpetSwap::onBonemeal}) {
            InteractionResult result = handler.use(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
            if (result != InteractionResult.PASS) {
                event.setCanceled(true);
                event.setCancellationResult(result);
                return;
            }
        }
    }

    private interface UseBlockHandler {
        InteractionResult use(Player player, Level level, InteractionHand hand, BlockHitResult hit);
    }

    private static void commonSetup() {
        MudBrickDyeing.buildLookup();
    }
}
