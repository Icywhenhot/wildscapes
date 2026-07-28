package com.wildscapes.entity.client;

import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.WitchModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Witch geometry converted from {@code models/complete redesign/witch.bbmodel}.
 *
 * <p>Part names mirror {@link WitchModel} / {@link VillagerModel} so the vanilla classes
 * still drive it — the twitching nose, the head tracking, the walk cycle and the
 * potion-drinking pose all come along unchanged.
 *
 * <p>{@code hat_rim} is empty: the redesign replaces vanilla's wide flat brim with the
 * chunkier {@code hat} block, but {@link VillagerModel}'s constructor still looks the part
 * up. {@code jacket} and {@code mole} carry the redesign's second body and nose cubes.
 */
public final class RedesignedWitchModel {
    private RedesignedWitchModel() {}

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(0, 0).addBox(-4F, -10F, -4F, 8F, 10F, 8F)
                        .texOffs(16, 94).addBox(-4F, -6F, -4.01F, 8F, 2F, 0F)
                        .mirror(false),
                PartPose.ZERO);
        PartDefinition hat = head.addOrReplaceChild("hat",
                CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(24, 64).addBox(-10F, -5.02F, 0F, 10F, 3F, 10F)
                        .mirror(false),
                PartPose.offset(5F, -7.03F, -5F));
        hat.addOrReplaceChild("hat_rim",
                CubeListBuilder.create(),
                PartPose.ZERO);
        PartDefinition hat2 = hat.addOrReplaceChild("hat2",
                CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(0, 76).addBox(-2F, -5.5F, -5.5F, 7F, 4F, 7F)
                        .mirror(false),
                PartPose.offsetAndRotation(-6.75F, 0.03F, 7F, -0.0524F, 0F, 0.0262F));
        PartDefinition hat3 = hat2.addOrReplaceChild("hat3",
                CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(0, 87).addBox(-0.75F, -5.5F, -3F, 4F, 4F, 4F)
                        .mirror(false),
                PartPose.offsetAndRotation(0F, -3F, 0F, -0.1047F, 0F, 0.0524F));
        hat3.addOrReplaceChild("hat4",
                CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(0, 95).addBox(0.5F, -4F, -1F, 1F, 2F, 1F, new CubeDeformation(0.25F))
                        .mirror(false),
                PartPose.offsetAndRotation(0F, -3F, 0F, -0.2094F, 0F, 0.1047F));
        PartDefinition nose = head.addOrReplaceChild("nose",
                CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(24, 0).addBox(-1F, -1F, -6F, 2F, 4F, 2F)
                        .mirror(false),
                PartPose.offset(0F, -2F, 0F));
        nose.addOrReplaceChild("mole",
                CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(0, 0).addBox(-1F, 0F, -6.75F, 1F, 1F, 1F, new CubeDeformation(-0.25F))
                        .mirror(false),
                PartPose.ZERO);
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(16, 20).addBox(-4F, 0F, -3F, 8F, 12F, 6F)
                        .mirror(false),
                PartPose.ZERO);
        body.addOrReplaceChild("jacket",
                CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(0, 38).addBox(-4F, 0F, -3F, 8F, 18F, 6F, new CubeDeformation(0.5F))
                        .mirror(false),
                PartPose.ZERO);
        root.addOrReplaceChild("arms",
                CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(40, 38).addBox(-4F, 2F, -2F, 8F, 4F, 4F)
                        .texOffs(29, 79).addBox(-2F, 2F, -2F, 4F, 4F, 4F, new CubeDeformation(0.25F))
                        .texOffs(44, 22).addBox(4F, -2F, -2F, 4F, 8F, 4F)
                        .texOffs(0, 99).addBox(4F, -2F, -2F, 4F, 5F, 4F, new CubeDeformation(0.25F))
                        .texOffs(16, 99).addBox(-8F, -2F, -2F, 4F, 5F, 4F, new CubeDeformation(0.25F))
                        .texOffs(44, 22).addBox(-8F, -2F, -2F, 4F, 8F, 4F)
                        .mirror(false),
                PartPose.offsetAndRotation(0F, 3F, -1F, -0.75F, 0F, 0F));
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

        return LayerDefinition.create(mesh, 64, 128);
    }
}
