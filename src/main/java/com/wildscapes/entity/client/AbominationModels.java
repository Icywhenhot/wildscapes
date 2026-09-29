package com.wildscapes.entity.client;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;

public final class AbominationModels {
    private AbominationModels() {}

    public static LayerDefinition createTongueSegment() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("segment",
                CubeListBuilder.create().texOffs(2, 2).addBox(-1.5F, -1F, -4F, 3F, 2F, 8F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 32, 32);
    }

    public static LayerDefinition createTongueTip() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("tip",
                CubeListBuilder.create().texOffs(2, 2).addBox(-1.5F, -1F, -4F, 3F, 2F, 8F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 32, 32);
    }

    public static LayerDefinition createGrasp() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("grasp",
                CubeListBuilder.create().texOffs(0, 0).addBox(-6F, -19F, -5F, 12F, 19F, 10F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }
}
