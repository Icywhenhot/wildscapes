package com.wildscapes.gametest;

import java.util.List;

import com.wildscapes.block.WildscapesBlocks;
import com.wildscapes.item.CypressBoat;
import com.wildscapes.item.WildscapesItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.GameType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.fabric.api.registry.CompostableRegistry;

public final class CypressSetTests {
    private CypressSetTests() {}

    public static void cypressSetAndPolishHoldTogether(GameTestHelper helper) {
        signsHoldSignBlockEntities(helper);
        boatsUseTheCypressType(helper);
        raidBlocksAreUnbreakable(helper);
        mirelashTakesMending(helper);
        swampPlantsCompost(helper);
        frogLegStew(helper);
        helper.succeed();
    }

    private static void frogLegStew(GameTestHelper helper) {
        var level = helper.getLevel();
        var input = CraftingInput.of(3, 2, List.of(new ItemStack(Items.POTATO),
                new ItemStack(WildscapesItems.FROG_LEGS), new ItemStack(Items.BOWL),
                new ItemStack(WildscapesBlocks.LUMINOUS_MUSHROOM), ItemStack.EMPTY, new ItemStack(Items.CARROT)));
        var recipe = level.recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, level);
        helper.assertTrue(recipe.isPresent(), "frog leg stew recipe missing");
        var stew = recipe.get().value().assemble(input);
        helper.assertTrue(stew.is(WildscapesItems.FROG_LEG_STEW), "wrong stew recipe result");
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getFoodData().setFoodLevel(10);
        var bowl = stew.finishUsingItem(level, player);
        helper.assertTrue(bowl.is(Items.BOWL), "stew did not return a bowl");
        var effect = player.getEffect(MobEffects.JUMP_BOOST);
        helper.assertTrue(effect != null && effect.getAmplifier() == 1 && effect.getDuration() == 400,
                "stew did not give jump boost ii for 20 seconds");
        var legs = new SingleRecipeInput(new ItemStack(WildscapesItems.FROG_LEGS));
        for (var type : List.of(RecipeType.SMELTING, RecipeType.SMOKING, RecipeType.CAMPFIRE_COOKING)) {
            helper.assertTrue(level.recipeAccess().getRecipeFor(type, legs, level).isEmpty(),
                    "frog legs still cook in " + type);
        }
    }

    private static void signsHoldSignBlockEntities(GameTestHelper helper) {
        BlockPos sign = new BlockPos(1, 2, 1);
        BlockPos hanging = new BlockPos(3, 2, 1);
        helper.setBlock(sign, WildscapesBlocks.CYPRESS_SIGN);
        helper.setBlock(hanging, WildscapesBlocks.CYPRESS_HANGING_SIGN);
        helper.assertTrue(helper.getBlockEntity(sign, SignBlockEntity.class) != null, "cypress sign has no sign entity");
        helper.assertTrue(helper.getBlockEntity(hanging, HangingSignBlockEntity.class) != null,
                "cypress hanging sign has no hanging sign entity");
    }

    private static void boatsUseTheCypressType(GameTestHelper helper) {
        var level = helper.getLevel();
        Boat boat = CypressBoat.BOAT.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        helper.assertTrue(boat != null && boat.getType() == CypressBoat.BOAT, "wrong cypress boat entity type");
        var input = CraftingInput.of(2, 1, List.of(new ItemStack(Items.CHEST), new ItemStack(WildscapesItems.CYPRESS_BOAT)));
        var recipe = level.recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, level);
        helper.assertTrue(recipe.isPresent() && recipe.get().value().assemble(input)
                .is(WildscapesItems.CYPRESS_CHEST_BOAT), "chest boat recipe missing");
    }

    private static void raidBlocksAreUnbreakable(GameTestHelper helper) {
        for (BlockState state : List.of(WildscapesBlocks.CAULDRON_OF_SOULS.defaultBlockState(),
                WildscapesBlocks.SUMMONING_BONFIRE.defaultBlockState())) {
            helper.assertTrue(state.getDestroySpeed(helper.getLevel(), BlockPos.ZERO) < 0, state + " can be mined");
        }
    }

    private static void mirelashTakesMending(GameTestHelper helper) {
        var mending = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.MENDING);
        helper.assertTrue(mending.value().canEnchant(new ItemStack(WildscapesItems.MIRELASH)),
                "mirelash refuses mending");
    }

    private static void swampPlantsCompost(GameTestHelper helper) {
        for (var item : List.of(WildscapesBlocks.DUCKWEED.asItem(), WildscapesBlocks.RUSHES.asItem(),
                WildscapesBlocks.CYPRESS_LEAVES.asItem(), WildscapesBlocks.LUMINOUS_MUSHROOM.asItem())) {
            helper.assertTrue(CompostableRegistry.INSTANCE.get(item) != null,
                    item + " is not compostable");
        }
        var advancement = helper.getLevel().getServer().getAdvancements()
                .get(Identifier.fromNamespaceAndPath("wildscapes", "recipes/building_blocks/cypress_planks"));
        helper.assertTrue(advancement != null, "cypress planks recipe has no unlock advancement");
    }
}
