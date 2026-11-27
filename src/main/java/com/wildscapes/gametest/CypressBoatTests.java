package com.wildscapes.gametest;

import net.minecraft.core.BlockSourceImpl;
import com.wildscapes.item.CypressBoatItem;
import com.wildscapes.entity.WildscapesEntities;
import com.wildscapes.item.WildscapesItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BoatDispenseItemBehavior;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.phys.AABB;

public final class CypressBoatTests {
    private CypressBoatTests() {}

    public static void placingAndDispensingKeepTheType(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(8, 4, 8));
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) level.setBlockAndUpdate(pos.offset(x, 0, z), Blocks.WATER.defaultBlockState());
        }
        var player = helper.makeMockSurvivalPlayer();
        player.moveTo(pos.getX() + 0.5, pos.getY() + 2, pos.getZ() + 0.5, 0, 90);
        var types = java.util.List.of(WildscapesEntities.CYPRESS_BOAT.get(), WildscapesEntities.CYPRESS_CHEST_BOAT.get());
        var items = java.util.List.of(WildscapesItems.CYPRESS_BOAT.get(), WildscapesItems.CYPRESS_CHEST_BOAT.get());
        for (int i = 0; i < 2; i++) {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(items.get(i)));
            var result = items.get(i).use(level, player, InteractionHand.MAIN_HAND);
            helper.assertTrue(result.getResult().consumesAction(), "boat item did not place a boat");
            var boats = level.getEntitiesOfClass(Boat.class, new AABB(pos).inflate(6));
            helper.assertTrue(boats.size() == 1 && boats.get(0).getType() == types.get(i), "item spawned the wrong boat type");
            helper.assertTrue(boats.get(0).getDropItem() == items.get(i), "boat drops the wrong item");
            boats.forEach(Boat::discard);
        }
        BlockPos dispenser = pos.above(2).east(5);
        level.setBlockAndUpdate(dispenser, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.NORTH));
        level.setBlockAndUpdate(dispenser.north(), Blocks.WATER.defaultBlockState());
        var source = new BlockSourceImpl(level, dispenser);
        for (int i = 0; i < 2; i++) {
            CypressBoatItem.DISPENSER.dispense(source, new ItemStack(items.get(i)));
            var boats = level.getEntitiesOfClass(Boat.class, new AABB(dispenser).inflate(3));
            helper.assertTrue(boats.size() == 1 && boats.get(0).getType() == types.get(i), "dispenser spawned the wrong boat type");
            boats.forEach(Boat::discard);
        }
        helper.succeed();
    }
}
