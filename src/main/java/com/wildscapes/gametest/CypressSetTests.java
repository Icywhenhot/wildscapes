package com.wildscapes.gametest;

import com.wildscapes.entity.WildscapesEntities;
import java.util.List;

import com.wildscapes.block.WildscapesBlocks;
import com.wildscapes.item.WildscapesItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.GameType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import com.wildscapes.block.entity.CypressShelfBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;

public final class CypressSetTests {
    private CypressSetTests() {}

    public static void cypressSetAndPolishHoldTogether(GameTestHelper helper) {
        signsHoldSignBlockEntities(helper);
        shelfActsLikeAWoodenShelf(helper);
        boatsUseTheCypressType(helper);
        raidBlocksAreUnbreakable(helper);
        mirelashTakesMending(helper);
        swampPlantsCompost(helper);
        frogLegStew(helper);
        helper.succeed();
    }

    private static void frogLegStew(GameTestHelper helper) {
        var level = helper.getLevel();
        var input = TestRecipes.crafting(3, 2, List.of(new ItemStack(Items.POTATO),
                new ItemStack(WildscapesItems.FROG_LEGS.get()), new ItemStack(Items.BOWL),
                new ItemStack(WildscapesBlocks.LUMINOUS_MUSHROOM.get()), ItemStack.EMPTY, new ItemStack(Items.CARROT)));
        var recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
        helper.assertTrue(recipe.isPresent(), "frog leg stew recipe missing");
        var stew = recipe.get().assemble(input, helper.getLevel().registryAccess());
        helper.assertTrue(stew.is(WildscapesItems.FROG_LEG_STEW.get()), "wrong stew recipe result");
        var player = helper.makeMockSurvivalPlayer();
        player.getFoodData().setFoodLevel(10);
        var bowl = stew.finishUsingItem(level, player);
        helper.assertTrue(bowl.is(Items.BOWL), "stew did not return a bowl");
        var effect = player.getEffect(MobEffects.JUMP);
        helper.assertTrue(effect != null && effect.getAmplifier() == 1 && effect.getDuration() == 400,
                "stew did not give jump boost ii for 20 seconds");
        var legs = TestRecipes.slots(new ItemStack(WildscapesItems.FROG_LEGS.get()));
        for (var type : List.of(RecipeType.SMELTING, RecipeType.SMOKING, RecipeType.CAMPFIRE_COOKING)) {
            helper.assertTrue(level.getRecipeManager().getRecipeFor(type, legs, level).isEmpty(),
                    "frog legs still cook in " + type);
        }
    }

    private static void signsHoldSignBlockEntities(GameTestHelper helper) {
        BlockPos sign = new BlockPos(1, 2, 1);
        BlockPos hanging = new BlockPos(3, 2, 1);
        helper.setBlock(sign, WildscapesBlocks.CYPRESS_SIGN.get());
        helper.setBlock(hanging, WildscapesBlocks.CYPRESS_HANGING_SIGN.get());
        helper.assertTrue(helper.getBlockEntity(sign) instanceof SignBlockEntity, "cypress sign has no sign entity");
        helper.assertTrue(helper.getBlockEntity(hanging) instanceof HangingSignBlockEntity,
                "cypress hanging sign has no hanging sign entity");
    }

    private static void shelfActsLikeAWoodenShelf(GameTestHelper helper) {
        BlockPos shelf = new BlockPos(2, 2, 1);
        helper.setBlock(shelf, WildscapesBlocks.CYPRESS_SHELF.get());
        helper.assertTrue(helper.getBlockEntity(shelf) instanceof CypressShelfBlockEntity, "cypress shelf has no shelf entity");
        BlockState state = WildscapesBlocks.CYPRESS_SHELF.get().defaultBlockState();
        helper.assertTrue(state.is(WildscapesBlocks.WOODEN_SHELVES) && state.is(BlockTags.MINEABLE_WITH_AXE),
                "cypress shelf missing from wooden shelves or axe tags");
        var level = helper.getLevel();
        var log = new ItemStack(WildscapesBlocks.STRIPPED_CYPRESS_LOG.get());
        var input = TestRecipes.crafting(3, 3, List.of(log, log, log, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, log, log, log));
        var recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
        helper.assertTrue(recipe.isPresent() && recipe.get().assemble(input, helper.getLevel().registryAccess())
                .is(WildscapesBlocks.CYPRESS_SHELF.get().asItem()), "cypress shelf recipe missing");
    }

    private static void boatsUseTheCypressType(GameTestHelper helper) {
        var level = helper.getLevel();
        Boat boat = WildscapesEntities.CYPRESS_BOAT.get().create(level);
        helper.assertTrue(boat != null && boat.getType() == WildscapesEntities.CYPRESS_BOAT.get(), "wrong cypress boat entity type");
        var input = TestRecipes.crafting(2, 1, List.of(new ItemStack(Items.CHEST), new ItemStack(WildscapesItems.CYPRESS_BOAT.get())));
        var recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
        helper.assertTrue(recipe.isPresent() && recipe.get().assemble(input, helper.getLevel().registryAccess())
                .is(WildscapesItems.CYPRESS_CHEST_BOAT.get()), "chest boat recipe missing");
    }

    private static void raidBlocksAreUnbreakable(GameTestHelper helper) {
        for (BlockState state : List.of(WildscapesBlocks.CAULDRON_OF_SOULS.get().defaultBlockState(),
                WildscapesBlocks.SUMMONING_BONFIRE.get().defaultBlockState())) {
            helper.assertTrue(state.getDestroySpeed(helper.getLevel(), BlockPos.ZERO) < 0, state + " can be mined");
        }
    }

    private static void mirelashTakesMending(GameTestHelper helper) {
        helper.assertTrue(Enchantments.MENDING.canEnchant(new ItemStack(WildscapesItems.MIRELASH.get())),
                "mirelash refuses mending");
    }

    private static void swampPlantsCompost(GameTestHelper helper) {
        for (var item : List.of(WildscapesBlocks.DUCKWEED.get().asItem(), WildscapesBlocks.RUSHES.get().asItem(),
                WildscapesBlocks.CYPRESS_LEAVES.get().asItem(), WildscapesBlocks.LUMINOUS_MUSHROOM.get().asItem())) {
            helper.assertTrue(CompostingChanceRegistry.INSTANCE.get(item) != null,
                    item + " is not compostable");
        }
        var advancement = helper.getLevel().getServer().getAdvancements()
                .getAdvancement(new ResourceLocation("wildscapes", "recipes/building_blocks/cypress_planks"));
        helper.assertTrue(advancement != null, "cypress planks recipe has no unlock advancement");
    }
}
