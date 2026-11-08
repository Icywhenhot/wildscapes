package com.wildscapes.item;

import java.util.function.Function;

import com.wildscapes.Wildscapes;
import com.wildscapes.block.WildscapesBlocks;
import com.wildscapes.entity.WildscapesEntities;

import icy.lib.GuideBookItem;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.BoatDispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.level.block.DispenserBlock;

public final class WildscapesItems {
    private WildscapesItems() {}

    public static final Item SLIME_BANNER_PATTERN = bannerPattern("slime");
    public static final Item SPIRIT_BANNER_PATTERN = bannerPattern("spirit");

    public static final SmithingTemplateItem SPELL_ARMOR_TRIM_SMITHING_TEMPLATE =
            register("spell_armor_trim_smithing_template", SmithingTemplateItem::createArmorTrimTemplate);

    public static final SmithingTemplateItem VISCOSE_ARMOR_TRIM_SMITHING_TEMPLATE =
            register("viscose_armor_trim_smithing_template", SmithingTemplateItem::createArmorTrimTemplate);

    public static final Item ABOMINATION_SPAWN_EGG = register("abomination_spawn_egg",
            properties -> new SpawnEggItem(properties.spawnEgg(WildscapesEntities.ABOMINATION)));

    public static final Item ILLUSIONER_SPAWN_EGG = register("illusioner_spawn_egg",
            properties -> new SpawnEggItem(properties.spawnEgg(EntityTypes.ILLUSIONER)));

    public static final SignItem CYPRESS_SIGN = register("cypress_sign",
            properties -> new SignItem(WildscapesBlocks.CYPRESS_SIGN, WildscapesBlocks.CYPRESS_WALL_SIGN,
                    properties.stacksTo(16)));

    public static final HangingSignItem CYPRESS_HANGING_SIGN = register("cypress_hanging_sign",
            properties -> new HangingSignItem(WildscapesBlocks.CYPRESS_HANGING_SIGN,
                    WildscapesBlocks.CYPRESS_WALL_HANGING_SIGN, properties.stacksTo(16)));

    public static final BoatItem CYPRESS_BOAT = register("cypress_boat",
            properties -> new BoatItem(CypressBoat.BOAT, properties.stacksTo(1)));

    public static final BoatItem CYPRESS_CHEST_BOAT = register("cypress_chest_boat",
            properties -> new BoatItem(CypressBoat.CHEST_BOAT, properties.stacksTo(1)));

    public static final Item FROG_LEGS = register("frog_legs", Item::new);

    public static final Item FROG_LEG_STEW = register("frog_leg_stew",
            properties -> new Item(properties.stacksTo(1).usingConvertsTo(Items.BOWL)
                    .food(new FoodProperties.Builder().nutrition(5).saturationModifier(0.6F).build(),
                            Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(
                                    new MobEffectInstance(MobEffects.JUMP_BOOST, 400, 1))).build())));

    public static final Item LADLE = register("ladle", properties -> new Item(properties.durability(10)));
    public static final Item ABOMINATION_TONGUE = register("abomination_tongue", Item::new);

    public static final MirelashItem MIRELASH = register("mirelash",
            properties -> new MirelashItem(properties.durability(64).enchantable(12).repairable(ABOMINATION_TONGUE)
                    .attributes(ItemAttributeModifiers.builder()
                            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID,
                                    5.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                            .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID,
                                    -2.4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                            .build())));

    public static final Item ILLUSIONIST_NECKLACE =
            register("illusionist_necklace", properties -> new Item(properties.durability(432)));

    public static final IncendiaryBottleItem INCENDIARY_BOTTLE =
            register("incendiary_bottle", properties -> new IncendiaryBottleItem(properties.stacksTo(16)));

    public static final MagicSoupItem MAGIC_SOUP = register("magic_soup", MagicSoupItem::new);

    public static final GuideBookItem MERCHANTS_GUIDE = register("merchants_guide",
            properties -> new GuideBookItem(properties, Wildscapes.id("merchants_guide")));

    private static Item bannerPattern(String name) {
        var tag = TagKey.create(Registries.BANNER_PATTERN, Wildscapes.id("pattern_item/" + name));
        return register(name + "_banner_pattern", properties -> new Item(properties.stacksTo(1)
                .delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, context -> context.getOrThrow(tag))));
    }

    private static <T extends Item> T register(String name, Function<Item.Properties, T> factory) {
        var key = ResourceKey.create(Registries.ITEM, Wildscapes.id(name));
        return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(new Item.Properties().setId(key)));
    }

    public static void register() {
        CYPRESS_SIGN.registerBlocks(Item.BY_BLOCK, CYPRESS_SIGN);
        CYPRESS_HANGING_SIGN.registerBlocks(Item.BY_BLOCK, CYPRESS_HANGING_SIGN);
        DispenserBlock.registerBehavior(CYPRESS_BOAT, new BoatDispenseItemBehavior(CypressBoat.BOAT));
        DispenserBlock.registerBehavior(CYPRESS_CHEST_BOAT, new BoatDispenseItemBehavior(CypressBoat.CHEST_BOAT));
    }
}
