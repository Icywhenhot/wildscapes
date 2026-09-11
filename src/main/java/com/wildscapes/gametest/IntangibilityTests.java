package com.wildscapes.gametest;

import com.wildscapes.effect.Intangibility;
import com.wildscapes.item.WildscapesItems;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("wildscapes")
@PrefixGameTestTemplate(false)
public final class IntangibilityTests {
    private static final String PLATFORM = "gametest/tree_platform";

    private IntangibilityTests() {}

    @GameTest(template = PLATFORM)
    public static void mobsLoseIntangiblePlayer(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(5, 3, 5))));
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(WildscapesItems.ILLUSIONIST_NECKLACE.get()));
        var zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(6, 3, 5));
        zombie.setTarget(player);
        helper.assertTrue(zombie.getTarget() == player, "zombie did not acquire visible player");
        player.setShiftKeyDown(true);
        Intangibility.onMobTick(new EntityTickEvent.Pre(zombie));
        helper.assertTrue(zombie.getTarget() == null, "zombie retained intangible target");
        zombie.setTarget(player);
        helper.assertTrue(zombie.getTarget() == null, "zombie reacquired intangible target");
        helper.assertTrue(!TargetingConditions.forNonCombat().test(zombie, player), "passive detection found player");
        helper.assertTrue(!zombie.hasLineOfSight(player), "mob could see intangible player");
        var warden = helper.spawn(EntityType.WARDEN, new BlockPos(8, 3, 5));
        helper.assertTrue(!warden.canTargetEntity(player), "warden detected intangible player");
        player.setShiftKeyDown(false);
        zombie.setTarget(player);
        helper.assertTrue(zombie.getTarget() == player, "player stayed undetectable after uncrouching");
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void necklaceWearsOutWhileActive(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var stack = new ItemStack(WildscapesItems.ILLUSIONIST_NECKLACE.get());
        player.setItemInHand(InteractionHand.OFF_HAND, stack);
        for (int i = 0; i < 40; i++) {
            Intangibility.onPlayerTick(new EntityTickEvent.Post(player));
        }
        helper.assertTrue(stack.getDamageValue() == 0, "inactive necklace lost durability");
        player.setShiftKeyDown(true);
        for (int i = 0; i < 40; i++) {
            Intangibility.onPlayerTick(new EntityTickEvent.Post(player));
        }
        helper.assertTrue(stack.getDamageValue() == 2, "necklace did not lose one durability per second");
        stack.setDamageValue(stack.getMaxDamage() - 2);
        for (int i = 0; i < 40; i++) {
            Intangibility.onPlayerTick(new EntityTickEvent.Post(player));
        }
        helper.assertTrue(!stack.isEmpty(), "exhausted necklace was destroyed");
        helper.assertTrue(stack.getDamageValue() == stack.getMaxDamage() - 1, "necklace did not stop at one durability");
        helper.assertTrue(!Intangibility.isActive(player), "exhausted necklace still grants phasing");
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void playerCrossesSolidBlocksAndCobwebs(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(5, 3, 5))));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(WildscapesItems.ILLUSIONIST_NECKLACE.get()));
        player.setShiftKeyDown(true);
        for (int x = 5; x <= 9; x++) {
            helper.setBlock(new BlockPos(x, 2, 5), Blocks.STONE);
            helper.setBlock(new BlockPos(x, 3, 5), x == 7 ? Blocks.COBWEB : Blocks.STONE);
            helper.setBlock(new BlockPos(x, 4, 5), Blocks.STONE);
        }
        double x = player.getX();
        double y = player.getY();
        player.makeStuckInBlock(Blocks.COBWEB.defaultBlockState(), new Vec3(0.25, 0.05, 0.25));
        for (int i = 0; i < 12; i++) {
            player.move(MoverType.PLAYER, new Vec3(0.25, -0.08, 0));
            helper.assertTrue(!player.isInWall(), "player suffocates inside solid blocks");
        }
        helper.assertTrue(Math.abs(player.getX() - x - 3) < 0.001, "blocks slowed phasing movement");
        helper.assertTrue(player.getY() == y, "player phased through the floor");
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void playerPassesThroughWall(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(5, 3, 5))));
        player.setShiftKeyDown(true);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(WildscapesItems.ILLUSIONIST_NECKLACE.get()));
        helper.setBlock(new BlockPos(5, 2, 5), Blocks.STONE);
        helper.setBlock(new BlockPos(6, 3, 5), Blocks.STONE);
        helper.setBlock(new BlockPos(6, 4, 5), Blocks.STONE);
        double x = player.getX();
        double y = player.getY();

        helper.assertTrue(player.isShiftKeyDown(), "mock player did not crouch");
        helper.assertTrue(player.getMainHandItem().is(WildscapesItems.ILLUSIONIST_NECKLACE),
                "mock player did not hold the necklace");
        helper.assertTrue(Intangibility.isActive(player), "necklace did not activate phasing");
        player.move(MoverType.PLAYER, new Vec3(1.0, -0.5, 0.0));

        helper.assertTrue(player.getX() > x + 0.9,
                "wall blocked the intangible player at " + player.getX() + " from " + x);
        helper.assertTrue(player.getY() == y,
                "intangible player fell through the floor to " + player.getY() + " from " + y);
        helper.succeed();
    }
}
