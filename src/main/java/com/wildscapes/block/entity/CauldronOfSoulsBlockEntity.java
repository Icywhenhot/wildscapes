package com.wildscapes.block.entity;

import net.minecraft.world.entity.EntityTypes;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.wildscapes.Wildscapes;
import com.wildscapes.WildscapesAdvancements;
import com.wildscapes.block.CauldronOfSoulsBlock;
import com.wildscapes.block.SummoningBonfireBlock;
import com.wildscapes.effect.WildscapesEffects;
import com.wildscapes.entity.AbominationEntity;
import com.wildscapes.entity.IncendiaryBottle;
import com.wildscapes.entity.SoulHarvest;
import com.wildscapes.entity.WildscapesEntities;
import com.wildscapes.particle.SoulTrailOptions;
import com.wildscapes.particle.WildscapesParticles;
import com.wildscapes.sound.WildscapesSounds;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class CauldronOfSoulsBlockEntity extends BlockEntity {
    public static final ResourceKey<LootTable> REWARDS = ResourceKey.create(Registries.LOOT_TABLE,
            Identifier.fromNamespaceAndPath(Wildscapes.MODID, "gameplay/cauldron_of_souls"));

    private static final ResourceKey<LootTable> LOW_REWARDS = ResourceKey.create(Registries.LOOT_TABLE,
            Identifier.fromNamespaceAndPath(Wildscapes.MODID, "gameplay/soul_harvest_low"));
    private static final ResourceKey<LootTable> HIGH_REWARDS = ResourceKey.create(Registries.LOOT_TABLE,
            Identifier.fromNamespaceAndPath(Wildscapes.MODID, "gameplay/soul_harvest_high"));

    private static final Map<ResourceKey<Level>, Set<BlockPos>> LOADED = new HashMap<>();

    private static final int RADIUS = 48;
    private static final int TRIGGER_RANGE = 16;
    private static final int WINDUP = 300;
    private static final int ROTATE = 300;
    private static final int GIVE_UP = 600;
    private static final int CLOSE_DELAY = 60;
    private static final int BAR_LINGER = 100;
    private static final int CAP = 15;

    private static final float[] ABOMINATION_AT = {0.15F, 0.5F};
    private static final float[][] MOB_WORTH = {
            {0.10F, 0.20F}, {0.08F, 0.16F}, {0.06F, 0.12F}, {0.05F, 0.10F}, {0.04F, 0.08F}};
    private static final float[][] ABOMINATION_WORTH = {
            {0.30F, 0.60F}, {0.25F, 0.60F}, {0.25F, 0.50F}, {0.25F, 0.50F}, {0.20F, 0.40F}};
    private static final int REFILL = 5;
    private static final int ABOMINATION_SEARCH = 96;

    private enum Stage { IDLE, WINDUP, RAID, REWARDING, CLOSING }

    private Stage stage = Stage.IDLE;
    private int omen;
    private int timer;
    private float filled;
    private int rotateIn;
    private int away;
    private int sinceEmpowered;
    private int abominationsDue;
    private int abominationsSent;
    private int ejectIn;
    private boolean paused;
    private final List<BlockPos> bonfires = new ArrayList<>();
    private final List<BlockPos> lit = new ArrayList<>();
    private final Map<BlockPos, Integer> spawnIn = new HashMap<>();
    private final List<UUID> mobs = new ArrayList<>();
    private final IntArrayList arrivals = new IntArrayList();
    private final FloatArrayList worth = new FloatArrayList();
    private final Set<UUID> participants = new HashSet<>();
    private final Deque<ItemStack> loot = new ArrayDeque<>();

    @Nullable
    private ServerBossEvent bar;
    private int barLinger;
    private boolean humming;
    private float lid;
    private float lidO;
    private boolean lidSet;

    public CauldronOfSoulsBlockEntity(BlockPos pos, BlockState state) {
        super(WildscapesBlockEntities.CAULDRON_OF_SOULS, pos, state);
    }

    @Nullable
    public static CauldronOfSoulsBlockEntity harvesting(ServerLevel level, LivingEntity dead) {
        EntityType<?> type = dead.getType();
        if (type != EntityTypes.PILLAGER && type != EntityTypes.VINDICATOR && type != EntityTypes.WITCH
                && type != EntityTypes.EVOKER && type != EntityTypes.ILLUSIONER && type != WildscapesEntities.ABOMINATION) {
            return null;
        }
        Set<BlockPos> here = LOADED.get(level.dimension());
        if (here == null) {
            return null;
        }
        for (BlockPos pos : here) {
            if (dead.distanceToSqr(Vec3.atCenterOf(pos)) <= RADIUS * RADIUS
                    && level.getBlockEntity(pos) instanceof CauldronOfSoulsBlockEntity be && be.stage == Stage.RAID) {
                return be;
            }
        }
        return null;
    }

    public static void tryStart(ServerLevel level, Player player) {
        Set<BlockPos> here = LOADED.get(level.dimension());
        if (here == null || player.isSpectator()) {
            return;
        }
        for (BlockPos pos : here) {
            if (pos.distSqr(player.blockPosition()) > TRIGGER_RANGE * TRIGGER_RANGE) {
                continue;
            }
            if (level.getBlockEntity(pos) instanceof CauldronOfSoulsBlockEntity be && be.stage == Stage.IDLE) {
                be.start(level, player);
                return;
            }
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CauldronOfSoulsBlockEntity be) {
        be.tick((ServerLevel) level);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, CauldronOfSoulsBlockEntity be) {
        float target = switch (state.getValue(CauldronOfSoulsBlock.PHASE)) {
            case INACTIVE -> 0.0F;
            case ACTIVE -> 45.0F;
            case REWARDS -> 67.5F;
        };
        be.lidO = be.lid;
        if (!be.lidSet) {
            be.lid = be.lidO = target;
            be.lidSet = true;
        } else if (Math.abs(target - be.lid) < 0.3F) {
            be.lid = target;
        } else {
            be.lid += (target - be.lid) * 0.15F;
        }
        if (!be.humming) {
            be.humming = true;
            com.wildscapes.entity.client.SoulCauldronHum.start(be);
        }
    }

    public float lidAngle(float partialTick) {
        return Mth.lerp(partialTick, lidO, lid);
    }

    public void stopHumming() {
        humming = false;
    }

    private Vec3 center() {
        return Vec3.atCenterOf(worldPosition);
    }

    private void start(ServerLevel level, Player player) {
        MobEffectInstance effect = player.getEffect(MobEffects.BAD_OMEN);
        omen = Mth.clamp(effect == null ? 1 : effect.getAmplifier() + 1, 1, 5);
        player.removeEffect(MobEffects.BAD_OMEN);
        filled = 0.0F;
        arrivals.clear();
        worth.clear();
        mobs.clear();
        participants.clear();
        participants.add(player.getUUID());
        lit.clear();
        spawnIn.clear();
        sinceEmpowered = 0;
        away = 0;
        paused = false;
        findBonfires(level);

        stage = Stage.WINDUP;
        timer = 0;
        setPhase(CauldronOfSoulsBlock.Phase.ACTIVE);
        for (BlockPos b : bonfires) {
            SummoningBonfireBlock.setLit(level, b, true);
        }
        Vec3 c = center();
        level.playSound(null, worldPosition, WildscapesSounds.CAULDRON_OF_SOULS_ACTIVATE, SoundSource.BLOCKS, 6.0F, 1.0F);
        level.sendParticles(WildscapesParticles.SOUL_HARVEST_SPARK, c.x, c.y + 0.6, c.z, 30, 0.5, 0.6, 0.5, 0.08);

        bar = newBar();
        bar.setProgress(0.0F);
        barLinger = 0;
        updatePlayers(level);
        WildscapesAdvancements.award(player, "its_time_to_harvest");
        setChanged();
    }

    private void findBonfires(ServerLevel level) {
        bonfires.clear();
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int dx = -40; dx <= 40; dx++) {
            for (int dz = -40; dz <= 40; dz++) {
                for (int dy = -12; dy <= 12; dy++) {
                    p.set(worldPosition.getX() + dx, worldPosition.getY() + dy, worldPosition.getZ() + dz);
                    if (level.getBlockState(p).getBlock() instanceof SummoningBonfireBlock) {
                        bonfires.add(p.immutable());
                    }
                }
            }
        }
    }

    private ServerBossEvent newBar() {
        return new ServerBossEvent(UUID.randomUUID(), Component.translatable("event.wildscapes.soul_harvest"),
                BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
    }

    private void tick(ServerLevel level) {
        if (barLinger > 0 && --barLinger == 0 && bar != null) {
            bar.removeAllPlayers();
            bar = null;
        }
        if ((stage == Stage.WINDUP || stage == Stage.RAID) && bar == null) {
            bar = newBar();
        }
        switch (stage) {
            case WINDUP -> windup(level);
            case RAID -> raid(level);
            case REWARDING -> eject(level);
            case CLOSING -> {
                if (--timer <= 0) {
                    stage = Stage.IDLE;
                    setPhase(CauldronOfSoulsBlock.Phase.INACTIVE);
                    setChanged();
                }
            }
            default -> {
            }
        }
    }

    private void windup(ServerLevel level) {
        timer++;
        bar.setProgress(Math.min(1.0F, timer / (float) WINDUP));
        if (timer % 20 == 0) {
            updatePlayers(level);
        }
        if (timer < WINDUP) {
            return;
        }
        stage = Stage.RAID;
        timer = 0;
        bar.setProgress(0.0F);
        for (BlockPos b : bonfires) {
            SummoningBonfireBlock.setLit(level, b, false);
        }
        abominationsSent = 0;
        int wanted = omen >= 4 && level.getRandom().nextBoolean() ? 2 : 1;
        int already = level.getEntitiesOfClass(AbominationEntity.class, new AABB(worldPosition).inflate(ABOMINATION_SEARCH),
                AbominationEntity::isAlive).size();
        abominationsDue = Math.max(0, wanted - already);
        pickBonfires(level);
        setChanged();
    }

    private void raid(ServerLevel level) {
        timer++;
        if (timer % 20 == 0) {
            away = updatePlayers(level) > 0 ? 0 : away + 20;
            if (away >= GIVE_UP) {
                fail(level);
                return;
            }
            mobs.removeIf(id -> !(level.getEntity(id) instanceof Mob m) || !m.isAlive());
        }

        for (int i = arrivals.size() - 1; i >= 0; i--) {
            int left = arrivals.getInt(i) - 1;
            if (left > 0) {
                arrivals.set(i, left);
                continue;
            }
            arrivals.removeInt(i);
            filled += worth.removeFloat(i);
            Vec3 c = center();
            level.sendParticles(WildscapesParticles.CAULDRON_SWIRL, c.x, c.y + 0.8, c.z, 8, 0.3, 0.2, 0.3, 0.04);
            level.sendParticles(WildscapesParticles.SOUL_HARVEST_SPARK, c.x, c.y + 0.8, c.z, 12, 0.4, 0.3, 0.4, 0.06);
            level.playSound(null, worldPosition, SoundEvents.SOUL_ESCAPE.value(), SoundSource.BLOCKS, 2.0F, 0.8F);
            setChanged();
        }
        bar.setProgress(Math.min(1.0F, filled));
        if (filled >= 0.999F) {
            win(level);
            return;
        }

        float pending = filled;
        for (int i = 0; i < worth.size(); i++) {
            pending += worth.getFloat(i);
        }
        boolean full = pending >= 0.999F;
        if (timer % (180 - omen * 20) == 40) {
            Player player = nearest(level, center());
            if (player != null) {
                Vec3 c = center().add(0, 0.7, 0);
                IncendiaryBottle bottle = new IncendiaryBottle(level, c.x, c.y, c.z);
                bottle.haunt(worldPosition, player);
                level.addFreshEntity(bottle);
                level.playSound(null, worldPosition, SoundEvents.SOUL_ESCAPE.value(), SoundSource.BLOCKS, 1.5F, 1.3F);
            }
        }
        if (!paused && (mobs.size() >= CAP || full)) {
            paused = true;
            lit.forEach(b -> SummoningBonfireBlock.setLit(level, b, false));
        } else if (paused && !full && mobs.size() < REFILL) {
            paused = false;
            lit.forEach(b -> SummoningBonfireBlock.setLit(level, b, true));
        }

        if (--rotateIn <= 0) {
            pickBonfires(level);
        }
        if (!paused) {
            for (BlockPos b : lit) {
                int left = spawnIn.merge(b, -1, Integer::sum);
                if (left <= 0 && mobs.size() < CAP) {
                    boolean boss = abominationsSent < abominationsDue && filled >= ABOMINATION_AT[abominationsSent];
                    if (spawn(level, b, boss ? WildscapesEntities.ABOMINATION : roll(level)) && boss) {
                        abominationsSent++;
                    }
                    spawnIn.put(b, 140 + level.getRandom().nextInt(80) - omen * 8);
                }
            }
        }

    }

    private void pickBonfires(ServerLevel level) {
        bonfires.removeIf(p -> !(level.getBlockState(p).getBlock() instanceof SummoningBonfireBlock));
        List<BlockPos> before = new ArrayList<>(lit);
        lit.forEach(b -> SummoningBonfireBlock.setLit(level, b, false));
        lit.clear();
        spawnIn.clear();
        rotateIn = ROTATE + level.getRandom().nextInt(120);

        List<BlockPos> pool = new ArrayList<>(bonfires);
        if (pool.isEmpty()) {
            pool.add(worldPosition);
        }
        Util.shuffle(pool, level.getRandom());
        if (pool.size() > before.size()) {
            pool.sort((a, b) -> Boolean.compare(before.contains(a), before.contains(b)));
        }
        int n = pool.size() <= 2 ? 1 : level.getRandom().nextFloat() < 0.35F + omen * 0.1F ? 2 : 1;
        for (int i = 0; i < n; i++) {
            BlockPos b = pool.get(i);
            lit.add(b);
            spawnIn.put(b, 20 + level.getRandom().nextInt(20));
            if (!paused) {
                SummoningBonfireBlock.setLit(level, b, true);
            }
        }
        setChanged();
    }

    private boolean spawn(ServerLevel level, BlockPos at, EntityType<? extends Mob> type) {
        BlockPos spot = findSpot(level, at, type);
        if (spot == null) {
            return false;
        }
        Mob mob = type.create(level, EntitySpawnReason.EVENT);
        if (mob == null) {
            return false;
        }
        mob.snapTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, level.getRandom().nextFloat() * 360.0F, 0.0F);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(spot), EntitySpawnReason.EVENT, null);
        mob.setPersistenceRequired();
        mob.setAttached(SoulHarvest.ORIGIN, worldPosition);
        if (sinceEmpowered >= 3 || level.getRandom().nextFloat() < 0.15F) {
            SoulHarvest.empower(mob);
            sinceEmpowered = 0;
        } else {
            sinceEmpowered++;
        }
        Player target = nearest(level, mob.position());
        if (target != null) {
            mob.setTarget(target);
        }
        level.addFreshEntityWithPassengers(mob);
        mobs.add(mob.getUUID());

        double x = spot.getX() + 0.5;
        double z = spot.getZ() + 0.5;
        level.sendParticles(WildscapesParticles.SUMMON_WRAITH, x, spot.getY() + 0.6, z, 4, 0.3, 0.4, 0.3, 0.02);
        level.sendParticles(WildscapesParticles.SUMMON_RISE, x, spot.getY() + 0.2, z, 6, 0.4, 0.1, 0.4, 0.03);
        level.sendParticles(WildscapesParticles.SUMMON_MOTE, x, spot.getY() + 1.0, z, 12, 0.4, 0.6, 0.4, 0.05);
        level.playSound(null, spot, SoundEvents.TRIAL_SPAWNER_SPAWN_MOB, SoundSource.HOSTILE, 1.0F, 0.8F);
        setChanged();
        return true;
    }

    private EntityType<? extends Mob> roll(ServerLevel level) {
        int bosses = 0;
        for (UUID id : mobs) {
            Entity e = level.getEntity(id);
            if (e != null && (e.getType() == EntityTypes.ILLUSIONER || e.getType() == WildscapesEntities.ABOMINATION)) {
                bosses++;
            }
        }
        boolean room = bosses < 1 + omen / 3;
        int illusioner = room ? 2 + omen : 0;
        int r = level.getRandom().nextInt(30 + 30 + 12 + 10 + illusioner);
        if ((r -= 30) < 0) {
            return EntityTypes.PILLAGER;
        }
        if ((r -= 30) < 0) {
            return EntityTypes.VINDICATOR;
        }
        if ((r -= 12) < 0) {
            return EntityTypes.WITCH;
        }
        if ((r -= 10) < 0) {
            return EntityTypes.EVOKER;
        }
        return EntityTypes.ILLUSIONER;
    }

    @Nullable
    private static BlockPos findSpot(ServerLevel level, BlockPos at, EntityType<?> type) {
        RandomSource random = level.getRandom();
        for (int tries = 0; tries < 16; tries++) {
            int dx = random.nextInt(9) - 4;
            int dz = random.nextInt(9) - 4;
            if (Math.abs(dx) < 2 && Math.abs(dz) < 2) {
                continue;
            }
            for (int dy = 2; dy >= -3; dy--) {
                BlockPos p = at.offset(dx, dy, dz);
                BlockPos below = p.below();
                if (level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)
                        && level.getFluidState(p).isEmpty()
                        && level.noCollision(type.getSpawnAABB(p.getX() + 0.5, p.getY(), p.getZ() + 0.5))) {
                    return p;
                }
            }
        }
        return null;
    }

    public void onMobDeath(LivingEntity dead, DamageSource source) {
        mobs.remove(dead.getUUID());
        setChanged();
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        boolean empowered = SoulHarvest.isEmpowered(dead);
        if (empowered) {
            server.playSound(null, dead.blockPosition(), WildscapesSounds.EMPOWERED_DEATH, SoundSource.HOSTILE, 1.0F, 1.0F);
        }
        if (stage != Stage.RAID || !(source.getEntity() instanceof Player)) {
            server.sendParticles(WildscapesParticles.LOST_SOUL, dead.getX(), dead.getY(0.6), dead.getZ(),
                    empowered ? 6 : 3, 0.3, 0.4, 0.3, 0.02);
            return;
        }
        Vec3 to = center().add(0.0, 0.7, 0.0);
        int travel = 10 + Mth.clamp((int) (dead.position().distanceTo(to) * 1.65), 17, 66);
        server.sendParticles(new SoulTrailOptions(to, travel), dead.getX(), dead.getY(0.5), dead.getZ(),
                empowered ? 10 : 4, 0.3, 0.4, 0.3, 0.0);
        server.sendParticles(WildscapesParticles.SOUL_MOTE, dead.getX(), dead.getY(0.5), dead.getZ(),
                empowered ? 8 : 3, 0.4, 0.5, 0.4, 0.02);
        arrivals.add(travel);
        worth.add(worthOf(dead, empowered));
    }

    private float worthOf(LivingEntity dead, boolean empowered) {
        int i = Mth.clamp(omen, 1, 5) - 1;
        float[] row = dead.getType() == WildscapesEntities.ABOMINATION ? ABOMINATION_WORTH[i] : MOB_WORTH[i];
        return row[empowered ? 1 : 0];
    }

    private void win(ServerLevel level) {
        vanish(level);
        bonfires.forEach(b -> SummoningBonfireBlock.setLit(level, b, false));
        lit.clear();
        stage = Stage.REWARDING;
        setPhase(CauldronOfSoulsBlock.Phase.REWARDS);
        bar.setName(Component.translatable("event.wildscapes.soul_harvest.victory"));
        bar.setProgress(1.0F);
        barLinger = BAR_LINGER;

        LootTable table = level.getServer().reloadableRegistries().getLootTable(REWARDS);
        int rolls = (omen + 1) / 2;
        for (ServerPlayer player : near(level)) {
            if (!participants.contains(player.getUUID())) {
                continue;
            }
            LootParams params = new LootParams.Builder(level)
                    .withParameter(LootContextParams.ORIGIN, center())
                    .withParameter(LootContextParams.THIS_ENTITY, player)
                    .withLuck(player.getLuck())
                    .create(LootContextParamSets.VAULT);
            for (int i = 0; i < rolls; i++) {
                table.getRandomItems(params).forEach(loot::add);
            }
            if (omen <= 2 || omen >= 4) {
                level.getServer().reloadableRegistries().getLootTable(omen <= 2 ? LOW_REWARDS : HIGH_REWARDS)
                        .getRandomItems(params).forEach(loot::add);
            }
        }
        ejectIn = 30;
        setChanged();
    }

    private void eject(ServerLevel level) {
        if (--ejectIn > 0) {
            return;
        }
        ItemStack stack = loot.poll();
        if (stack == null) {
            stage = Stage.CLOSING;
            timer = CLOSE_DELAY;
            setChanged();
            return;
        }
        Vec3 c = center().add(0.0, 0.9, 0.0);
        ItemEntity item = new ItemEntity(level, c.x, c.y, c.z, stack);
        item.setDeltaMovement(level.getRandom().nextDouble() * 0.2 - 0.1, 0.35, level.getRandom().nextDouble() * 0.2 - 0.1);
        item.setDefaultPickUpDelay();
        level.addFreshEntity(item);
        level.sendParticles(WildscapesParticles.CAULDRON_REWARD, c.x, c.y, c.z, 6, 0.3, 0.2, 0.3, 0.03);
        level.playSound(null, worldPosition, WildscapesSounds.CAULDRON_OF_SOULS_REWARD, SoundSource.BLOCKS, 1.0F,
                0.9F + level.getRandom().nextFloat() * 0.2F);
        ejectIn = 12;
        setChanged();
    }

    private void fail(ServerLevel level) {
        vanish(level);
        bonfires.forEach(b -> SummoningBonfireBlock.setLit(level, b, false));
        lit.clear();
        stage = Stage.IDLE;
        omen = 0;
        setPhase(CauldronOfSoulsBlock.Phase.INACTIVE);
        if (bar != null) {
            bar.setName(Component.translatable("event.wildscapes.soul_harvest.defeat"));
            barLinger = BAR_LINGER;
        }
        setChanged();
    }

    public void collapse() {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        if (stage == Stage.WINDUP || stage == Stage.RAID) {
            vanish(server);
            bonfires.forEach(b -> SummoningBonfireBlock.setLit(server, b, false));
        }
        if (bar != null) {
            bar.removeAllPlayers();
            bar = null;
        }
        stage = Stage.IDLE;
    }

    private void vanish(ServerLevel level) {
        level.getEntitiesOfClass(IncendiaryBottle.class, new AABB(worldPosition).inflate(RADIUS + 16),
                b -> b.cameFrom(worldPosition)).forEach(Entity::discard);
        for (ServerPlayer p : near(level)) {
            p.removeEffect(WildscapesEffects.SOUL_HARVEST);
        }
        for (UUID id : mobs) {
            Entity e = level.getEntity(id);
            if (e != null) {
                level.sendParticles(WildscapesParticles.LOST_SOUL, e.getX(), e.getY(0.5), e.getZ(),
                        5, 0.3, 0.4, 0.3, 0.02);
                e.discard();
            }
        }
        mobs.clear();
        arrivals.clear();
        worth.clear();
    }

    private List<ServerPlayer> near(ServerLevel level) {
        Vec3 c = center();
        return level.getPlayers(p -> p.isAlive() && !p.isSpectator() && p.distanceToSqr(c) <= RADIUS * RADIUS);
    }

    private int updatePlayers(ServerLevel level) {
        List<ServerPlayer> near = near(level);
        for (ServerPlayer p : near) {
            participants.add(p.getUUID());
            bar.addPlayer(p);
            p.addEffect(new MobEffectInstance(WildscapesEffects.SOUL_HARVEST, 60, 0, true, false, true));
        }
        for (ServerPlayer p : new ArrayList<>(bar.getPlayers())) {
            if (!near.contains(p)) {
                bar.removePlayer(p);
            }
        }
        return near.size();
    }

    @Nullable
    private Player nearest(ServerLevel level, Vec3 from) {
        Player best = null;
        double bestDist = Double.MAX_VALUE;
        for (ServerPlayer p : near(level)) {
            if (p.isCreative()) {
                continue;
            }
            double d = p.distanceToSqr(from);
            if (d < bestDist) {
                bestDist = d;
                best = p;
            }
        }
        return best;
    }

    private void setPhase(CauldronOfSoulsBlock.Phase phase) {
        BlockState state = getBlockState();
        if (level != null && state.getValue(CauldronOfSoulsBlock.PHASE) != phase) {
            level.setBlock(worldPosition, state.setValue(CauldronOfSoulsBlock.PHASE, phase), Block.UPDATE_ALL);
        }
    }

    public void onLoad() {
        if (level != null && !level.isClientSide()) {
            LOADED.computeIfAbsent(level.dimension(), k -> new HashSet<>()).add(worldPosition);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide()) {
            Set<BlockPos> here = LOADED.get(level.dimension());
            if (here != null) {
                here.remove(worldPosition);
            }
            if (bar != null) {
                bar.removeAllPlayers();
                bar = null;
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putString("Stage", stage.name());
        tag.putInt("Omen", omen);
        tag.putInt("Timer", timer);
        tag.putFloat("Filled", filled);
        tag.putInt("RotateIn", rotateIn);
        tag.putInt("Away", away);
        tag.putInt("SinceEmpowered", sinceEmpowered);
        tag.putInt("AbominationsDue", abominationsDue);
        tag.putInt("AbominationsSent", abominationsSent);
        tag.putInt("EjectIn", ejectIn);
        tag.putBoolean("Paused", paused);
        tag.store("Bonfires", com.mojang.serialization.Codec.LONG_STREAM, bonfires.stream().mapToLong(BlockPos::asLong));
        tag.store("Lit", com.mojang.serialization.Codec.LONG_STREAM, lit.stream().mapToLong(BlockPos::asLong));
        tag.putIntArray("Arrivals", arrivals.toIntArray());
        tag.store("Worth", com.mojang.serialization.Codec.FLOAT.listOf(), List.copyOf(worth));
        tag.store("Mobs", UUIDUtil.CODEC.listOf(), List.copyOf(mobs));
        tag.store("Participants", UUIDUtil.CODEC.listOf(), List.copyOf(participants));
        tag.store("Loot", ItemStack.CODEC.listOf(), List.copyOf(loot));
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        try {
            stage = Stage.valueOf(tag.getStringOr("Stage", "IDLE").replace("COOLDOWN", "CLOSING"));
        } catch (IllegalArgumentException e) {
            stage = Stage.IDLE;
        }
        omen = tag.getIntOr("Omen", 0);
        timer = stage == Stage.CLOSING ? Math.min(tag.getIntOr("Timer", 0), CLOSE_DELAY) : tag.getIntOr("Timer", 0);
        filled = tag.getFloatOr("Filled", 0);
        rotateIn = tag.getIntOr("RotateIn", 0);
        away = tag.getIntOr("Away", 0);
        sinceEmpowered = tag.getIntOr("SinceEmpowered", 0);
        abominationsDue = tag.getIntOr("AbominationsDue", 0);
        abominationsSent = tag.getIntOr("AbominationsSent", 0);
        ejectIn = tag.getIntOr("EjectIn", 0);
        paused = tag.getBooleanOr("Paused", false);
        bonfires.clear();
        for (long l : tag.read("Bonfires", com.mojang.serialization.Codec.LONG_STREAM).orElseGet(java.util.stream.LongStream::empty).toArray()) {
            bonfires.add(BlockPos.of(l));
        }
        lit.clear();
        spawnIn.clear();
        for (long l : tag.read("Lit", com.mojang.serialization.Codec.LONG_STREAM).orElseGet(java.util.stream.LongStream::empty).toArray()) {
            lit.add(BlockPos.of(l));
            spawnIn.put(BlockPos.of(l), 40);
        }
        arrivals.clear();
        arrivals.addElements(0, tag.getIntArray("Arrivals").orElseGet(() -> new int[0]));
        worth.clear();
        tag.read("Worth", com.mojang.serialization.Codec.FLOAT.listOf()).ifPresent(worth::addAll);
        while (worth.size() < arrivals.size()) {
            worth.add(0.0F);
        }
        mobs.clear();
        tag.read("Mobs", UUIDUtil.CODEC.listOf()).ifPresent(mobs::addAll);
        participants.clear();
        tag.read("Participants", UUIDUtil.CODEC.listOf()).ifPresent(participants::addAll);
        loot.clear();
        tag.read("Loot", ItemStack.CODEC.listOf()).ifPresent(loot::addAll);
    }
}
