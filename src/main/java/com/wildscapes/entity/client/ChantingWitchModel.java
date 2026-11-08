package com.wildscapes.entity.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.witch.WitchModel;
import net.minecraft.client.renderer.entity.state.WitchRenderState;
import net.minecraft.util.Mth;

public class ChantingWitchModel extends WitchModel {
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
    public void setupAnim(WitchRenderState state) {
        super.setupAnim(state);
        boolean chanting = state.getDataOrDefault(MobVisuals.CHANTING, false);
        this.arms.visible = !chanting;
        this.rightArm.visible = chanting;
        this.leftArm.visible = chanting;
        if (!chanting) {
            return;
        }
        float sway = Mth.cos(state.ageInTicks * 0.6662F) * 0.25F;
        this.rightArm.xRot = sway;
        this.leftArm.xRot = sway;
        this.rightArm.zRot = Mth.PI * 0.75F;
        this.leftArm.zRot = -Mth.PI * 0.75F;
        this.getHead().xRot = 0.35F;
    }
}
