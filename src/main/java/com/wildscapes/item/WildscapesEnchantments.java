package com.wildscapes.item;

import com.wildscapes.Wildscapes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import com.wildscapes.registry.DeferredRegister;
import com.wildscapes.registry.RegistryObject;

public final class WildscapesEnchantments {
    private static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(Registries.ENCHANTMENT, Wildscapes.MODID);

    public static final RegistryObject<Enchantment> ELASTICITY = ENCHANTMENTS.register("elasticity",
            () -> new MirelashSpell(Enchantment.Rarity.RARE, 2,
                    EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND));
    public static final RegistryObject<Enchantment> SLIME_SNARE = ENCHANTMENTS.register("slime_snare",
            () -> new MirelashSpell(Enchantment.Rarity.VERY_RARE, 1,
                    EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND) {
                @Override
                public int getMinCost(int level) {
                    return 25;
                }

                @Override
                public int getMaxCost(int level) {
                    return 60;
                }
            });
    public static final RegistryObject<Enchantment> MULTI_JUMP = ENCHANTMENTS.register("multi_jump",
            () -> new Spell(Enchantment.Rarity.RARE, EnchantmentCategory.ARMOR_FEET, 2, true, EquipmentSlot.FEET));
    public static final RegistryObject<Enchantment> EMPOWERED_JUMP = ENCHANTMENTS.register("empowered_jump",
            () -> new Spell(Enchantment.Rarity.RARE, EnchantmentCategory.ARMOR_LEGS, 1, true, EquipmentSlot.LEGS));

    private WildscapesEnchantments() {}

    public static void register() {
        ENCHANTMENTS.register();
    }

    public static boolean onlyForMirelash(Enchantment enchantment) {
        return enchantment instanceof MirelashSpell;
    }

    private static class MirelashSpell extends Spell {
        MirelashSpell(Rarity rarity, int levels, EquipmentSlot... slots) {
            super(rarity, EnchantmentCategory.BREAKABLE, levels, false, slots);
        }

        @Override
        public boolean canEnchant(ItemStack stack) {
            return stack.is(WildscapesItems.MIRELASH.get());
        }
    }

    private static class Spell extends Enchantment {
        private final int levels;
        private final boolean treasure;

        Spell(Rarity rarity, EnchantmentCategory category, int levels, boolean treasure, EquipmentSlot... slots) {
            super(rarity, category, slots);
            this.levels = levels;
            this.treasure = treasure;
        }

        @Override
        public int getMaxLevel() {
            return levels;
        }

        @Override
        public int getMinCost(int level) {
            return 15 + (level - 1) * 20;
        }

        @Override
        public int getMaxCost(int level) {
            return 45 + (level - 1) * 20;
        }

        @Override
        public boolean isTreasureOnly() {
            return treasure;
        }

        @Override
        public boolean isTradeable() {
            return !treasure;
        }

        @Override
        public boolean isDiscoverable() {
            return !treasure;
        }
    }
}
