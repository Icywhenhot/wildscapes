package com.wildscapes.gametest;

import net.minecraft.world.item.alchemy.PotionUtils;
import com.wildscapes.effect.WildscapesPotions;
import com.wildscapes.item.WildscapesItems;

import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public final class GnashingTests {
    private GnashingTests() {}

    public static void potionFormsShareTheGreenTint(GameTestHelper helper) {
        for (var potion : java.util.List.of(WildscapesPotions.GNASHING.get(),
                WildscapesPotions.LONG_GNASHING.get(), WildscapesPotions.STRONG_GNASHING.get())) {
            for (var item : java.util.List.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION, Items.TIPPED_ARROW)) {
                ItemStack stack = PotionUtils.setPotion(new ItemStack(item), potion);
                helper.assertTrue((PotionUtils.getColor(stack) & 0xFFFFFF) == 0x354F2A,
                        "gnashing form lost its green tint");
            }
        }
        helper.assertTrue(WildscapesPotions.LONG_GNASHING.get().getEffects().get(0).getDuration()
                > WildscapesPotions.GNASHING.get().getEffects().get(0).getDuration(), "long gnashing did not last longer");
        helper.assertTrue(WildscapesPotions.STRONG_GNASHING.get().getEffects().get(0).getAmplifier() == 1,
                "strong gnashing did not reach level two");
        helper.succeed();
    }

    public static void structuresAndSoulHarvestGiveCommonGnashingLoot(GameTestHelper helper) {
        for (String path : new String[] {"chests/witch_camp/standard", "chests/witch_camp/dungeon",
                "chests/village/swamp_house", "chests/village/swamp_armorer", "chests/village/swamp_cartographer",
                "chests/village/swamp_fisher", "chests/village/swamp_fletcher", "chests/village/swamp_mason",
                "chests/village/swamp_tannery", "chests/village/swamp_temple", "pots/witch_camp/cellar",
                "gameplay/cauldron_of_souls"}) {
            LootParams params = new LootParams.Builder(helper.getLevel())
                    .withParameter(LootContextParams.ORIGIN, Vec3.ZERO)
                    .create(LootContextParamSets.CHEST);
            LootTable table = helper.getLevel().getServer().getLootData()
                    .getLootTable(new ResourceLocation("wildscapes", path));
            int found = 0;
            int splashes = 0;
            int arrows = 0;
            int bottles = 0;
            for (int i = 0; i < 128; i++) {
                boolean hit = false;
                for (ItemStack stack : table.getRandomItems(params)) {
                    if (stack.is(WildscapesItems.INCENDIARY_BOTTLE.get())) {
                        helper.assertTrue(stack.getCount() >= 1 && stack.getCount() <= 4, "wrong incendiary bottle count");
                        bottles++;
                    }
                    var potion = PotionUtils.getPotion(stack);
                    if (potion != WildscapesPotions.GNASHING.get()
                            && potion != WildscapesPotions.LONG_GNASHING.get()
                            && potion != WildscapesPotions.STRONG_GNASHING.get()) {
                        continue;
                    }
                    helper.assertTrue(stack.is(Items.SPLASH_POTION) || stack.is(Items.TIPPED_ARROW),
                            "gnashing loot had the wrong form in " + path);
                    if (stack.is(Items.TIPPED_ARROW)) {
                        helper.assertTrue(stack.getCount() >= 4 && stack.getCount() <= 12, "wrong gnashing arrow count");
                        arrows++;
                    } else {
                        splashes++;
                    }
                    hit = true;
                }
                if (hit) {
                    found++;
                }
            }
            helper.assertTrue(found > 64 && found < 128, "gnashing was missing or not common in " + path + ": " + found);
            helper.assertTrue(splashes > 0 && arrows > 0, "a gnashing loot form was missing in " + path);
            helper.assertTrue(bottles > (path.startsWith("pots/") ? 8 : 48), "incendiary bottles were not common in " + path);
        }
        helper.succeed();
    }
}
