package com.wildscapes.gametest;

import java.util.List;

import com.wildscapes.item.WildscapesItems;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("wildscapes")
@PrefixGameTestTemplate(false)
public final class BannerPatternTests {
    private BannerPatternTests() {}

    @SuppressWarnings("removal")
    @GameTest(template = "gametest/tree_platform", batch = "banner_patterns")
    public static void patternsCraftAndWorkInTheLoomAndOnShields(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = helper.makeMockServerPlayerInLevel();
        var templates = List.of(WildscapesItems.SLIME_BANNER_PATTERN, WildscapesItems.SPIRIT_BANNER_PATTERN);
        for (int i = 0; i < templates.size(); i++) {
            var item = templates.get(i).get();
            String name = i == 0 ? "slime" : "spirit";
            var key = ResourceKey.create(Registries.BANNER_PATTERN, ResourceLocation.fromNamespaceAndPath("wildscapes", name));
            var input = CraftingInput.of(2, 1, List.of(new ItemStack(Items.PAPER),
                    new ItemStack(i == 0 ? Items.SLIME_BALL : Items.GHAST_TEAR)));
            var recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
            helper.assertTrue(recipe.isPresent(), "missing pattern recipe for " + name);
            helper.assertTrue(recipe.orElseThrow().value().assemble(input, level.registryAccess()).is(item), "wrong pattern item");
            for (DyeColor color : DyeColor.values()) {
                LoomMenu menu = new LoomMenu(0, player.getInventory());
                menu.getSlot(0).set(new ItemStack(Items.WHITE_BANNER));
                menu.getSlot(1).set(new ItemStack(DyeItem.byColor(color)));
                menu.getSlot(2).set(new ItemStack(item));
                ItemStack banner = menu.getSlot(3).getItem().copy();
                helper.assertTrue(!banner.isEmpty(), "loom produced no banner for " + name);
                var layers = banner.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY).layers();
                helper.assertTrue(layers.size() == 1 && layers.getFirst().pattern().is(key)
                        && layers.getFirst().color() == color, "wrong banner pattern or dye");
                menu.getSlot(3).onTake(player, banner);
                helper.assertTrue(menu.getSlot(2).getItem().is(item), "loom consumed the pattern item");
                var shieldInput = CraftingInput.of(2, 1, List.of(new ItemStack(Items.SHIELD), banner));
                var shieldRecipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, shieldInput, level);
                helper.assertTrue(shieldRecipe.isPresent(), "patterned banner could not decorate a shield");
                ItemStack shield = shieldRecipe.orElseThrow().value().assemble(shieldInput, level.registryAccess());
                helper.assertTrue(shield.get(DataComponents.BANNER_PATTERNS).layers().equals(layers), "shield lost its pattern");
            }
        }
        level.getServer().getPlayerList().remove(player);
        helper.succeed();
    }
}
