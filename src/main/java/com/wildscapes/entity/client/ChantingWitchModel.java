package com.wildscapes.entity.client;

import com.wildscapes.entity.WitchRitual;

import net.minecraft.client.model.WitchModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Witch;

public class ChantingWitchModel extends WitchModel<Witch> {
    private final ModelPart arms;
    private final ModelPart rightArm;
    private final ModelPart leftArm;

    public ChantingWitchModel(ModelPart root) {
        super(root);
        this.arms = root.getChild("arms");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
    }

    @Override
    public void setupAnim(Witch witch, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(witch, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        boolean chanting = WitchRitual.isChanting(witch);
        this.arms.visible = !chanting;
        this.rightArm.visible = chanting;
        this.leftArm.visible = chanting;
        if (!chanting) {
            return;
        }
        float sway = Mth.cos(ageInTicks * 0.6662F) * 0.25F;
        this.rightArm.xRot = sway;
        this.leftArm.xRot = sway;
        this.rightArm.zRot = Mth.PI * 0.75F;
        this.leftArm.zRot = -Mth.PI * 0.75F;
        this.getHead().xRot = 0.35F;
    }
}
