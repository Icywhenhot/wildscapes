package com.wildscapes.entity.client;

import java.util.Map;
import com.mojang.datafixers.util.Pair;
import com.wildscapes.Wildscapes;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.vehicle.Boat;

public class CypressBoatRenderer extends BoatRenderer {
    public CypressBoatRenderer(EntityRendererProvider.Context context, boolean chest) {
        super(context, chest);
        boatResources = Map.of(Boat.Type.OAK, Pair.of(Wildscapes.id("textures/entity/"
                + (chest ? "chest_boat" : "boat") + "/cypress.png"), boatResources.get(Boat.Type.OAK).getSecond()));
    }
}
