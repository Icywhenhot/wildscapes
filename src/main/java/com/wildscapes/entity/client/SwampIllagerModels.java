package com.wildscapes.entity.client;

import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class SwampIllagerModels {
    private SwampIllagerModels() {}

    private static final PartPose ARMS_POSE = PartPose.offsetAndRotation(0.0F, 3.0F, -1.0F, -0.75F, 0.0F, 0.0F);

    public static LayerDefinition createPillagerLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(32, 0).addBox(-4F, -10F, -4F, 8F, 10F, 8F, new CubeDeformation(0.25F))
                        .texOffs(0, 0).addBox(-4F, -10F, -4F, 8F, 10F, 8F),
                PartPose.ZERO);
        head.addOrReplaceChild("hat",
                CubeListBuilder.create(),
                PartPose.ZERO);
        head.addOrReplaceChild("nose",
                CubeListBuilder.create()
                        .texOffs(24, 0).addBox(-1F, -1F, -6F, 2F, 4F, 2F),
                PartPose.offset(0F, -2F, 0F));
        root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(16, 20).addBox(-4F, 0F, -3F, 8F, 12F, 6F)
                        .texOffs(0, 38).addBox(-4F, 0F, -3F, 8F, 18F, 6F, new CubeDeformation(0.5F)),
                PartPose.ZERO);
        PartDefinition arms = root.addOrReplaceChild("arms",
                CubeListBuilder.create(),
                ARMS_POSE);
        arms.addOrReplaceChild("left_shoulder",
                CubeListBuilder.create(),
                PartPose.ZERO);
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(0, 22).addBox(-2F, 0F, -2F, 4F, 12F, 4F)
                        .mirror(false),
                PartPose.offset(-2F, 12F, 0F));
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 22).addBox(-2F, 0F, -2F, 4F, 12F, 4F),
                PartPose.offset(2F, 12F, 0F));
        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(40, 46).addBox(-3F, -2F, -2F, 4F, 12F, 4F),
                PartPose.offset(-5F, 2F, 0F));
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(40, 46).addBox(-1F, -2F, -2F, 4F, 12F, 4F)
                        .mirror(false),
                PartPose.offset(5F, 2F, 0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    public static LayerDefinition createVindicatorLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4F, -10F, -4F, 8F, 10F, 8F),
                PartPose.ZERO);

        head.addOrReplaceChild("hat",
                CubeListBuilder.create()
                        .texOffs(32, 0).addBox(-4F, -10F, -4F, 8F, 10F, 8F, new CubeDeformation(0.25F)),
                PartPose.ZERO);
        head.addOrReplaceChild("nose",
                CubeListBuilder.create()
                        .texOffs(24, 0).addBox(-1F, -1F, -6F, 2F, 4F, 2F),
                PartPose.offset(0F, -2F, 0F));
        root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(16, 20).addBox(-4F, 0F, -3F, 8F, 12F, 6F)
                        .texOffs(0, 38).addBox(-4F, 0F, -3F, 8F, 18F, 6F, new CubeDeformation(0.5F)),
                PartPose.ZERO);
        PartDefinition arms = root.addOrReplaceChild("arms",
                CubeListBuilder.create()
                        .texOffs(44, 22).addBox(-8F, -3F, -1F, 4F, 8F, 4F)
                        .texOffs(44, 22).addBox(4F, -3F, -1F, 4F, 8F, 4F)
                        .texOffs(40, 38).addBox(-4F, 1F, -1F, 8F, 4F, 4F),
                ARMS_POSE);
        arms.addOrReplaceChild("left_shoulder",
                CubeListBuilder.create(),
                PartPose.ZERO);
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 22).addBox(-2F, 0F, -2F, 4F, 12F, 4F),
                PartPose.offset(-2F, 12F, 0F));
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(0, 22).addBox(-2F, 0F, -2F, 4F, 12F, 4F)
                        .mirror(false),
                PartPose.offset(2F, 12F, 0F));
        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(40, 46).addBox(-3F, -2F, -2F, 4F, 12F, 4F, new CubeDeformation(0.01F)),
                PartPose.offset(-5F, 2F, 0F));
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(40, 46).addBox(-1F, -2F, -2F, 4F, 12F, 4F, new CubeDeformation(0.01F))
                        .mirror(false),
                PartPose.offset(5F, 2F, 0F));

        return LayerDefinition.create(mesh, 64, 64);
    }
}
