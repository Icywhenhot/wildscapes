package com.wildscapes.gametest;

import com.wildscapes.effect.WildscapesPotions;
import com.wildscapes.item.WildscapesItems;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("wildscapes")
@PrefixGameTestTemplate(false)
public final class GnashingTests {
    private GnashingTests() {}

    @GameTest(template = "gametest/tree_platform", batch = "gnashing")
    public static void potionFormsShareTheGreenTint(GameTestHelper helper) {
        for (var potion : java.util.List.of(WildscapesPotions.GNASHING,
                WildscapesPotions.LONG_GNASHING, WildscapesPotions.STRONG_GNASHING)) {
            for (var item : java.util.List.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION, Items.TIPPED_ARROW)) {
                ItemStack stack = PotionContents.createItemStack(item, potion);
                helper.assertTrue(stack.get(DataComponents.POTION_CONTENTS).getColor() == 0xFF354F2A,
                        "gnashing form lost its green tint");
            }
        }
        helper.assertTrue(WildscapesPotions.LONG_GNASHING.get().getEffects().getFirst().getDuration()
                > WildscapesPotions.GNASHING.get().getEffects().getFirst().getDuration(), "long gnashing did not last longer");
        helper.assertTrue(WildscapesPotions.STRONG_GNASHING.get().getEffects().getFirst().getAmplifier() == 1,
                "strong gnashing did not reach level two");
        helper.succeed();
    }

    @GameTest(template = "gametest/tree_platform", batch = "gnashing")
    public static void structuresAndSoulHarvestGiveCommonGnashingLoot(GameTestHelper helper) {
        for (String path : new String[] {"chests/witch_camp/standard", "chests/witch_camp/dungeon",
                "chests/village/swamp_house", "chests/village/swamp_armorer", "chests/village/swamp_cartographer",
                "chests/village/swamp_fisher", "chests/village/swamp_fletcher", "chests/village/swamp_mason",
                "chests/village/swamp_tannery", "chests/village/swamp_temple", "pots/witch_camp/cellar",
                "gameplay/cauldron_of_souls"}) {
            LootParams params = new LootParams.Builder(helper.getLevel())
                    .withParameter(LootContextParams.ORIGIN, Vec3.ZERO)
                    .create(path.startsWith("gameplay/") ? LootContextParamSets.VAULT : LootContextParamSets.CHEST);
            ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE,
                    ResourceLocation.fromNamespaceAndPath("wildscapes", path));
            LootTable table = helper.getLevel().getServer().reloadableRegistries().getLootTable(key);
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
                    var contents = stack.get(DataComponents.POTION_CONTENTS);
                    if (contents == null || contents.potion().isEmpty()) {
                        continue;
                    }
                    var potion = contents.potion().get();
                    if (!potion.is(WildscapesPotions.GNASHING.getKey())
                            && !potion.is(WildscapesPotions.LONG_GNASHING.getKey())
                            && !potion.is(WildscapesPotions.STRONG_GNASHING.getKey())) {
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
