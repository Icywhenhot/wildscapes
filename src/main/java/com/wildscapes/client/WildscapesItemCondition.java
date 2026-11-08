package com.wildscapes.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.wildscapes.Wildscapes;
import com.wildscapes.effect.Intangibility;
import com.wildscapes.entity.JumpEnchantments;
import com.wildscapes.item.MirelashItem;
import com.wildscapes.item.WitchCampMap;
import com.wildscapes.mixin.client.ItemConditionsAccessor;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public record WildscapesItemCondition(String kind) implements ConditionalItemModelProperty {
    private static final MapCodec<WildscapesItemCondition> CODEC =
            Codec.STRING.fieldOf("kind").xmap(WildscapesItemCondition::new, WildscapesItemCondition::kind);

    public static void register() {
        ItemConditionsAccessor.wildscapes$conditions().put(Wildscapes.id("item_condition"), CODEC);
    }

    @Override
    public MapCodec<WildscapesItemCondition> type() {
        return CODEC;
    }

    @Override
    public boolean get(ItemStack stack, ClientLevel level, LivingEntity owner, int seed, ItemDisplayContext display) {
        return switch (kind) {
            case "witch_camp" -> WitchCampMap.isWitchCampMap(stack);
            case "mirelash" -> level != null && owner != null && MirelashItem.isHookOut(level, owner);
            case "necklace" -> owner instanceof Player player
                    && (player.getMainHandItem() == stack || player.getOffhandItem() == stack)
                    && Intangibility.isActive(player);
            case "elasticity" -> hasEnchantment(stack, MirelashItem.ELASTICITY);
            case "slime_snare" -> hasEnchantment(stack, MirelashItem.SLIME_SNARE);
            case "multi_jump" -> hasEnchantment(stack, JumpEnchantments.MULTI_JUMP);
            case "empowered_jump" -> hasEnchantment(stack, JumpEnchantments.EMPOWERED_JUMP);
            default -> false;
        };
    }

    private static boolean hasEnchantment(ItemStack stack, ResourceKey<Enchantment> key) {
        var enchantments = stack.get(DataComponents.STORED_ENCHANTMENTS);
        return enchantments != null && enchantments.keySet().stream().anyMatch(enchantment -> enchantment.is(key));
    }
}
