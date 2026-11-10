package com.wildscapes.item;

import com.wildscapes.entity.WildscapesEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class CypressBoatItem extends BoatItem {
    private final boolean chest;

    public static final DefaultDispenseItemBehavior DISPENSER = new DefaultDispenseItemBehavior() {
        @Override
        protected ItemStack execute(BlockSource source, ItemStack stack) {
            Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
            Level level = source.getLevel();
            double distance = 0.5625 + WildscapesEntities.CYPRESS_BOAT.get().getWidth() / 2.0;
            double x = source.x() + direction.getStepX() * distance;
            double y = source.y() + direction.getStepY() * 1.125;
            double z = source.z() + direction.getStepZ() * distance;
            BlockPos pos = source.getPos().relative(direction);
            Boat boat = ((CypressBoatItem) stack.getItem()).create(level, new Vec3(x, y, z));
            if (boat.canBoatInFluid(level.getFluidState(pos))) {
                y++;
            } else if (!level.getBlockState(pos).isAir() || !boat.canBoatInFluid(level.getFluidState(pos.below()))) {
                return super.execute(source, stack);
            }
            boat.setPos(x, y, z);
            boat.setYRot(direction.toYRot());
            level.addFreshEntity(boat);
            stack.shrink(1);
            return stack;
        }
    };

    public CypressBoatItem(boolean chest, Properties properties) {
        super(chest, Boat.Type.OAK, properties);
        this.chest = chest;
    }

    @Override
    public Boat getBoat(Level level, HitResult hit) {
        return create(level, hit.getLocation());
    }

    public Boat create(Level level, Vec3 pos) {
        Boat boat = chest ? WildscapesEntities.CYPRESS_CHEST_BOAT.get().create(level)
                : WildscapesEntities.CYPRESS_BOAT.get().create(level);
        boat.setPos(pos);
        return boat;
    }
}
