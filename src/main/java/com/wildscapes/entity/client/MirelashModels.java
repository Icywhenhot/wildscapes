package com.wildscapes.entity.client;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;

public final class MirelashModels {
    private MirelashModels() {}

    public static LayerDefinition createHookLayer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("hook",
                CubeListBuilder.create()
                        .texOffs(6, 6).addBox(-1.5F, -1.25F, -2.5F, 3F, 3F, 4F, new CubeDeformation(0.3F))
                        .texOffs(0, 11).addBox(0F, -4.25F, -10.5F, 0F, 8F, 8F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 32, 32);
    }

    public static LayerDefinition createSegmentLayer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("segment",
                CubeListBuilder.create().texOffs(2, 2).addBox(-1.5F, -1.5F, -4F, 3F, 3F, 8F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 32, 32);
    }

    public static LayerDefinition createSlimeBubbleLayer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("bubble",
                CubeListBuilder.create()
                        .texOffs(96, 32).addBox(-16F, -16F, -16F, 32F, 32F, 0F)
                        .texOffs(96, 0).addBox(-16F, -16F, -16F, 0F, 32F, 32F)
                        .texOffs(96, 32).addBox(-16F, -16F, 16F, 32F, 32F, 0F)
                        .texOffs(96, 0).addBox(16F, -16F, -16F, 0F, 32F, 32F)
                        .texOffs(-32, 0).addBox(-16F, 16F, -16F, 32F, 0F, 32F)
                        .texOffs(32, 0).addBox(-16F, -16F, -16F, 32F, 0F, 32F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 256, 128);
    }
}
