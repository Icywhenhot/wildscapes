package com.wildscapes.item;

import java.util.function.Supplier;

import com.wildscapes.block.WildscapesBlocks;

import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

public final class CypressBoat {
    private CypressBoat() {}

    public static final EnumProxy<Boat.Type> TYPE = new EnumProxy<>(Boat.Type.class,
            (Supplier<Block>) WildscapesBlocks.CYPRESS_PLANKS::get,
            "wildscapes:cypress",
            (Supplier<Item>) WildscapesItems.CYPRESS_BOAT::get,
            (Supplier<Item>) WildscapesItems.CYPRESS_CHEST_BOAT::get,
            (Supplier<Item>) () -> Items.STICK,
            false);
}
