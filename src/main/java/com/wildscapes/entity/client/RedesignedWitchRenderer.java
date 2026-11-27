package com.wildscapes.entity.client;

import com.wildscapes.Wildscapes;
import com.wildscapes.entity.SoulHarvest;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.WitchRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Witch;

public class RedesignedWitchRenderer extends WitchRenderer {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Wildscapes.MODID, "textures/entity/witch.png");
    private static final ResourceLocation EMPOWERED =
            new ResourceLocation(Wildscapes.MODID, "textures/entity/witch_empowered.png");
    private static final ResourceLocation VANILLA = new ResourceLocation("textures/entity/witch.png");

    private final boolean vanilla;

    public RedesignedWitchRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.vanilla = MobLook.vanilla(context, "witch", WildscapesModelLayers.WITCH, VANILLA);
        if (!vanilla) {
            this.model = new ChantingWitchModel(context.bakeLayer(WildscapesModelLayers.WITCH));
            this.addLayer(new EmpoweredGlowLayer<>(this, "witch_empowered_glow"));
        }
    }

    @Override
    public ResourceLocation getTextureLocation(Witch entity) {
        if (vanilla) {
            return VANILLA;
        }
        return SoulHarvest.isEmpowered(entity) ? EMPOWERED : TEXTURE;
    }
}
