package com.wildscapes.item;

import java.util.List;
import java.util.Optional;

import com.wildscapes.block.CauldronSoups;

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

public class MagicSoupItem extends Item {
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

        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide && soup != null) {
            for (MobEffectInstance effect : soup.effects()) {
                entity.addEffect(stackOnto(entity.getEffect(effect.getEffect()), effect));
            }
        }
        return result;
    }

    private static MobEffectInstance stackOnto(MobEffectInstance current, MobEffectInstance added) {
        if (current == null || added.getEffect().value().isInstantenous()) {
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
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        SoupContents soup = stack.get(WildscapesDataComponents.SOUP_CONTENTS.get());
        if (soup == null || soup.isEmpty()) {
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
        if (!effect.getEffect().value().isInstantenous()) {
            name = Component.translatable("potion.withDuration", name,
                    net.minecraft.util.StringUtil.formatTickDuration(effect.getDuration(), 20.0F));
        }
        ChatFormatting color = effect.getEffect().value().getCategory().getTooltipFormatting();
        return name.copy().withStyle(color);
    }
}
