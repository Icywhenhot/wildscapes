package com.wildscapes.gametest;

import java.util.List;

import com.wildscapes.item.WitchCampMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public final class WitchCampMapTests {
    private WitchCampMapTests() {}

    public static void mapKeepsItsDestinationWhenSavedAndCopied(GameTestHelper helper) {
        var level = helper.getLevel();
        var camp = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(
                ResourceKey.create(Registries.STRUCTURE, Identifier.parse("wildscapes:witch_camp")));
        helper.assertTrue(camp.is(WitchCampMap.DESTINATION), "witch camp missing from map destinations");
        ItemStack map = MapItem.create(level, 0, 0, (byte) 2, true, true);
        helper.assertTrue(!WitchCampMap.isWitchCampMap(map), "ordinary map has witch camp icon");
        MapItemSavedData.addTargetDecoration(map, BlockPos.ZERO, "+", WitchCampMap.WITCH_CAMP);
        var ops = level.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE);
        ItemStack saved = ItemStack.CODEC.parse(ops, ItemStack.CODEC.encodeStart(ops, map).getOrThrow()).getOrThrow();
        helper.assertTrue(WitchCampMap.isWitchCampMap(saved), "saved map lost witch camp icon");
        var input = CraftingInput.of(2, 1, List.of(saved, new ItemStack(Items.MAP)));
        var recipe = level.recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, level).orElseThrow();
        ItemStack copies = recipe.value().assemble(input);
        helper.assertTrue(copies.getCount() == 2 && WitchCampMap.isWitchCampMap(copies), "copied maps lost witch camp icon");
        helper.assertTrue(copies.get(DataComponents.MAP_DECORATIONS).equals(map.get(DataComponents.MAP_DECORATIONS)),
                "copied maps changed destination");
        helper.assertTrue(copies.get(DataComponents.MAP_ID).equals(map.get(DataComponents.MAP_ID)), "copied maps changed map id");
        helper.assertTrue(WitchCampMap.WITCH_CAMP.value().showOnItemFrame()
                && WitchCampMap.WITCH_CAMP.value().explorationMapElement(), "witch camp marker is not an explorer marker");
        helper.succeed();
    }
}
