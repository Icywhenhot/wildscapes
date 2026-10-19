package com.wildscapes.gametest;

import java.util.List;

import com.wildscapes.item.WitchCampMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("wildscapes")
@PrefixGameTestTemplate(false)
public final class WitchCampMapTests {
    private WitchCampMapTests() {}

    @GameTest(template = "gametest/tree_platform", batch = "witch_camp_map")
    public static void mapKeepsItsDestinationWhenSavedAndCopied(GameTestHelper helper) {
        var level = helper.getLevel();
        var camp = level.registryAccess().registryOrThrow(Registries.STRUCTURE).getHolderOrThrow(
                ResourceKey.create(Registries.STRUCTURE, ResourceLocation.parse("wildscapes:witch_camp")));
        helper.assertTrue(camp.is(WitchCampMap.DESTINATION), "witch camp missing from map destinations");
        ItemStack map = MapItem.create(level, 0, 0, (byte) 2, true, true);
        helper.assertTrue(!WitchCampMap.isWitchCampMap(map), "ordinary map has witch camp icon");
        MapItemSavedData.addTargetDecoration(map, BlockPos.ZERO, "+", WitchCampMap.WITCH_CAMP);
        ItemStack saved = ItemStack.parse(level.registryAccess(), map.save(level.registryAccess())).orElseThrow();
        helper.assertTrue(WitchCampMap.isWitchCampMap(saved), "saved map lost witch camp icon");
        var input = CraftingInput.of(2, 1, List.of(saved, new ItemStack(Items.MAP)));
        var recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level).orElseThrow();
        ItemStack copies = recipe.value().assemble(input, level.registryAccess());
        helper.assertTrue(copies.getCount() == 2 && WitchCampMap.isWitchCampMap(copies), "copied maps lost witch camp icon");
        helper.assertTrue(copies.get(DataComponents.MAP_DECORATIONS).equals(map.get(DataComponents.MAP_DECORATIONS)),
                "copied maps changed destination");
        helper.assertTrue(copies.get(DataComponents.MAP_ID).equals(map.get(DataComponents.MAP_ID)), "copied maps changed map id");
        helper.assertTrue(WitchCampMap.WITCH_CAMP.get().showOnItemFrame()
                && WitchCampMap.WITCH_CAMP.get().explorationMapElement(), "witch camp marker is not an explorer marker");
        helper.succeed();
    }
}
