package com.wildscapes.entity;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.wildscapes.particle.WildscapesParticles;
import com.wildscapes.sound.WildscapesSounds;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.keyframe.event.builtin.AutoPlayingSoundKeyframeHandler;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AbominationEntity extends Monster implements GeoEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.abomination.static");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.abomination.walk");
    private static final RawAnimation SWIM = RawAnimation.begin().thenLoop("animation.abomination.swim");
    private static final RawAnimation CROAK = RawAnimation.begin().thenPlay("animation.abomination.croak");
    private static final RawAnimation THOUNGE = RawAnimation.begin().thenPlayAndHold("animation.abomination.thounge");
    private static final RawAnimation ENTERTAINMENT =
            RawAnimation.begin().thenPlay("animation.abomination.occasional_entertainment");
    private static final RawAnimation PRE_JUMP = RawAnimation.begin().thenPlayAndHold("animation.abomination.pre_jump");
    private static final RawAnimation JUMP_UP = RawAnimation.begin().thenLoop("animation.abomination.jump_up");
    private static final RawAnimation JUMP_PEAK = RawAnimation.begin().thenPlayAndHold("animation.abomination.intermediate_jump");
    private static final RawAnimation JUMP_DOWN = RawAnimation.begin().thenLoop("animation.abomination.jump_down");

    private static final int ST_NONE = 0, ST_PREJUMP = 1, ST_ASCENT = 2, ST_PEAK = 3, ST_DESCENT = 4;
    private static final EntityDataAccessor<Integer> JUMP_STATE =
            SynchedEntityData.defineId(AbominationEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Float> JUMP_SPEED =
            SynchedEntityData.defineId(AbominationEntity.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Integer> TONGUE_TARGET =
            SynchedEntityData.defineId(AbominationEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> TONGUE_TICKS =
            SynchedEntityData.defineId(AbominationEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<CompoundTag> TONGUE_PATH =
            SynchedEntityData.defineId(AbominationEntity.class, EntityDataSerializers.COMPOUND_TAG);

    private static final int PRE_JUMP_LAUNCH = 17;
    private static final float PREJUMP_TURN = 18.0F;
    private static final double JUMP_G = 0.09D;
    private static final double LEAP_H_MAX = 0.95D;
    private static final int T_MIN = 14, T_MAX = 30;
    private static final int INTERMEDIATE_TICKS = 13;
    private static final int REFERENCE_AIR = 20;
    private static final double LEAP_RANGE = 16.0D;
    private static final float POUNCE_DAMAGE = 12.0F;
    private static final int ARC_STEP = 3;
    private static final int ARC_MAX = 8;

    private static final int TONGUE_SHOOT = 14;
    private static final int TONGUE_GRAB = 21;
    private static final int TONGUE_HOLD_END = 41;
    private static final int TONGUE_END = 47;
    private static final double TONGUE_RANGE = 18.0D;
    private static final double TONGUE_FAR = 12.0D;
    private static final float CHIP_DAMAGE = 2.0F;
    private static final float MOUTH_HOLD = 0.08F;
    private static final double MOUTH_HEIGHT = 1.5D;
    private static final double MOUTH_FORWARD = 0.75D;

    private static final Holder<MobEffect>[] RESIDUE_EFFECTS = residueEffects();

    @SuppressWarnings("unchecked")
    private static Holder<MobEffect>[] residueEffects() {
        return new Holder[] {
                MobEffects.POISON, MobEffects.MOVEMENT_SLOWDOWN, MobEffects.WEAKNESS,
                MobEffects.CONFUSION, MobEffects.BLINDNESS, MobEffects.HUNGER,
                MobEffects.DIG_SLOWDOWN, MobEffects.WITHER
        };
    }

    private int jumpTicks = -1;
    private int jumpFlightTime;
    private double jumpVX, jumpVY0, jumpVZ;
    private boolean pounceLanded;
    private final List<Vec3> arc = new ArrayList<>();
    private int croakCooldown = 100;
    private int leapCooldown = 40;
    private int tongueCooldown = 60;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public AbominationEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, 16.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 28.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(JUMP_STATE, 0);
        builder.define(JUMP_SPEED, 1.0F);
        builder.define(TONGUE_TARGET, -1);
        builder.define(TONGUE_TICKS, -1);
        builder.define(TONGUE_PATH, new CompoundTag());
    }

    public int getTongueTargetId() {
        return this.entityData.get(TONGUE_TARGET);
    }

    private void setTongueTarget(int id) {
        this.entityData.set(TONGUE_TARGET, id);
    }

    public int getTongueTicks() {
        return this.entityData.get(TONGUE_TICKS);
    }

    private int getJumpState() {
        return this.entityData.get(JUMP_STATE);
    }

    private void setJumpState(int state) {
        this.entityData.set(JUMP_STATE, state);
    }

    private float getJumpSpeed() {
        return this.entityData.get(JUMP_SPEED);
    }

    private void setJumpSpeed(float speed) {
        this.entityData.set(JUMP_SPEED, speed);
    }

    public Vec3 mouthPos(float partialTick) {
        double x = Mth.lerp(partialTick, this.xo, this.getX());
        double y = Mth.lerp(partialTick, this.yo, this.getY());
        double z = Mth.lerp(partialTick, this.zo, this.getZ());
        float yaw = Mth.rotLerp(partialTick, this.yBodyRotO, this.yBodyRot) * Mth.DEG_TO_RAD;
        return new Vec3(x - Mth.sin(yaw) * MOUTH_FORWARD, y + MOUTH_HEIGHT, z + Mth.cos(yaw) * MOUTH_FORWARD);
    }

    public List<Vec3> tonguePath() {
        int[] v = this.entityData.get(TONGUE_PATH).getIntArray("p");
        if (v.length < 6) {
            return List.of();
        }
        List<Vec3> out = new ArrayList<>(v.length / 3);
        for (int i = 0; i < v.length; i += 3) {
            out.add(new Vec3(v[i] / 16.0D, v[i + 1] / 16.0D, v[i + 2] / 16.0D));
        }
        return out;
    }

    public boolean isGrasping() {
        int ticks = getTongueTicks();
        return ticks >= TONGUE_GRAB && ticks < TONGUE_END;
    }

    public float tongueReach(float partialTick) {
        int ticks = getTongueTicks();
        if (ticks < 0) {
            return 0.0F;
        }
        float t = ticks + partialTick;
        if (t <= TONGUE_SHOOT) {
            return 0.0F;
        }
        if (t < TONGUE_GRAB) {
            return (t - TONGUE_SHOOT) / (TONGUE_GRAB - TONGUE_SHOOT);
        }
        if (t < TONGUE_HOLD_END) {
            return Mth.lerp((t - TONGUE_GRAB) / (TONGUE_HOLD_END - TONGUE_GRAB), 1.0F, MOUTH_HOLD);
        }
        return Mth.lerp(Math.min(1.0F, (t - TONGUE_HOLD_END) / (TONGUE_END - TONGUE_HOLD_END)), MOUTH_HOLD, 0.0F);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(0, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return false;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            return;
        }

        boolean wet = this.isInWaterOrBubble();
        if (wet && this.getJumpState() != ST_NONE) {
            endLeap();
        }

        if (this.getJumpState() != ST_NONE) {
            tickLeap();
            return;
        }
        if (getTongueTicks() >= 0) {
            tickTongue();
            return;
        }

        if (this.leapCooldown > 0) this.leapCooldown--;
        if (this.tongueCooldown > 0) this.tongueCooldown--;

        LivingEntity target = this.getTarget();
        if (target == null) {
            if (wet) {
                escapeWater();
            } else if (this.getDeltaMovement().horizontalDistanceSqr() < 1.0E-4 && --this.croakCooldown <= 0) {
                triggerAnim("special", "croak");
                this.croakCooldown = 200 + this.random.nextInt(100);
            }
            return;
        }

        this.getLookControl().setLookAt(target);
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        boolean los = this.getSensing().hasLineOfSight(target);

        if (los && this.tongueCooldown <= 0 && wantsTongue(target, dist)) {
            startTongue(target);
            return;
        }
        if (!wet && los && this.leapCooldown <= 0 && this.onGround() && dist <= LEAP_RANGE) {
            startLeap();
            this.leapCooldown = 80 + this.random.nextInt(40);
        } else if (wet) {
            escapeWater();
        }
    }

    private boolean wantsTongue(LivingEntity target, double dist) {
        if (dist > TONGUE_RANGE) {
            return false;
        }
        if (target.isInWaterOrBubble() || dist > TONGUE_FAR || this.isInWaterOrBubble()) {
            return true;
        }
        return baitable(target);
    }

    private boolean baitable(LivingEntity target) {
        List<ResidueCloud> clouds = this.level().getEntitiesOfClass(ResidueCloud.class,
                this.getBoundingBox().inflate(10.0D));
        if (clouds.isEmpty()) {
            return false;
        }
        for (ResidueCloud cloud : clouds) {
            double r = cloud.getRadius();
            if (target.distanceToSqr(cloud.getX(), target.getY(), cloud.getZ()) <= r * r) {
                return false;
            }
        }
        return true;
    }

    private void startLeap() {
        this.setJumpState(ST_PREJUMP);
        this.setJumpSpeed(1.0F);
        this.jumpTicks = 0;
        this.pounceLanded = false;
        this.arc.clear();
        this.getNavigation().stop();
    }

    private void endLeap() {
        this.setNoGravity(false);
        this.jumpTicks = -1;
        this.setJumpState(ST_NONE);
        this.setJumpSpeed(1.0F);
        this.arc.clear();
    }

    private void tickLeap() {
        LivingEntity target = this.getTarget();
        if (target != null) {
            this.getLookControl().setLookAt(target);
        }

        if (this.getJumpState() == ST_PREJUMP) {
            this.getNavigation().stop();
            this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
            faceTarget(target);
            if (++this.jumpTicks >= PRE_JUMP_LAUNCH) {
                launch(target);
                this.jumpTicks = 0;
                this.setJumpState(ST_ASCENT);
            }
            return;
        }

        double vy = this.jumpVY0 - JUMP_G * this.jumpTicks;
        homeHorizontal(target);
        this.setDeltaMovement(this.jumpVX, vy, this.jumpVZ);

        int ascentTicks = (int) Math.round(this.jumpVY0 / JUMP_G);
        int peakDur = Math.max(2, (int) Math.ceil(INTERMEDIATE_TICKS / this.getJumpSpeed()));
        int phase = this.jumpTicks < ascentTicks ? ST_ASCENT
                : (this.jumpTicks < ascentTicks + peakDur ? ST_PEAK : ST_DESCENT);
        if (this.getJumpState() != phase) {
            this.setJumpState(phase);
        }

        if (this.jumpTicks % ARC_STEP == 0 && this.arc.size() < ARC_MAX) {
            this.arc.add(this.position());
        }
        slam(target);

        this.jumpTicks++;
        boolean pastApex = this.jumpTicks > ascentTicks + 1;
        if ((this.onGround() && pastApex) || this.jumpTicks > this.jumpFlightTime + 12) {
            spawnResidue();
            endLeap();
        }
    }

    private void faceTarget(@Nullable LivingEntity target) {
        if (target == null) {
            return;
        }
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        float want = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
        this.setYRot(Mth.approachDegrees(this.getYRot(), want, PREJUMP_TURN));
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.getYRot();
    }

    private void slam(@Nullable LivingEntity target) {
        if (this.pounceLanded || target == null
                || !this.getBoundingBox().inflate(0.25D).intersects(target.getBoundingBox())) {
            return;
        }
        this.pounceLanded = true;
        target.hurt(this.damageSources().mobAttack(this), POUNCE_DAMAGE);
        Vec3 push = new Vec3(this.jumpVX, 0.0D, this.jumpVZ).normalize().scale(0.6D);
        target.push(push.x, 0.35D, push.z);
        target.hurtMarked = true;
    }

    private void launch(@Nullable LivingEntity target) {
        this.setNoGravity(true);
        this.getNavigation().stop();

        double dx, dz;
        if (target != null) {
            dx = target.getX() - this.getX();
            dz = target.getZ() - this.getZ();
        } else {
            Vec3 look = this.getLookAngle();
            dx = look.x;
            dz = look.z;
        }
        double dist = Math.sqrt(dx * dx + dz * dz);

        int t = Mth.clamp((int) Math.round(dist * 1.15D), T_MIN, T_MAX);
        this.jumpFlightTime = t;
        this.jumpVY0 = JUMP_G * t / 2.0D;
        double speed = Math.min(dist / t, LEAP_H_MAX);
        this.jumpVX = dist > 1.0E-4 ? dx / dist * speed : 0.0D;
        this.jumpVZ = dist > 1.0E-4 ? dz / dist * speed : 0.0D;

        this.setJumpSpeed((float) Mth.clamp((double) REFERENCE_AIR / t, 1.0D, 2.5D));
        this.setDeltaMovement(this.jumpVX, this.jumpVY0, this.jumpVZ);

        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(WildscapesParticles.RESIDUE_SWIRL.get(), this.getX(), this.getY() + 0.4D, this.getZ(),
                    24, 0.7D, 0.3D, 0.7D, 0.06D);
        }
    }

    private void homeHorizontal(@Nullable LivingEntity target) {
        if (target == null) {
            return;
        }
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        int remaining = Math.max(1, this.jumpFlightTime - this.jumpTicks);
        double speed = Math.min(dist / remaining, LEAP_H_MAX);
        this.jumpVX = dist > 1.0E-4 ? dx / dist * speed : 0.0D;
        this.jumpVZ = dist > 1.0E-4 ? dz / dist * speed : 0.0D;
    }

    private void spawnResidue() {
        Holder<MobEffect> effect = RESIDUE_EFFECTS[this.random.nextInt(RESIDUE_EFFECTS.length)];
        for (Vec3 sample : this.arc) {
            BlockPos ground = groundUnder(sample);
            if (ground != null) {
                cloud(ground.getX() + 0.5D, ground.getY() + 1.0D, ground.getZ() + 0.5D, 2.2F, 140, effect);
            }
        }
        cloud(this.getX(), this.getY(), this.getZ(), 6.0F, 200, effect);

        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(WildscapesParticles.RESIDUE_SPLAT.get(), this.getX(), this.getY() + 0.15D, this.getZ(),
                    30, 1.4D, 0.1D, 1.4D, 0.12D);
        }
        this.arc.clear();
    }

    private void cloud(double x, double y, double z, float radius, int duration, Holder<MobEffect> effect) {
        ResidueCloud cloud = new ResidueCloud(this.level(), x, y, z);
        cloud.setOwner(this);
        cloud.setParticle(WildscapesParticles.RESIDUE_WISP.get());
        cloud.setRadius(radius);
        cloud.setDuration(duration);
        cloud.setWaitTime(10);
        cloud.setRadiusOnUse(-0.5F);
        cloud.setRadiusPerTick(-radius / duration);
        cloud.addEffect(new MobEffectInstance(effect, 120, 0));
        this.level().addFreshEntity(cloud);
    }

    @Nullable
    private BlockPos groundUnder(Vec3 from) {
        Vec3 to = from.subtract(0.0D, 6.0D, 0.0D);
        HitResult hit = this.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
                CollisionContext.empty()));
        return hit.getType() == HitResult.Type.MISS ? null : BlockPos.containing(hit.getLocation()).below();
    }

    private void startTongue(LivingEntity target) {
        List<Vec3> path = TonguePath.find(this.level(), mouthPos(1.0F), target.getBoundingBox().getCenter());
        if (path == null) {
            this.tongueCooldown = 30;
            return;
        }
        storePath(path);
        this.entityData.set(TONGUE_TICKS, 0);
        setTongueTarget(target.getId());
        this.tongueCooldown = 120 + this.random.nextInt(60);
        this.getNavigation().stop();
    }

    private void storePath(List<Vec3> path) {
        int[] v = new int[path.size() * 3];
        for (int i = 0; i < path.size(); i++) {
            Vec3 p = path.get(i);
            v[i * 3] = (int) Math.round(p.x * 16.0D);
            v[i * 3 + 1] = (int) Math.round(p.y * 16.0D);
            v[i * 3 + 2] = (int) Math.round(p.z * 16.0D);
        }
        CompoundTag tag = new CompoundTag();
        tag.putIntArray("p", v);
        this.entityData.set(TONGUE_PATH, tag);
    }

    private void endTongue() {
        this.entityData.set(TONGUE_TICKS, -1);
        this.entityData.set(TONGUE_PATH, new CompoundTag());
        setTongueTarget(-1);
    }

    private void tickTongue() {
        this.getNavigation().stop();
        this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);

        int ticks = getTongueTicks();
        if (!(this.level().getEntity(getTongueTargetId()) instanceof LivingEntity target) || !target.isAlive()) {
            endTongue();
            return;
        }

        this.getLookControl().setLookAt(target);
        if (ticks >= TONGUE_GRAB && ticks < TONGUE_END) {
            haul(target, ticks);
        }

        this.entityData.set(TONGUE_TICKS, ticks + 1);
        if (ticks + 1 >= TONGUE_END) {
            endTongue();
        }
    }

    private void haul(LivingEntity target, int ticks) {
        List<Vec3> path = tonguePath();
        if (path.size() < 2) {
            return;
        }
        Vec3 want = TonguePath.pointAt(path, Math.max(MOUTH_HOLD, tongueReach(1.0F)));
        Vec3 delta = want.subtract(target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D));
        target.setDeltaMovement(delta.scale(0.8D));
        target.hurtMarked = true;
        target.hasImpulse = true;
        target.fallDistance = 0.0F;

        if (ticks == TONGUE_GRAB) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, TONGUE_END - TONGUE_GRAB, 6,
                    false, false, false));
        } else if (ticks == TONGUE_GRAB + 1 || ticks == TONGUE_HOLD_END - 1) {
            target.hurt(this.damageSources().mobAttack(this), CHIP_DAMAGE);
        }
    }

    private void escapeWater() {
        BlockPos land = findNearestLand();
        if (land != null) {
            this.getNavigation().stop();
            this.getLookControl().setLookAt(land.getX() + 0.5, land.getY(), land.getZ() + 0.5);
            this.getMoveControl().setWantedPosition(land.getX() + 0.5, land.getY(), land.getZ() + 0.5, 1.3D);
        }
    }

    @Nullable
    private BlockPos findNearestLand() {
        BlockPos origin = this.blockPosition();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        BlockPos best = null;
        double bestDistSq = Double.MAX_VALUE;
        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                for (int dy = 4; dy >= -3; dy--) {
                    m.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    if (isDryStandable(m)) {
                        double distSq = origin.distSqr(m);
                        if (distSq < bestDistSq) {
                            bestDistSq = distSq;
                            best = m.immutable();
                        }
                        break;
                    }
                }
            }
        }
        return best;
    }

    private boolean isDryStandable(BlockPos pos) {
        Level level = this.level();
        if (!level.getFluidState(pos).isEmpty() || level.getBlockState(pos).blocksMotion()) {
            return false;
        }
        BlockState below = level.getBlockState(pos.below());
        return level.getFluidState(pos.below()).isEmpty()
                && below.isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return WildscapesSounds.ABOMINATION_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return WildscapesSounds.ABOMINATION_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return WildscapesSounds.ABOMINATION_DEATH.get();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 5, this::movementController)
                .setAnimationSpeedHandler(a -> a.getJumpState() != ST_NONE ? (double) a.getJumpSpeed() : 1.0D)
                .setSoundKeyframeHandler(new AutoPlayingSoundKeyframeHandler<>()));

        controllers.add(new AnimationController<AbominationEntity>(this, "special", 0, state -> PlayState.STOP)
                .triggerableAnim("croak", CROAK)
                .triggerableAnim("entertainment", ENTERTAINMENT)
                .setSoundKeyframeHandler(new AutoPlayingSoundKeyframeHandler<>()));
    }

    private PlayState movementController(AnimationState<AbominationEntity> state) {
        if (this.isInWaterOrBubble()) {
            state.setAnimation(SWIM);
            return PlayState.CONTINUE;
        }
        if (this.getJumpState() != ST_NONE) {
            switch (this.getJumpState()) {
                case ST_PREJUMP -> state.setAnimation(PRE_JUMP);
                case ST_ASCENT -> state.setAnimation(JUMP_UP);
                case ST_PEAK -> state.setAnimation(JUMP_PEAK);
                default -> state.setAnimation(JUMP_DOWN);
            }
            return PlayState.CONTINUE;
        }
        if (getTongueTicks() >= 0) {
            state.setAnimation(THOUNGE);
            return PlayState.CONTINUE;
        }
        state.setAnimation(state.isMoving() ? WALK : IDLE);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
