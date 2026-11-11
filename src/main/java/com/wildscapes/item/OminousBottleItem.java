package com.wildscapes.item;

import java.util.List;

import com.wildscapes.sound.WildscapesSounds;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;

public class OminousBottleItem extends Item {
    public OminousBottleItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity drinker) {
        if (drinker instanceof ServerPlayer player) {
            CriteriaTriggers.CONSUME_ITEM.trigger(player, stack);
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        if (!level.isClientSide) {
            level.playSound(null, drinker.blockPosition(), WildscapesSounds.OMINOUS_BOTTLE_DISPOSE.get(),
                    drinker.getSoundSource(), 1.0F, 1.0F);
            drinker.addEffect(omen(stack));
        }
        if (!(drinker instanceof Player player) || !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        PotionUtils.addPotionTooltip(List.of(omen(stack)), tooltip, 1.0F);
    }

    private static MobEffectInstance omen(ItemStack stack) {
        int amplifier = stack.hasTag() ? Mth.clamp(stack.getTag().getInt("OminousBottleAmplifier"), 0, 4) : 0;
        return new MobEffectInstance(MobEffects.BAD_OMEN, 120000, amplifier, false, false, true);
    }
}
