package com.wildscapes.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

final class TestBiomes {
    private TestBiomes() {}

    static void fill(GameTestHelper helper, ResourceKey<Biome> biome) {
        BlockPos from = helper.absolutePos(BlockPos.ZERO);
        BlockPos to = helper.absolutePos(new BlockPos(23, 19, 23));
        var server = helper.getLevel().getServer();
        var source = server.createCommandSourceStack().withLevel(helper.getLevel()).withSuppressedOutput().withPermission(4);
        server.getCommands().performPrefixedCommand(source, String.format("fillbiome %d %d %d %d %d %d %s",
                from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ(), biome.location()));
    }
}
