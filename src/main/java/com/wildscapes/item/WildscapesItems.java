package com.wildscapes.item;

import com.wildscapes.Wildscapes;
import com.wildscapes.block.WildscapesBlocks;
import com.wildscapes.entity.WildscapesEntities;

import icy.lib.GuideBookItem;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BannerPatternItem;
import net.minecraft.world.item.BowlFoodItem;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.SignItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class WildscapesItems {
    private WildscapesItems() {}

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, Wildscapes.MODID);

    public static final RegistryObject<BannerPatternItem> SLIME_BANNER_PATTERN = ITEMS.register("slime_banner_pattern",
            () -> new BannerPatternItem(TagKey.create(Registries.BANNER_PATTERN,
                    ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "pattern_item/slime")), new Item.Properties().stacksTo(1)));

    public static final RegistryObject<BannerPatternItem> SPIRIT_BANNER_PATTERN = ITEMS.register("spirit_banner_pattern",
            () -> new BannerPatternItem(TagKey.create(Registries.BANNER_PATTERN,
                    ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "pattern_item/spirit")), new Item.Properties().stacksTo(1)));

    public static final RegistryObject<SmithingTemplateItem> SPELL_ARMOR_TRIM_SMITHING_TEMPLATE = ITEMS.register(
            "spell_armor_trim_smithing_template", () -> SmithingTemplateItem.createArmorTrimTemplate(
                    ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "spell")));

    public static final RegistryObject<SmithingTemplateItem> VISCOSE_ARMOR_TRIM_SMITHING_TEMPLATE = ITEMS.register(
            "viscose_armor_trim_smithing_template", () -> SmithingTemplateItem.createArmorTrimTemplate(
                    ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "viscose")));

    public static final RegistryObject<Item> ABOMINATION_SPAWN_EGG = ITEMS.register("abomination_spawn_egg",
            () -> new ForgeSpawnEggItem(WildscapesEntities.ABOMINATION, 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final RegistryObject<Item> BOGGED_SPAWN_EGG = ITEMS.register("bogged_spawn_egg",
            () -> new ForgeSpawnEggItem(WildscapesEntities.BOGGED, 9084018, 3231003, new Item.Properties()));

    public static final RegistryObject<Item> ILLUSIONER_SPAWN_EGG = ITEMS.register("illusioner_spawn_egg",
            () -> new ForgeSpawnEggItem(() -> EntityType.ILLUSIONER, 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final RegistryObject<SignItem> CYPRESS_SIGN = ITEMS.register("cypress_sign",
            () -> new SignItem(new Item.Properties().stacksTo(16),
                    WildscapesBlocks.CYPRESS_SIGN.get(), WildscapesBlocks.CYPRESS_WALL_SIGN.get()));

    public static final RegistryObject<HangingSignItem> CYPRESS_HANGING_SIGN = ITEMS.register("cypress_hanging_sign",
            () -> new HangingSignItem(WildscapesBlocks.CYPRESS_HANGING_SIGN.get(),
                    WildscapesBlocks.CYPRESS_WALL_HANGING_SIGN.get(), new Item.Properties().stacksTo(16)));

    public static final RegistryObject<CypressBoatItem> CYPRESS_BOAT = ITEMS.register("cypress_boat",
            () -> new CypressBoatItem(false, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<CypressBoatItem> CYPRESS_CHEST_BOAT = ITEMS.register("cypress_chest_boat",
            () -> new CypressBoatItem(true, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> FROG_LEGS = ITEMS.register("frog_legs",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> FROG_LEG_STEW = ITEMS.register("frog_leg_stew",
            () -> new BowlFoodItem(new Item.Properties().stacksTo(1).food(new FoodProperties.Builder()
                    .nutrition(5)
                    .saturationMod(0.6F)
                    .effect(() -> new MobEffectInstance(MobEffects.JUMP, 400, 1), 1.0F)
                    .build())));

    public static final RegistryObject<Item> LADLE = ITEMS.register("ladle",
            () -> new Item(new Item.Properties().durability(10)));

    public static final RegistryObject<Item> ABOMINATION_TONGUE = ITEMS.register("abomination_tongue",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<MirelashItem> MIRELASH = ITEMS.register("mirelash",
            () -> new MirelashItem(new Item.Properties().durability(64)));

    public static final RegistryObject<Item> ILLUSIONIST_NECKLACE = ITEMS.register("illusionist_necklace",
            () -> new Item(new Item.Properties().durability(432)));

    public static final RegistryObject<IncendiaryBottleItem> INCENDIARY_BOTTLE = ITEMS.register("incendiary_bottle",
            () -> new IncendiaryBottleItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<MagicSoupItem> MAGIC_SOUP = ITEMS.register("magic_soup",
            () -> new MagicSoupItem(new Item.Properties()));

    public static final RegistryObject<GuideBookItem> MERCHANTS_GUIDE = ITEMS.register("merchants_guide",
            () -> new GuideBookItem(new Item.Properties(),
                    ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "merchants_guide")));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
