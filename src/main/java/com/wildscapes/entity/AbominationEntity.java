package com.wildscapes.entity;

import org.jetbrains.annotations.Nullable;

import com.wildscapes.sound.WildscapesSounds;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.keyframe.event.builtin.AutoPlayingSoundKeyframeHandler;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * "Abomination of the Witches" — a hostile GeckoLib-animated mob (a big witch's toad).
 *
 * <p>Combat: leaps at players from &gt; 10 blocks (dropping a potion-residue cloud on
 * landing), tongue-yanks players within 10 blocks (or reels them out of water), avoids
 * &amp; actively escapes water, is immune to potions and fall damage, and does little
 * direct melee damage. The leap is a four-phase sequence — anticipation ({@code pre_jump}),
 * ascent ({@code jump_up}), peak ({@code intermediate_jump}), descent ({@code jump_down}) —
 * with the physics driven to match each phase.
 */
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

    // Jump phase, synced so the animation matches the physics on the client.
    private static final int ST_NONE = 0, ST_PREJUMP = 1, ST_ASCENT = 2, ST_PEAK = 3, ST_DESCENT = 4;
    private static final EntityDataAccessor<Integer> JUMP_STATE =
            SynchedEntityData.defineId(AbominationEntity.class, EntityDataSerializers.INT);
    // Jump animation playback speed, synced so the client speeds the animation up to
    // match the computed arc.
    private static final EntityDataAccessor<Float> JUMP_SPEED =
            SynchedEntityData.defineId(AbominationEntity.class, EntityDataSerializers.FLOAT);
    // Entity id of the tongue's current victim (-1 = none), synced so the client-side
    // model can aim and stretch the tongue bone at it.
    private static final EntityDataAccessor<Integer> TONGUE_TARGET =
            SynchedEntityData.defineId(AbominationEntity.class, EntityDataSerializers.INT);

    // Jump tuning (20 ticks = 1s). The leap is a real ballistic arc whose flight time
    // scales with distance, and the jump animation is sped up to match so it doesn't
    // hang in the air.
    private static final int PRE_JUMP_LAUNCH = 24;      // ~1.19s into pre_jump = when jump.wav plays
    private static final double JUMP_G = 0.09D;         // per-tick gravity for the arc
    private static final double LEAP_H_MAX = 1.2D;      // horizontal speed cap (enough to reach far targets)
    private static final int T_MIN = 12, T_MAX = 26;    // flight-time bounds (ticks) ≈ 0.6–1.3s
    private static final int INTERMEDIATE_TICKS = 13;   // length of intermediate_jump (~0.67s)
    private static final int REFERENCE_AIR = 20;        // flight time at which anim speed = 1.0

    // Tongue timing (thounge is ~1.25s = 25 ticks). These are synced to the baked tongue
    // reveal in the animation (scaleY peaks at ~0.83s and retracts by ~1.04s, i.e. ticks
    // 14–21). The grab is held until the tongue has fully reached out and is touching the
    // target (~tick 19) so the reel only starts once the tongue has actually caught them —
    // then it reels them in as the tongue retracts, settling ~1 block from the mob.
    private static final int TONGUE_GRAB_TICK = 19;   // tongue has reached the target — catch
    private static final int TONGUE_REEL_END = 23;    // stop reeling (tongue is retracting)
    private static final int TONGUE_END_TICK = 25;    // animation ends
    private static final double TONGUE_HOLD_RADIUS = 1.0D; // desired final distance (blocks)
    private static final double TONGUE_REEL_SPEED = 2.5D;  // max reel step per tick (fast whip)

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
    private int jumpFlightTime;      // computed flight time (ticks) for the current leap
    private double jumpVX, jumpVY0, jumpVZ;
    private int tongueTicks = -1;
    private int croakCooldown = 100;
    private int leapCooldown = 40;
    private int tongueCooldown = 60;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public AbominationEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, 16.0F); // bad swimmer: avoid water
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
    }

    /** Entity id the tongue is currently grabbing, or -1. Used by the client model. */
    public int getTongueTargetId() {
        return this.entityData.get(TONGUE_TARGET);
    }

    private void setTongueTarget(int id) {
        this.entityData.set(TONGUE_TARGET, id);
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

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(0, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // ---- Immunities ----
    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return false; // immune to all potion effects
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false; // never takes fall damage (it leaps constantly)
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            return;
        }

        // Can't swim — bail on any action and scramble back to land.
        if (this.isInWaterOrBubble()) {
            if (this.jumpTicks >= 0 || this.getJumpState() != ST_NONE) {
                this.setNoGravity(false);
                this.jumpTicks = -1;
                this.setJumpState(ST_NONE);
            }
            this.tongueTicks = -1;
            setTongueTarget(-1);
            escapeWater();
            return;
        }

        if (this.getJumpState() != ST_NONE) {
            tickLeap();
            return;
        }
        if (this.tongueTicks >= 0) {
            tickTongue();
            return;
        }

        if (this.leapCooldown > 0) this.leapCooldown--;
        if (this.tongueCooldown > 0) this.tongueCooldown--;

        LivingEntity target = this.getTarget();
        if (target != null) {
            this.getLookControl().setLookAt(target);
            double dx = target.getX() - this.getX();
            double dz = target.getZ() - this.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            boolean los = this.getSensing().hasLineOfSight(target);
            boolean targetInWater = target.isInWaterOrBubble();

            if (targetInWater) {
                // Never leap at a target in water — close in and tongue them out.
                if (los && this.tongueCooldown <= 0 && dist <= 12.0D) {
                    startTongue();
                    this.tongueCooldown = 80 + this.random.nextInt(40);
                }
            } else if (los && this.tongueCooldown <= 0 && dist <= 10.0D) {
                startTongue();
                this.tongueCooldown = 80 + this.random.nextInt(40);
            } else if (los && this.leapCooldown <= 0 && dist > 10.0D && this.onGround()) {
                startLeap();
                this.leapCooldown = 100 + this.random.nextInt(60);
            }
        } else if (this.getDeltaMovement().horizontalDistanceSqr() < 1.0E-4 && --this.croakCooldown <= 0) {
            triggerAnim("special", "croak");
            this.croakCooldown = 200 + this.random.nextInt(100); // 10–15s
        }
    }

    // ---- Leap (four phases: anticipation → ascent → peak → descent) ----
    private void startLeap() {
        this.setJumpState(ST_PREJUMP);
        this.setJumpSpeed(1.0F);   // pre_jump windup plays at normal speed
        this.jumpTicks = 0;
        this.getNavigation().stop();
    }

    private void tickLeap() {
        LivingEntity target = this.getTarget();
        if (target != null) {
            this.getLookControl().setLookAt(target);
        }

        if (this.getJumpState() == ST_PREJUMP) {
            this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D); // wind up in place
            if (++this.jumpTicks >= PRE_JUMP_LAUNCH) {
                launch(target);
                this.jumpTicks = 0;
                this.setJumpState(ST_ASCENT);
            }
            return;
        }

        // Airborne — follow the pre-computed ballistic arc (no artificial hang).
        double vy = this.jumpVY0 - JUMP_G * this.jumpTicks;
        homeHorizontal(target);
        this.setDeltaMovement(this.jumpVX, vy, this.jumpVZ);

        // Animation phase from the arc: rising → apex flourish → falling.
        int ascentTicks = (int) Math.round(this.jumpVY0 / JUMP_G);
        int peakDur = Math.max(2, (int) Math.ceil(INTERMEDIATE_TICKS / this.getJumpSpeed()));
        int phase = this.jumpTicks < ascentTicks ? ST_ASCENT
                : (this.jumpTicks < ascentTicks + peakDur ? ST_PEAK : ST_DESCENT);
        if (this.getJumpState() != phase) {
            this.setJumpState(phase);
        }

        this.jumpTicks++;
        boolean pastApex = this.jumpTicks > ascentTicks + 1;
        if ((this.onGround() && pastApex) || this.jumpTicks > this.jumpFlightTime + 12) {
            this.setNoGravity(false);
            spawnResidue();
            this.jumpTicks = -1;
            this.setJumpState(ST_NONE);
            this.setJumpSpeed(1.0F);
        }
    }

    /** Compute a ballistic arc that lands on the target and set the launch velocity. */
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

        // Flight time scales with distance (short hop vs long leap), then the vertical
        // launch speed is whatever makes the arc land after that many ticks.
        int t = Mth.clamp((int) Math.round(dist * 0.9D), T_MIN, T_MAX);
        this.jumpFlightTime = t;
        this.jumpVY0 = JUMP_G * t / 2.0D;
        double speed = Math.min(dist / t, LEAP_H_MAX);
        this.jumpVX = dist > 1.0E-4 ? dx / dist * speed : 0.0D;
        this.jumpVZ = dist > 1.0E-4 ? dz / dist * speed : 0.0D;

        // Speed the jump animation up so a quick arc doesn't play in slow motion.
        this.setJumpSpeed((float) Mth.clamp((double) REFERENCE_AIR / t, 1.0D, 2.5D));
        this.setDeltaMovement(this.jumpVX, this.jumpVY0, this.jumpVZ);
    }

    /** Re-aim horizontal velocity at the target each tick so the leap lands on them. */
    private void homeHorizontal(@Nullable LivingEntity target) {
        if (target == null) {
            return;
        }
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        int remaining = Math.max(1, this.jumpFlightTime - this.jumpTicks);
        double speed = Math.min(dist / remaining, LEAP_H_MAX); // arrive at the target as it lands
        this.jumpVX = dist > 1.0E-4 ? dx / dist * speed : 0.0D;
        this.jumpVZ = dist > 1.0E-4 ? dz / dist * speed : 0.0D;
    }

    private void spawnResidue() {
        Holder<MobEffect> effect = RESIDUE_EFFECTS[this.random.nextInt(RESIDUE_EFFECTS.length)];
        AreaEffectCloud cloud = new AreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ());
        cloud.setOwner(this);
        cloud.setParticle(ParticleTypes.WITCH);
        cloud.setRadius(6.0F);
        cloud.setDuration(200);
        cloud.setWaitTime(10);
        cloud.setRadiusOnUse(-0.5F);
        cloud.setRadiusPerTick(-cloud.getRadius() / cloud.getDuration());
        cloud.addEffect(new MobEffectInstance(effect, 120, 0));
        this.level().addFreshEntity(cloud);
    }

    // ---- Tongue reel ----
    private void startTongue() {
        // The thounge (mouth-open) animation is driven by the movement controller while
        // a tongue target is set — see movementController().
        this.tongueTicks = 0;
        LivingEntity target = this.getTarget();
        setTongueTarget(target != null ? target.getId() : -1);
        this.getNavigation().stop();
    }

    private void tickTongue() {
        this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D); // mob stays planted while lashing
        LivingEntity target = this.getTarget();
        if (target != null) {
            this.getLookControl().setLookAt(target);
        }
        if (target != null && this.tongueTicks >= TONGUE_GRAB_TICK && this.tongueTicks <= TONGUE_REEL_END) {
            reelTarget(target, this.tongueTicks == TONGUE_GRAB_TICK);
        }
        this.tongueTicks++;
        if (this.tongueTicks >= TONGUE_END_TICK) {
            this.tongueTicks = -1;
            setTongueTarget(-1);
        }
    }

    /**
     * Reel {@code target} toward the mob, decelerating so it settles ~{@link #TONGUE_HOLD_RADIUS}
     * block away instead of overshooting. Sets velocity each tick so it converges (Minecraft
     * won't let a player be positioned to the exact centimetre — the collision boxes keep them
     * a hair over 1 block — but they end up reliably adjacent).
     */
    private void reelTarget(LivingEntity target, boolean firstTick) {
        double dx = this.getX() - target.getX();
        double dz = this.getZ() - target.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        double vy = firstTick && target.onGround() ? 0.3D : target.getDeltaMovement().y; // pop off the ground once
        double overshoot = dist - TONGUE_HOLD_RADIUS;
        if (overshoot > 0.05D && dist > 1.0E-4) {
            double step = Math.min(overshoot, TONGUE_REEL_SPEED);
            target.setDeltaMovement(dx / dist * step, vy, dz / dist * step);
        } else {
            target.setDeltaMovement(0.0D, vy, 0.0D); // arrived — hold at ~1 block
        }
        target.hurtMarked = true; // sync the velocity to the client each tick
        target.hasImpulse = true;
    }

    // ---- Water escape ----
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

    // ---- Gameplay sounds ----
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

    // ---- GeckoLib ----
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
        if (this.getTongueTargetId() >= 0) {
            state.setAnimation(THOUNGE); // mouth-open / lash body motion
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
