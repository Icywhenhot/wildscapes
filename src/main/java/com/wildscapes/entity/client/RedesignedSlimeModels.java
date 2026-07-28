package com.wildscapes.entity.client;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Slime geometry converted from {@code models/complete redesign/slime/*.bbmodel}.
 *
 * <p>Unlike vanilla — one 1-block model the renderer scales up — each size was modelled
 * at its real size: slime1 is the small (size 1) slime, slime2 the medium (size 2) and
 * slime3 the big one (size 4). {@link RedesignedSlimeRenderer} picks the matching pair and
 * only scales when a slime's size doesn't land on one of those three.
 *
 * <p>Part names match vanilla's {@code SlimeModel} layers so the translucent outer shell
 * and the inner core split the same way.
 */
public final class RedesignedSlimeModels {
    private RedesignedSlimeModels() {}

    public static LayerDefinition createSmallInnerLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("cube",
                CubeListBuilder.create().texOffs(0, 16).addBox(-3F, 17F, -3F, 6F, 6F, 6F),
                PartPose.ZERO);
        root.addOrReplaceChild("right_eye",
                CubeListBuilder.create().texOffs(32, 0).addBox(-3.5F, 18F, -3.5F, 2F, 2F, 2F),
                PartPose.ZERO);
        root.addOrReplaceChild("left_eye",
                CubeListBuilder.create().texOffs(32, 4).addBox(1.5F, 18F, -3.5F, 2F, 2F, 2F),
                PartPose.ZERO);
        root.addOrReplaceChild("mouth",
                CubeListBuilder.create().texOffs(32, 8).addBox(0F, 21F, -3.5F, 1F, 1F, 1F),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 32);
    }

    public static LayerDefinition createSmallOuterLayer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("cube",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4F, 16F, -4F, 8F, 8F, 8F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static LayerDefinition createMediumInnerLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("cube",
                CubeListBuilder.create().texOffs(0, 32).addBox(-6F, 10F, -6F, 12F, 12F, 12F),
                PartPose.ZERO);
        root.addOrReplaceChild("right_eye",
                CubeListBuilder.create().texOffs(68, 4).addBox(-3.5F, 15F, -6.5F, 2F, 2F, 2F),
                PartPose.ZERO);
        root.addOrReplaceChild("left_eye",
                CubeListBuilder.create().texOffs(66, 12).addBox(1.5F, 15F, -6.5F, 2F, 2F, 2F),
                PartPose.ZERO);
        root.addOrReplaceChild("mouth",
                CubeListBuilder.create().texOffs(66, 18).addBox(0F, 19F, -6.5F, 1F, 1F, 1F),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 128, 64);
    }

    public static LayerDefinition createMediumOuterLayer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("cube",
                CubeListBuilder.create().texOffs(0, 0).addBox(-8F, 8F, -8F, 16F, 16F, 16F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 128, 64);
    }

    public static LayerDefinition createLargeInnerLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("cube",
                CubeListBuilder.create().texOffs(0, 64).addBox(-11F, -4F, -11F, 22F, 22F, 22F),
                PartPose.ZERO);
        root.addOrReplaceChild("right_eye",
                CubeListBuilder.create().texOffs(132, 24).addBox(-5.5F, 2F, -11.5F, 2F, 2F, 2F),
                PartPose.ZERO);
        root.addOrReplaceChild("left_eye",
                CubeListBuilder.create().texOffs(132, 28).addBox(3.5F, 2F, -11.5F, 2F, 2F, 2F),
                PartPose.ZERO);
        root.addOrReplaceChild("mouth",
                CubeListBuilder.create().texOffs(132, 32).addBox(1F, 10F, -11.5F, 1F, 1F, 1F),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 256, 128);
    }

    public static LayerDefinition createLargeOuterLayer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("cube",
                CubeListBuilder.create().texOffs(0, 0).addBox(-16F, -8F, -16F, 32F, 32F, 32F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 256, 128);
    }
}
