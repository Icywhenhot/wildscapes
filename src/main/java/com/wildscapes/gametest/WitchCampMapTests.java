package com.wildscapes.gametest;

import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.CompoundTag;
import java.util.List;

import com.wildscapes.item.WitchCampMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public final class WitchCampMapTests {
    private WitchCampMapTests() {}

    public static void mapKeepsItsDestinationWhenSavedAndCopied(GameTestHelper helper) {
        var level = helper.getLevel();
        var camp = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(
                ResourceKey.create(Registries.STRUCTURE, new ResourceLocation("wildscapes:witch_camp")));
        helper.assertTrue(camp.is(WitchCampMap.DESTINATION), "witch camp missing from map destinations");
        ItemStack map = MapItem.create(level, 0, 0, (byte) 2, true, true);
        helper.assertTrue(!WitchCampMap.isWitchCampMap(map), "ordinary map has witch camp icon");
        MapItemSavedData.addTargetDecoration(map, BlockPos.ZERO, "+", MapDecoration.Type.RED_X);
        map.getTag().getList("Decorations", Tag.TAG_COMPOUND).getCompound(0).putString("id", WitchCampMap.MARKER);
        ItemStack saved = ItemStack.of(map.save(new CompoundTag()));
        helper.assertTrue(WitchCampMap.isWitchCampMap(saved), "saved map lost witch camp icon");
        var input = TestRecipes.crafting(2, 1, List.of(saved, new ItemStack(Items.MAP)));
        var recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level).orElseThrow();
        ItemStack copies = recipe.assemble(input, helper.getLevel().registryAccess());
        helper.assertTrue(copies.getCount() == 2 && WitchCampMap.isWitchCampMap(copies), "copied maps lost witch camp icon");
        helper.assertTrue(copies.getTag().getList("Decorations", Tag.TAG_COMPOUND)
                .equals(map.getTag().getList("Decorations", Tag.TAG_COMPOUND)), "copied maps changed destination");
        helper.assertTrue(java.util.Objects.equals(MapItem.getMapId(copies), MapItem.getMapId(map)), "copied maps changed map id");
        helper.succeed();
    }
}
