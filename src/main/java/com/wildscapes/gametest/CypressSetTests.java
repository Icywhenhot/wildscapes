package com.wildscapes.gametest;

import java.util.List;

import com.wildscapes.block.WildscapesBlocks;
import com.wildscapes.item.CypressBoat;
import com.wildscapes.item.WildscapesItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;
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
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

@GameTestHolder("wildscapes")
@PrefixGameTestTemplate(false)
public final class CypressSetTests {
    private CypressSetTests() {}

    @GameTest(template = "gametest/tree_platform", batch = "cypress_set")
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
                new ItemStack(WildscapesItems.FROG_LEGS.get()), new ItemStack(Items.BOWL),
                new ItemStack(WildscapesBlocks.LUMINOUS_MUSHROOM.get()), ItemStack.EMPTY, new ItemStack(Items.CARROT)));
        var recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
        helper.assertTrue(recipe.isPresent(), "frog leg stew recipe missing");
        var stew = recipe.get().value().assemble(input, level.registryAccess());
        helper.assertTrue(stew.is(WildscapesItems.FROG_LEG_STEW.get()), "wrong stew recipe result");
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getFoodData().setFoodLevel(10);
        var bowl = stew.finishUsingItem(level, player);
        helper.assertTrue(bowl.is(Items.BOWL), "stew did not return a bowl");
        var effect = player.getEffect(MobEffects.JUMP);
        helper.assertTrue(effect != null && effect.getAmplifier() == 1 && effect.getDuration() == 400,
                "stew did not give jump boost ii for 20 seconds");
        var legs = new SingleRecipeInput(new ItemStack(WildscapesItems.FROG_LEGS.get()));
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

    private static void boatsUseTheCypressType(GameTestHelper helper) {
        Boat.Type type = CypressBoat.TYPE.getValue();
        helper.assertTrue(type.getName().equals("wildscapes:cypress"), "wrong boat type name");
        helper.assertTrue(type.getPlanks() == WildscapesBlocks.CYPRESS_PLANKS.get(), "boat drops the wrong planks");
        Boat boat = new Boat(helper.getLevel(), 0, 0, 0);
        boat.setVariant(type);
        helper.assertTrue(boat.getDropItem() == WildscapesItems.CYPRESS_BOAT.get(), "boat drops the wrong item");
        var level = helper.getLevel();
        var input = CraftingInput.of(2, 1, List.of(new ItemStack(Items.CHEST), new ItemStack(WildscapesItems.CYPRESS_BOAT.get())));
        var recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
        helper.assertTrue(recipe.isPresent() && recipe.get().value().assemble(input, level.registryAccess())
                .is(WildscapesItems.CYPRESS_CHEST_BOAT.get()), "chest boat recipe missing");
    }

    private static void raidBlocksAreUnbreakable(GameTestHelper helper) {
        for (BlockState state : List.of(WildscapesBlocks.CAULDRON_OF_SOULS.get().defaultBlockState(),
                WildscapesBlocks.SUMMONING_BONFIRE.get().defaultBlockState())) {
            helper.assertTrue(state.getDestroySpeed(helper.getLevel(), BlockPos.ZERO) < 0, state + " can be mined");
        }
    }

    private static void mirelashTakesMending(GameTestHelper helper) {
        var mending = helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.MENDING);
        helper.assertTrue(new ItemStack(WildscapesItems.MIRELASH.get()).supportsEnchantment(mending),
                "mirelash refuses mending");
    }

    private static void swampPlantsCompost(GameTestHelper helper) {
        for (var item : List.of(WildscapesBlocks.DUCKWEED.get().asItem(), WildscapesBlocks.RUSHES.get().asItem(),
                WildscapesBlocks.CYPRESS_LEAVES.get().asItem(), WildscapesBlocks.LUMINOUS_MUSHROOM.get().asItem())) {
            helper.assertTrue(item.builtInRegistryHolder().getData(NeoForgeDataMaps.COMPOSTABLES) != null,
                    item + " is not compostable");
        }
        var advancement = helper.getLevel().getServer().getAdvancements()
                .get(ResourceLocation.fromNamespaceAndPath("wildscapes", "recipes/building_blocks/cypress_planks"));
        helper.assertTrue(advancement != null, "cypress planks recipe has no unlock advancement");
    }
}
