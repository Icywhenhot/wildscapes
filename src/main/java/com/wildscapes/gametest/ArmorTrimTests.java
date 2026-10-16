package com.wildscapes.gametest;

import java.util.List;

import com.wildscapes.item.WildscapesItems;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("wildscapes")
@PrefixGameTestTemplate(false)
public final class ArmorTrimTests {
    private ArmorTrimTests() {}

    @GameTest(template = "gametest/tree_platform", batch = "armor_trims")
    public static void templatesApplyBothPatternsAndDuplicate(GameTestHelper helper) {
        var level = helper.getLevel();
        var recipes = level.getRecipeManager();
        var templates = List.of(WildscapesItems.SPELL_ARMOR_TRIM_SMITHING_TEMPLATE,
                WildscapesItems.VISCOSE_ARMOR_TRIM_SMITHING_TEMPLATE);
        for (int i = 0; i < templates.size(); i++) {
            var template = templates.get(i).get();
            String name = i == 0 ? "spell" : "viscose";
            var pattern = ResourceKey.create(Registries.TRIM_PATTERN,
                    ResourceLocation.fromNamespaceAndPath("wildscapes", name));
            for (var armor : List.of(Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS)) {
                for (var material : List.of(Items.QUARTZ, Items.IRON_INGOT, Items.GOLD_INGOT, Items.DIAMOND,
                        Items.NETHERITE_INGOT, Items.REDSTONE, Items.COPPER_INGOT, Items.EMERALD, Items.LAPIS_LAZULI, Items.AMETHYST_SHARD)) {
                    ItemStack base = new ItemStack(armor);
                    base.setDamageValue(12);
                    var input = new SmithingRecipeInput(new ItemStack(template), base, new ItemStack(material));
                    var recipe = recipes.getRecipeFor(RecipeType.SMITHING, input, level);
                    helper.assertTrue(recipe.isPresent(), "missing smithing recipe for " + name);
                    ItemStack result = recipe.orElseThrow().value().assemble(input, level.registryAccess());
                    var trim = result.get(DataComponents.TRIM);
                    helper.assertTrue(result.is(armor) && result.getDamageValue() == 12, "smithing changed the armor");
                    helper.assertTrue(trim != null && trim.pattern().is(pattern), "wrong trim pattern for " + name);
                    helper.assertTrue(trim.material().value().ingredient().value() == material, "wrong trim material");
                    helper.assertTrue(!base.has(DataComponents.TRIM), "smithing changed the input armor");
                }
            }
            ItemStack diamond = new ItemStack(Items.DIAMOND);
            var input = CraftingInput.of(3, 3, List.of(diamond, new ItemStack(template), diamond,
                    diamond, new ItemStack(i == 0 ? Items.AMETHYST_BLOCK : Items.SLIME_BLOCK), diamond,
                    diamond, diamond, diamond));
            var recipe = recipes.getRecipeFor(RecipeType.CRAFTING, input, level);
            helper.assertTrue(recipe.isPresent(), "missing duplication recipe for " + name);
            ItemStack result = recipe.orElseThrow().value().assemble(input, level.registryAccess());
            helper.assertTrue(result.is(template) && result.getCount() == 2, "template did not duplicate");
        }
        helper.succeed();
    }

    @GameTest(template = "gametest/tree_platform", batch = "armor_trims")
    public static void harvestAwardsBothTrimsAndCampsKeepThemRare(GameTestHelper helper) {
        for (String path : new String[] {"gameplay/cauldron_of_souls", "chests/witch_camp/standard",
                "chests/witch_camp/dungeon", "pots/witch_camp/cellar"}) {
            boolean harvest = path.startsWith("gameplay/");
            var params = new LootParams.Builder(helper.getLevel())
                    .withParameter(LootContextParams.ORIGIN, Vec3.ZERO)
                    .create(harvest ? LootContextParamSets.VAULT : LootContextParamSets.CHEST);
            var key = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("wildscapes", path));
            LootTable table = helper.getLevel().getServer().reloadableRegistries().getLootTable(key);
            int spell = 0;
            int viscose = 0;
            for (int i = 0; i < 1024; i++) {
                for (ItemStack stack : table.getRandomItems(params)) {
                    if (stack.is(WildscapesItems.SPELL_ARMOR_TRIM_SMITHING_TEMPLATE.get())) {
                        spell++;
                    } else if (stack.is(WildscapesItems.VISCOSE_ARMOR_TRIM_SMITHING_TEMPLATE.get())) {
                        viscose++;
                    }
                }
            }
            helper.assertTrue(spell > 0 && viscose > 0, "a custom trim was missing in " + path);
            helper.assertTrue(harvest ? spell + viscose > 128 : spell + viscose < 64,
                    "wrong trim rarity in " + path + ": " + (spell + viscose));
        }
        helper.succeed();
    }
}
