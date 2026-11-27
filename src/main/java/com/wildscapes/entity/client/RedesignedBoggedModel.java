package com.wildscapes.entity.client;

import net.minecraft.client.model.BoggedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class RedesignedBoggedModel extends BoggedModel {
    public RedesignedBoggedModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F),
                PartPose.ZERO);
        root.addOrReplaceChild("hat",
                CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-4F, -8F, -4F, 8F, 8F, 8F),
                PartPose.ZERO);
        PartDefinition mushrooms = head.addOrReplaceChild("mushrooms",
                CubeListBuilder.create(),
                PartPose.ZERO);
        mushrooms.addOrReplaceChild("red_mushroom_1",
                CubeListBuilder.create()
                        .texOffs(40, 26).addBox(-3F, -3F, 0F, 6F, 4F, 0F),
                PartPose.offsetAndRotation(3F, -8F, 3F, 0F, 0.785398F, 0F));
        mushrooms.addOrReplaceChild("red_mushroom_2",
                CubeListBuilder.create()
                        .texOffs(40, 26).addBox(-3F, -3F, 0F, 6F, 4F, 0F),
                PartPose.offsetAndRotation(3F, -8F, 3F, 0F, 2.356194F, 0F));
        mushrooms.addOrReplaceChild("brown_mushroom_1",
                CubeListBuilder.create()
                        .texOffs(40, 30).addBox(-3F, -3F, 0F, 6F, 4F, 0F),
                PartPose.offsetAndRotation(-3F, -8F, -3F, 0F, 0.785398F, 0F));
        mushrooms.addOrReplaceChild("brown_mushroom_2",
                CubeListBuilder.create()
                        .texOffs(40, 30).addBox(-3F, -3F, 0F, 6F, 4F, 0F),
                PartPose.offsetAndRotation(-3F, -8F, -3F, 0F, 2.356194F, 0F));
        mushrooms.addOrReplaceChild("brown_mushroom_3",
                CubeListBuilder.create()
                        .texOffs(32, 40).addBox(-3F, -4F, 0F, 6F, 4F, 0F),
                PartPose.offsetAndRotation(-2F, -1F, 4F, -1.570796F, 0F, 0.785398F));
        mushrooms.addOrReplaceChild("brown_mushroom_4",
                CubeListBuilder.create()
                        .texOffs(32, 40).addBox(-3F, -4F, 0F, 6F, 4F, 0F),
                PartPose.offsetAndRotation(-2F, -1F, 4F, -1.570796F, 0F, 2.356194F));
        root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 32).addBox(-4F, 0F, -2F, 8F, 12F, 4F)
                        .texOffs(32, 0).addBox(-4F, 0F, -2F, 8F, 12F, 4F, new CubeDeformation(0.25F)),
                PartPose.ZERO);
        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(24, 32).addBox(-1F, -2F, -1F, 2F, 12F, 2F, new CubeDeformation(0.01F))
                        .texOffs(32, 16).addBox(-3F, -2F, -2F, 4F, 6F, 4F, new CubeDeformation(0.02F)),
                PartPose.offset(-5F, 2F, 0F));
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .mirror(true).texOffs(24, 32).addBox(-1F, -2F, -1F, 2F, 12F, 2F, new CubeDeformation(0.01F))
                        .mirror(false).texOffs(32, 16).addBox(-1F, -2F, -2F, 4F, 6F, 4F, new CubeDeformation(0.02F)),
                PartPose.offset(5F, 2F, 0F));
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(32, 26).addBox(-1F, 0F, -1F, 2F, 12F, 2F),
                PartPose.offset(-2F, 12F, 0F));
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .mirror(true).texOffs(32, 26).addBox(-1F, 0F, -1F, 2F, 12F, 2F),
                PartPose.offset(2F, 12F, 0F));

        return LayerDefinition.create(mesh, 64, 64);
    }
}
