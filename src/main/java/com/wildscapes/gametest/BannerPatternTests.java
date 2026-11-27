package com.wildscapes.gametest;

import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.List;

import com.wildscapes.item.WildscapesItems;

import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;

public final class BannerPatternTests {
    private BannerPatternTests() {}

    @SuppressWarnings("removal")
    public static void patternsCraftAndWorkInTheLoomAndOnShields(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = helper.makeMockServerPlayerInLevel();
        var templates = List.of(WildscapesItems.SLIME_BANNER_PATTERN.get(), WildscapesItems.SPIRIT_BANNER_PATTERN.get());
        for (int i = 0; i < templates.size(); i++) {
            var item = templates.get(i);
            String name = i == 0 ? "slime" : "spirit";
            var key = ResourceKey.create(Registries.BANNER_PATTERN, new ResourceLocation("wildscapes", name));
            var input = TestRecipes.crafting(2, 1, List.of(new ItemStack(Items.PAPER),
                    new ItemStack(i == 0 ? Items.SLIME_BALL : Items.GHAST_TEAR)));
            var recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
            helper.assertTrue(recipe.isPresent(), "missing pattern recipe for " + name);
            helper.assertTrue(recipe.orElseThrow().assemble(input, helper.getLevel().registryAccess()).is(item), "wrong pattern item");
            for (DyeColor color : DyeColor.values()) {
                LoomMenu menu = new LoomMenu(0, player.getInventory());
                menu.getSlot(0).set(new ItemStack(Items.WHITE_BANNER));
                menu.getSlot(1).set(new ItemStack(net.minecraft.world.item.DyeItem.byColor(color)));
                menu.getSlot(2).set(new ItemStack(item));
                ItemStack banner = menu.getSlot(3).getItem().copy();
                helper.assertTrue(!banner.isEmpty(), "loom produced no banner for " + name);
                var layers = BannerBlockEntity.getItemPatterns(banner);
                String hash = BuiltInRegistries.BANNER_PATTERN.getOrThrow(key).getHashname();
                helper.assertTrue(layers != null && layers.size() == 1
                        && layers.getCompound(0).getString("Pattern").equals(hash)
                        && layers.getCompound(0).getInt("Color") == color.getId(), "wrong banner pattern or dye");
                menu.getSlot(3).onTake(player, banner);
                helper.assertTrue(menu.getSlot(2).getItem().is(item), "loom consumed the pattern item");
                var shieldInput = TestRecipes.crafting(2, 1, List.of(new ItemStack(Items.SHIELD), banner));
                var shieldRecipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, shieldInput, level);
                helper.assertTrue(shieldRecipe.isPresent(), "patterned banner could not decorate a shield");
                ItemStack shield = shieldRecipe.orElseThrow().assemble(shieldInput, helper.getLevel().registryAccess());
                helper.assertTrue(layers.equals(BannerBlockEntity.getItemPatterns(shield)), "shield lost its pattern");
            }
        }
        level.getServer().getPlayerList().remove(player);
        helper.succeed();
    }
}
