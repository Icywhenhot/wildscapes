package com.wildscapes.entity.client;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class RedesignedIllusionerModel {
    private RedesignedIllusionerModel() {}

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 24).addBox(-4F, -10F, -4F, 8F, 10F, 8F),
                PartPose.ZERO);
        head.addOrReplaceChild("hat",
                CubeListBuilder.create()
                        .texOffs(28, 0).addBox(-4F, -10F, -4F, 8F, 10F, 8F, new CubeDeformation(0.25F)),
                PartPose.ZERO);
        head.addOrReplaceChild("nose",
                CubeListBuilder.create()
                        .texOffs(18, 42).addBox(-1F, -3F, -6F, 2F, 4F, 2F),
                PartPose.ZERO);
        head.addOrReplaceChild("right_flap",
                CubeListBuilder.create()
                        .texOffs(0, 57).addBox(0F, -6F, -4F, 0F, 6F, 7F),
                PartPose.offsetAndRotation(4.24913F, 0.2495F, 1.25F, 0F, 0F, 0.5236F));
        head.addOrReplaceChild("left_flap",
                CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(0, 57).addBox(0F, -6F, -4F, 0F, 6F, 7F)
                        .mirror(false),
                PartPose.offsetAndRotation(-4.24913F, 0.2495F, 1.25F, 0F, 0F, -0.5236F));
        head.addOrReplaceChild("veil",
                CubeListBuilder.create()
                        .texOffs(0, 51).addBox(-4.5F, -6F, 0F, 9F, 6F, 0F),
                PartPose.offsetAndRotation(0F, 0.2495F, 4.25F, -0.5236F, 0F, 0F));

        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(32, 18).addBox(-4F, 0F, -3F, 8F, 12F, 6F)
                        .texOffs(0, 0).addBox(-4F, 0F, -3F, 8F, 18F, 6F, new CubeDeformation(0.5F)),
                PartPose.ZERO);
        body.addOrReplaceChild("cape",
                CubeListBuilder.create()
                        .texOffs(32, 36).addBox(-4.5F, 0F, 0F, 9F, 18F, 0F),
                PartPose.offsetAndRotation(0F, -0.5F, 3.55F, 0.1745F, 0F, 0F));

        PartDefinition arms = root.addOrReplaceChild("arms",
                CubeListBuilder.create()
                        .texOffs(34, 54).addBox(-8F, -3F, -1F, 4F, 8F, 4F)
                        .texOffs(34, 54).addBox(4F, -3F, -1F, 4F, 8F, 4F)
                        .texOffs(50, 36).addBox(-4F, 1F, -1F, 8F, 4F, 4F),
                PartPose.offsetAndRotation(0F, 3F, -1F, -0.75F, 0F, 0F));
        arms.addOrReplaceChild("left_shoulder",
                CubeListBuilder.create(),
                PartPose.ZERO);

        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(50, 44).addBox(-2F, 0F, -2F, 4F, 12F, 4F),
                PartPose.offset(-2F, 12F, 0F));
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(50, 44).addBox(-2F, 0F, -2F, 4F, 12F, 4F)
                        .mirror(false),
                PartPose.offset(2F, 12F, 0F));

        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(18, 54).addBox(-3F, -2F, -2F, 4F, 12F, 4F, new CubeDeformation(0.01F)),
                PartPose.offset(-5F, 2F, 0F));
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(18, 54).addBox(-1F, -2F, -2F, 4F, 12F, 4F, new CubeDeformation(0.01F))
                        .mirror(false),
                PartPose.offset(5F, 2F, 0F));

        return LayerDefinition.create(mesh, 128, 128);
    }
}
