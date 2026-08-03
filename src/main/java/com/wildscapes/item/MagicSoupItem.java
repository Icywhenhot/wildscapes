package com.wildscapes.item;

import java.util.List;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * A bowl of magic soup scooped from a boiling cauldron. It is eaten almost instantly and,
 * on top of a little hunger, grants every effect the soup was brewed with — but each effect
 * is short-lived (see {@link com.wildscapes.block.CauldronSoups}). The effects ride along in
 * a {@link SoupContents} component, so soups with different effects never stack together.
 */
public class MagicSoupItem extends Item {

    /** Eats in ~0.4 s — faster than any vanilla food. */
    private static final float EAT_SECONDS = 0.4F;

    public MagicSoupItem(Properties properties) {
        super(properties
                .stacksTo(8)
                .food(new FoodProperties(4, 0.4F, true, EAT_SECONDS,
                        Optional.of(new ItemStack(Items.BOWL)), List.of())));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        SoupContents soup = stack.get(WildscapesDataComponents.SOUP_CONTENTS.get());
        // super applies hunger/saturation and converts the bowl back; grab the effects first
        // because the stack it returns may be the empty-handed bowl.
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide && soup != null) {
            for (MobEffectInstance effect : soup.effects()) {
                entity.addEffect(new MobEffectInstance(effect));
            }
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        SoupContents soup = stack.get(WildscapesDataComponents.SOUP_CONTENTS.get());
        if (soup == null || soup.isEmpty()) {
            tooltip.add(Component.translatable("item.wildscapes.magic_soup.empty").withStyle(ChatFormatting.GRAY));
            return;
        }
        for (MobEffectInstance effect : soup.effects()) {
            tooltip.add(describe(effect));
        }
    }

    private static Component describe(MobEffectInstance effect) {
        Component name = Component.translatable(effect.getDescriptionId());
        if (effect.getAmplifier() > 0) {
            name = Component.translatable("potion.withAmplifier", name,
                    Component.translatable("potion.potency." + effect.getAmplifier()));
        }
        if (!effect.getEffect().value().isInstantenous()) {
            name = Component.translatable("potion.withDuration", name,
                    net.minecraft.util.StringUtil.formatTickDuration(effect.getDuration(), 20.0F));
        }
        ChatFormatting color = effect.getEffect().value().getCategory().getTooltipFormatting();
        return name.copy().withStyle(color);
    }
}
