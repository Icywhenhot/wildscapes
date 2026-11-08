package com.wildscapes.entity.client;

import java.util.Map;
import java.util.WeakHashMap;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import com.wildscapes.Wildscapes;

import net.minecraft.client.model.monster.illager.IllagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.entity.state.IllusionerRenderState;
import net.minecraft.world.entity.monster.illager.Illusioner;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class RedesignedIllusionerRenderer extends IllagerRenderer<Illusioner, IllusionerRenderState> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Wildscapes.MODID, "textures/entity/illager/illusioner.png");
    private static final Identifier EMPOWERED =
            Identifier.fromNamespaceAndPath(Wildscapes.MODID, "textures/entity/illager/illusioner_empowered.png");

    private final Map<Illusioner, Trail> trails = new WeakHashMap<>();

    public RedesignedIllusionerRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerModel<>(context.bakeLayer(WildscapesModelLayers.ILLUSIONER)), 0.5F);
        this.addLayer(new ItemInHandLayer<>(this) {
            @Override
            public void submit(PoseStack pose, SubmitNodeCollector nodes, int light, IllusionerRenderState state, float yaw, float pitch) {
                if (state.isCastingSpell || state.isAggressive) {
                    super.submit(pose, nodes, light, state, yaw, pitch);
                }
            }
        });
        this.model.getHat().visible = true;
        this.addLayer(new EmpoweredGlowLayer<>(this, "illager/illusioner_empowered_glow"));
    }

    @Override
    public IllusionerRenderState createRenderState() {
        return new IllusionerRenderState();
    }

    @Override
    public Identifier getTextureLocation(IllusionerRenderState state) {
        return state.getDataOrDefault(MobVisuals.EMPOWERED, false) ? EMPOWERED : TEXTURE;
    }

    @Override
    protected boolean isBodyVisible(IllusionerRenderState state) {
        return true;
    }

    @Override
    public void extractRenderState(Illusioner entity, IllusionerRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.isCastingSpell = entity.isCastingSpell();
        state.illusionOffsets = new Vec3[0];
        if (!entity.isInvisible()) {
            trails.remove(entity);
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

        state.illusionOffsets = new Vec3[offs.length];
        for (int i = 0; i < offs.length; i++) {
            if (i == tr.real) {
                tr.y[i] = 0.0;
                state.illusionOffsets[i] = Vec3.ZERO;
                continue;
            }

            double dx = offs[i].x - offs[tr.real].x;
            double dz = offs[i].z - offs[tr.real].z;

            double g = groundAt(entity, entity.getX() + dx, entity.getZ() + dz);
            double want = Double.isNaN(g) || Double.isNaN(base) ? 0.0 : g - base;
            tr.y[i] += (want - tr.y[i]) * a;

            state.illusionOffsets[i] = new Vec3(dx, tr.y[i], dz);
        }

        tr.seen = true;
    }

    @Override
    public void submit(IllusionerRenderState state, PoseStack pose, SubmitNodeCollector nodes, CameraRenderState camera) {
        if (state.illusionOffsets.length == 0) {
            super.submit(state, pose, nodes, camera);
            return;
        }

        for (Vec3 offset : state.illusionOffsets) {
            pose.pushPose();
            pose.translate(offset.x, offset.y, offset.z);
            super.submit(state, pose, nodes, camera);
            pose.popPose();
        }
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
