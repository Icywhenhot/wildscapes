package com.wildscapes.entity.client;

import com.mojang.datafixers.util.Pair;
import com.wildscapes.Wildscapes;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;

public class CypressBoatRenderer extends BoatRenderer {
    private final ResourceLocation texture;

    public CypressBoatRenderer(EntityRendererProvider.Context context, boolean chest) {
        super(context, chest);
        texture = ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID,
                "textures/entity/" + (chest ? "chest_boat/" : "boat/") + "cypress.png");
    }

    @Override
    public Pair<ResourceLocation, ListModel<Boat>> getModelWithLocation(Boat boat) {
        return Pair.of(texture, super.getModelWithLocation(boat).getSecond());
    }
}
