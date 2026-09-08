package com.wildscapes.entity.client;

import java.util.Map;
import java.util.WeakHashMap;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wildscapes.Wildscapes;

import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class RedesignedIllusionerRenderer extends IllagerRenderer<Illusioner> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "textures/entity/illager/illusioner.png");

    private final Map<Illusioner, Trail> trails = new WeakHashMap<>();

    public RedesignedIllusionerRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerModel<>(context.bakeLayer(WildscapesModelLayers.ILLUSIONER)), 0.5F);
        this.addLayer(new ItemInHandLayer<Illusioner, IllagerModel<Illusioner>>(this, context.getItemInHandRenderer()) {
            @Override
            public void render(PoseStack pose, MultiBufferSource buf, int light, Illusioner entity, float limbSwing,
                    float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
                if (entity.isCastingSpell() || entity.isAggressive()) {
                    super.render(pose, buf, light, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
                }
            }
        });
        this.model.getHat().visible = true;
    }

    @Override
    public ResourceLocation getTextureLocation(Illusioner entity) {
        return TEXTURE;
    }

    @Override
    protected boolean isBodyVisible(Illusioner entity) {
        return true;
    }

    @Override
    public void render(Illusioner entity, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buf, int light) {
        if (!entity.isInvisible()) {
            trails.remove(entity);
            super.render(entity, yaw, partialTicks, pose, buf, light);
            return;
        }

        Vec3[] offs = entity.getIllusionOffsets(partialTicks);
        double base = groundAt(entity, entity.getX(), entity.getZ());

        Trail tr = this.trails.computeIfAbsent(entity, e -> new Trail());
        if (!tr.seen || entity.hurtTime > tr.hurtTime) {
            tr.real = entity.getRandom().nextInt(offs.length);
        }
        tr.hurtTime = entity.hurtTime;
        boolean spread = false;
        for (Vec3 offset : offs) {
            spread |= offset.lengthSqr() > 1.0E-6;
        }
        if (!spread) {
            offs = new Vec3[] {new Vec3(-1.5, 0.0, -1.5), new Vec3(1.5, 0.0, -1.5),
                    new Vec3(1.5, 0.0, 1.5), new Vec3(-1.5, 0.0, 1.5)};
        }
        float now = entity.tickCount + partialTicks;
        double a = tr.seen ? 1.0 - Math.pow(0.55, Math.max(0.0F, now - tr.t)) : 1.0;
        tr.t = now;

        for (int i = 0; i < offs.length; i++) {
            if (i == tr.real) {
                tr.y[i] = 0.0;
                super.render(entity, yaw, partialTicks, pose, buf, light);
                continue;
            }

            double dx = offs[i].x - offs[tr.real].x;
            double dz = offs[i].z - offs[tr.real].z;

            double g = groundAt(entity, entity.getX() + dx, entity.getZ() + dz);
            double want = Double.isNaN(g) || Double.isNaN(base) ? 0.0 : g - base;
            tr.y[i] += (want - tr.y[i]) * a;

            pose.pushPose();
            pose.translate(dx, tr.y[i], dz);
            super.render(entity, yaw, partialTicks, pose, buf, light);
            pose.popPose();
        }

        tr.seen = true;
    }

    private static final class Trail {
        final double[] y = new double[4];
        boolean seen;
        int real;
        int hurtTime;
        float t;
    }

    private static double groundAt(Illusioner e, double x, double z) {
        Vec3 from = new Vec3(x, e.getY() + 4.0, z);
        Vec3 to = new Vec3(x, e.getY() - 8.0, z);
        BlockHitResult hit = e.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, e));
        return hit.getType() == HitResult.Type.MISS ? Double.NaN : hit.getLocation().y;
    }
}
