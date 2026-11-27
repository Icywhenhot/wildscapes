package com.wildscapes.item;

import java.util.List;

import com.wildscapes.block.CauldronSoups;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BowlFoodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class MagicSoupItem extends BowlFoodItem {
    public MagicSoupItem(Properties properties) {
        super(properties
                .stacksTo(8)
                .food(new FoodProperties.Builder().nutrition(4).saturationMod(0.05F).alwaysEat().build()));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        SoupContents soup = SoupContents.read(stack);

        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide) {
            for (MobEffectInstance effect : soup.effects()) {
                entity.addEffect(stackOnto(entity.getEffect(effect.getEffect()), effect));
            }
        }
        return result;
    }

    private static MobEffectInstance stackOnto(MobEffectInstance current, MobEffectInstance added) {
        if (current == null || added.getEffect().isInstantenous()) {
            return new MobEffectInstance(added);
        }

        int amplifier = Math.max(current.getAmplifier(),
                Math.min(current.getAmplifier() + added.getAmplifier() + 1,
                        CauldronSoups.maxAmplifier(added.getEffect())));
        int duration = Math.max(current.getDuration(), added.getDuration());
        return new MobEffectInstance(added.getEffect(), duration, amplifier,
                added.isAmbient(), added.isVisible(), added.showIcon());
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        SoupContents soup = SoupContents.read(stack);
        if (soup.isEmpty()) {
            tooltip.add(Component.translatable("item.wildscapes.magic_soup.empty").withStyle(ChatFormatting.GRAY));
            return;
        }
        for (MobEffectInstance effect : soup.effects()) {
            tooltip.add(describe(effect));
        }
        tooltip.add(Component.translatable("item.wildscapes.magic_soup.stacks").withStyle(ChatFormatting.DARK_GRAY));
    }

    private static Component describe(MobEffectInstance effect) {
        Component name = Component.translatable(effect.getDescriptionId());
        if (effect.getAmplifier() > 0) {
            name = Component.translatable("potion.withAmplifier", name,
                    Component.translatable("potion.potency." + effect.getAmplifier()));
        }
        if (!effect.getEffect().isInstantenous()) {
            name = Component.translatable("potion.withDuration", name,
                    net.minecraft.util.StringUtil.formatTickDuration(effect.getDuration()));
        }
        ChatFormatting color = effect.getEffect().getCategory().getTooltipFormatting();
        return name.copy().withStyle(color);
    }
}
