package com.wildscapes.client;

import com.wildscapes.Wildscapes;
import com.wildscapes.effect.Intangibility;
import com.wildscapes.entity.JumpEnchantments;
import com.wildscapes.item.MirelashItem;
import com.wildscapes.item.WitchCampMap;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public final class WildscapesItemCondition {
    private WildscapesItemCondition() {}

    public static void register() {
        net.minecraft.client.renderer.item.ItemProperties.register(net.minecraft.world.item.Items.FILLED_MAP,
                Wildscapes.id("witch_camp"), (stack, level, owner, seed) -> WitchCampMap.isWitchCampMap(stack) ? 1F : 0F);
        net.minecraft.client.renderer.item.ItemProperties.register(com.wildscapes.item.WildscapesItems.MIRELASH,
                Wildscapes.id("active"), (stack, level, owner, seed) -> level != null && owner != null
                        && MirelashItem.isHookOut(level, owner) ? 1F : 0F);
        net.minecraft.client.renderer.item.ItemProperties.register(com.wildscapes.item.WildscapesItems.ILLUSIONIST_NECKLACE,
                Wildscapes.id("active"), (stack, level, owner, seed) -> owner instanceof Player player
                        && (player.getMainHandItem() == stack || player.getOffhandItem() == stack)
                        && Intangibility.isActive(player) ? 1F : 0F);
        for (var key : java.util.List.of(MirelashItem.ELASTICITY, MirelashItem.SLIME_SNARE,
                JumpEnchantments.MULTI_JUMP, JumpEnchantments.EMPOWERED_JUMP)) {
            net.minecraft.client.renderer.item.ItemProperties.register(net.minecraft.world.item.Items.ENCHANTED_BOOK,
                    key.location(), (stack, level, owner, seed) -> hasEnchantment(stack, key) ? 1F : 0F);
        }
    }

    private static boolean hasEnchantment(ItemStack stack, ResourceKey<Enchantment> key) {
        var enchantments = stack.get(DataComponents.STORED_ENCHANTMENTS);
        return enchantments != null && enchantments.keySet().stream().anyMatch(enchantment -> enchantment.is(key));
    }
}
