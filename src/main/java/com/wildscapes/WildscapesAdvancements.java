package com.wildscapes;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class WildscapesAdvancements {
    private WildscapesAdvancements() {}

    public static void award(Player player, String path) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        var advancement = serverPlayer.level().getServer().getAdvancements()
                .get(Identifier.fromNamespaceAndPath(Wildscapes.MODID, path));
        if (advancement != null) {
            serverPlayer.getAdvancements().award(advancement, "trigger");
        }
    }
}
