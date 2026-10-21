package com.wildscapes.gametest;

import com.wildscapes.block.WildscapesBlocks;
import com.wildscapes.block.WildscapesCauldronBlock;
import com.wildscapes.block.entity.CauldronBlockEntity;
import com.wildscapes.effect.Intangibility;
import com.wildscapes.item.WildscapesItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("wildscapes")
@PrefixGameTestTemplate(false)
public final class AdvancementTests {
    private static final String PLATFORM = "gametest/tree_platform";

    private AdvancementTests() {}

    @GameTest(template = PLATFORM)
    public static void soupNeedsToBeCollectedFromCauldron(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        var level = helper.getLevel();
        BlockPos pos = new BlockPos(5, 3, 5);
        BlockPos at = helper.absolutePos(pos);
        helper.setBlock(pos.below(), Blocks.CAMPFIRE.defaultBlockState());
        helper.setBlock(pos, WildscapesBlocks.CAULDRON.get().defaultBlockState()
                .setValue(WildscapesCauldronBlock.BOILING, true));
        var be = (CauldronBlockEntity) level.getBlockEntity(at);
        be.setPlainWater(3);
        be.startSoupBase();
        var hit = new BlockHitResult(Vec3.atCenterOf(at), Direction.UP, at, false);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOWL));
        level.getBlockState(at).useItemOn(player.getMainHandItem(), level, player, InteractionHand.MAIN_HAND, hit);
        helper.assertFalse(earned(player, "cooking_is_my_passion"), "unfinished soup awarded advancement");

        be.addIngredient(new ItemStack(Items.SUGAR));
        be.startMixing();
        for (int i = 0; i < CauldronBlockEntity.MIX_DURATION; i++) {
            CauldronBlockEntity.serverTick(level, at, level.getBlockState(at), be);
        }
        helper.assertTrue(be.hasFinishedSoup(), "soup did not finish cooking");
        helper.assertFalse(earned(player, "cooking_is_my_passion"), "uncollected soup awarded advancement");
        level.getBlockState(at).useItemOn(player.getMainHandItem(), level, player, InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(player.getMainHandItem().is(WildscapesItems.MAGIC_SOUP), "bowl did not collect soup");
        helper.assertTrue(earned(player, "cooking_is_my_passion"), "collecting soup did not award advancement");
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void necklaceNeedsToBeActivated(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        var stack = new ItemStack(WildscapesItems.ILLUSIONIST_NECKLACE.get());
        player.setItemInHand(InteractionHand.OFF_HAND, stack);
        Intangibility.onPlayerTick(new EntityTickEvent.Post(player));
        helper.assertFalse(earned(player, "now_you_see_me_now_you_dont"), "holding necklace awarded advancement");
        stack.setDamageValue(stack.getMaxDamage() - 1);
        player.setShiftKeyDown(true);
        Intangibility.onPlayerTick(new EntityTickEvent.Post(player));
        helper.assertFalse(earned(player, "now_you_see_me_now_you_dont"), "exhausted necklace awarded advancement");
        stack.setDamageValue(0);
        Intangibility.onPlayerTick(new EntityTickEvent.Post(player));
        helper.assertTrue(earned(player, "now_you_see_me_now_you_dont"), "activating necklace did not award advancement");
        helper.succeed();
    }

    private static boolean earned(ServerPlayer player, String path) {
        var advancement = player.server.getAdvancements().get(ResourceLocation.fromNamespaceAndPath("wildscapes", path));
        return player.getAdvancements().getOrStartProgress(advancement).isDone();
    }
}
