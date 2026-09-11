package com.wildscapes.effect;

import com.wildscapes.item.WildscapesItems;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public final class Intangibility {
    private Intangibility() {}

    public static boolean isActive(Player player) {
        return player.isShiftKeyDown() && (canUse(player.getMainHandItem()) || canUse(player.getOffhandItem()));
    }

    private static boolean canUse(ItemStack stack) {
        return stack.is(WildscapesItems.ILLUSIONIST_NECKLACE) && stack.getDamageValue() < stack.getMaxDamage() - 1;
    }

    public static void onMobTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide()) {
            return;
        }
        if (mob.getTarget() instanceof Player player && isActive(player)) {
            mob.setTarget(null);
            mob.getNavigation().stop();
        }
        var brain = mob.getBrain();
        if (brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                && brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null) instanceof Player player
                && isActive(player)) {
            brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
            brain.eraseMemory(MemoryModuleType.WALK_TARGET);
            brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
            mob.getNavigation().stop();
        }
    }

    public static void onPlayerTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        boolean active = isActive(player);
        if (active && !player.level().isClientSide() && !player.getAbilities().instabuild) {
            var data = player.getPersistentData();
            int ticks = data.getInt("wildscapes:necklace_ticks") + 1;
            if (ticks >= 20) {
                EquipmentSlot slot = canUse(player.getMainHandItem()) ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                player.getItemBySlot(slot).hurtAndBreak(1, player, slot);
                ticks = 0;
                active = isActive(player);
            }
            data.putInt("wildscapes:necklace_ticks", ticks);
        }
        MobEffectInstance effect = player.getEffect(WildscapesEffects.INTANGIBILITY);
        if (active) {
            if (effect == null) {
                player.addEffect(new MobEffectInstance(WildscapesEffects.INTANGIBILITY,
                        MobEffectInstance.INFINITE_DURATION, 0, false, false, true));
            }
        } else if (effect != null) {
            player.removeEffect(WildscapesEffects.INTANGIBILITY);
        }
    }

    public static void onDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player player && isActive(player)
                && event.getSource().getEntity() != null) {
            event.setCanceled(true);
        }
    }
}
