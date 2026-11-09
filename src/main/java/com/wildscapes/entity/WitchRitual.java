package com.wildscapes.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.wildscapes.Wildscapes;
import com.wildscapes.particle.WildscapesParticles;
import com.wildscapes.sound.WildscapesSounds;
import com.wildscapes.worldgen.WildscapesStructures;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@EventBusSubscriber(modid = Wildscapes.MODID)
public final class WitchRitual {
    private static final int GATHER_MAX = 160;
    private static final int CHANT = 200;
    private static final double REACH = 16.0D;
    private static final int MAX_WITCHES = 8;
    private static final int CHECK_EVERY = 1200;
    private static final int ODDS = 300;

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Wildscapes.MODID);

    public static final RegistryObject<AttachmentType<Boolean>> CHANTING =
            ATTACHMENT_TYPES.register("chanting", () -> AttachmentType.builder(() -> false)
                    .sync(ByteBufCodecs.BOOL)
                    .build());

    private static final RegistryObject<AttachmentType<Boolean>> TOAD =
            ATTACHMENT_TYPES.register("ritual_toad", () -> AttachmentType.builder(() -> false)
                    .serialize(Codec.BOOL)
                    .build());

    private static final List<WitchRitual> running = new ArrayList<>();

    private final ServerLevel level;
    private final Vec3 center;
    private final Frog toad;
    private final List<Witch> witches;
    private final List<Vec3> slots = new ArrayList<>();
    private int ticks;
    private boolean chanting;

    private WitchRitual(ServerLevel level, Vec3 center, Frog toad, List<Witch> witches) {
        this.level = level;
        this.center = center;
        this.toad = toad;
        this.witches = new ArrayList<>(witches);
        double r = Math.max(3.0D, witches.size() * 0.75D);
        double first = witches.isEmpty() ? 0.0D : angle(witches.get(0).position());
        for (int i = 0; i < witches.size(); i++) {
            double a = first + Math.PI * 2.0D * i / witches.size();
            this.slots.add(new Vec3(center.x + Math.cos(a) * r, center.y, center.z + Math.sin(a) * r));
        }
    }

    public static void register(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }

    @SubscribeEvent
    static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) {
            tickAll(level);
        }
    }

    @SubscribeEvent
    static void onServerStopping(ServerStoppingEvent event) {
        List.copyOf(running).forEach(WitchRitual::abort);
    }

    @SubscribeEvent
    static void onJoinLevel(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof Frog frog && frog.hasData(TOAD)
                && running.stream().noneMatch(r -> r.toad == frog)) {
            frog.removeData(TOAD);
            frog.setNoAi(false);
        }
    }

    @SubscribeEvent
    static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal(Wildscapes.MODID)
                        .requires(src -> src.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("ritual").executes(cmd -> fromCommand(cmd.getSource()))));
    }

    private static int fromCommand(CommandSourceStack src) {
        ServerLevel level = src.getLevel();
        Vec3 at = src.getPosition();
        if (near(level, at, REACH) != null) {
            src.sendFailure(Component.translatable("commands.wildscapes.ritual.busy"));
            return 0;
        }
        List<Witch> witches = witchesAround(level, at);
        if (witches.isEmpty()) {
            for (int i = 0; i < 4; i++) {
                double a = Math.PI * 0.5D * i;
                BlockPos p = ground(level, BlockPos.containing(at.x + Math.cos(a) * 4.0D, at.y, at.z + Math.sin(a) * 4.0D));
                Witch witch = EntityType.WITCH.spawn(level, p, MobSpawnType.COMMAND);
                if (witch != null) {
                    witches.add(witch);
                }
            }
        }
        if (start(level, at, witches) == null) {
            src.sendFailure(Component.translatable("commands.wildscapes.ritual.failed"));
            return 0;
        }
        src.sendSuccess(() -> Component.translatable("commands.wildscapes.ritual.started"), true);
        return 1;
    }

    public static void maybeStart(Witch witch) {
        if ((witch.tickCount + witch.getId()) % CHECK_EVERY != 0 || witch.getRandom().nextInt(ODDS) != 0) {
            return;
        }
        ServerLevel level = (ServerLevel) witch.level();
        if (!level.isNight() || witch.getTarget() != null || of(witch) != null
                || level.getNearestPlayer(witch, 48.0D) == null
                || !inWitchCamp(level, witch.blockPosition())
                || near(level, witch.position(), 32.0D) != null
                || !level.getEntitiesOfClass(AbominationEntity.class, witch.getBoundingBox().inflate(48.0D)).isEmpty()) {
            return;
        }
        List<Witch> witches = witchesAround(level, witch.position());
        Vec3 mid = Vec3.ZERO;
        for (Witch w : witches) {
            mid = mid.add(w.position());
        }
        mid = mid.scale(1.0D / witches.size());
        BlockPos spot = ground(level, BlockPos.containing(mid));
        if (!standable(level, spot)) {
            spot = witch.blockPosition();
        }
        start(level, Vec3.atBottomCenterOf(spot), witches);
    }

    @Nullable
    public static WitchRitual start(ServerLevel level, Vec3 at, List<Witch> witches) {
        Frog toad = EntityType.FROG.create(level);
        if (toad == null) {
            return null;
        }
        toad.moveTo(at.x, at.y, at.z, level.getRandom().nextFloat() * 360.0F, 0.0F);
        toad.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(at)), MobSpawnType.EVENT, null);
        level.addFreshEntity(toad);
        toad.setNoAi(true);
        toad.setData(TOAD, true);

        WitchRitual ritual = new WitchRitual(level, at, toad, witches);
        running.add(ritual);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.HOSTILE, 1.0F, 0.7F);
        return ritual;
    }

    private static void tickAll(ServerLevel level) {
        for (WitchRitual r : List.copyOf(running)) {
            if (r.level == level) {
                r.tick();
            }
        }
    }

    private void tick() {
        this.witches.removeIf(w -> !w.isAlive());
        if (!this.toad.isAlive() || this.witches.isEmpty()) {
            abort();
            return;
        }
        this.ticks++;

        if (!this.chanting) {
            boolean ready = true;
            for (Witch w : this.witches) {
                Vec3 slot = slotOf(w);
                ready &= w.position().subtract(slot.x, w.getY(), slot.z).horizontalDistanceSqr() < 0.6D;
            }
            if (ready || this.ticks >= GATHER_MAX) {
                this.chanting = true;
                this.ticks = 0;
                this.witches.forEach(w -> w.setData(CHANTING, true));
            }
            return;
        }

        float t = (float) this.ticks / CHANT;
        int souls = 1 + (int) (t * 4.0F);
        this.level.sendParticles(WildscapesParticles.LOST_SOUL.get(), this.center.x, this.center.y + 0.3D, this.center.z,
                0, 0.0D, 1.0D, 0.0D, 0.04D + t * 0.08D);
        this.level.sendParticles(WildscapesParticles.LOST_SOUL.get(), this.center.x, this.center.y + 0.4D, this.center.z,
                souls, 0.25D, 0.2D, 0.25D, 0.02D);
        if (this.ticks % 3 == 0) {
            this.level.sendParticles(WildscapesParticles.SOUL_MOTE.get(), this.center.x, this.center.y + 0.6D, this.center.z,
                    2 + (int) (t * 6.0F), 0.4D, 0.5D, 0.4D, 0.02D);
        }

        ColorParticleOption spell = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0.45F, 0.3F, 0.7F);
        for (Witch w : this.witches) {
            float body = w.yBodyRot * Mth.DEG_TO_RAD + Mth.cos(w.tickCount * 0.6662F) * 0.25F;
            double cx = Mth.cos(body) * 0.6D;
            double cz = Mth.sin(body) * 0.6D;
            this.level.sendParticles(spell, w.getX() + cx, w.getY() + 1.8D, w.getZ() + cz, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            this.level.sendParticles(spell, w.getX() - cx, w.getY() + 1.8D, w.getZ() - cz, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }

        if (this.ticks % 25 == 0) {
            Witch w = this.witches.get(this.level.getRandom().nextInt(this.witches.size()));
            this.level.playSound(null, w.getX(), w.getY(), w.getZ(), SoundEvents.WITCH_CELEBRATE, SoundSource.HOSTILE,
                    1.0F, 0.7F + this.level.getRandom().nextFloat() * 0.2F);
        }
        if (this.ticks >= CHANT) {
            climax();
        }
    }

    private void climax() {
        double x = this.toad.getX();
        double y = this.toad.getY();
        double z = this.toad.getZ();
        this.level.sendParticles(WildscapesParticles.RESIDUE_SPLAT.get(), x, y + 0.2D, z, 40, 1.2D, 0.2D, 1.2D, 0.15D);
        this.level.sendParticles(WildscapesParticles.RESIDUE_SWIRL.get(), x, y + 0.8D, z, 30, 0.8D, 0.8D, 0.8D, 0.08D);
        this.level.sendParticles(WildscapesParticles.SUMMON_WRAITH.get(), x, y + 1.0D, z, 10, 0.6D, 0.6D, 0.6D, 0.04D);
        this.level.sendParticles(WildscapesParticles.SUMMON_MOTE.get(), x, y + 1.0D, z, 30, 1.0D, 1.0D, 1.0D, 0.08D);
        this.level.sendParticles(WildscapesParticles.EMPOWERED_RING.get(), x, y + 0.1D, z, 6, 0.4D, 0.0D, 0.4D, 0.0D);
        this.level.playSound(null, x, y, z, SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 1.5F, 0.6F);
        this.level.playSound(null, x, y, z, WildscapesSounds.ABOMINATION_AMBIENT.get(), SoundSource.HOSTILE, 1.5F, 0.9F);

        AbominationEntity mob = WildscapesEntities.ABOMINATION.get().create(this.level);
        if (mob != null) {
            mob.moveTo(x, y, z, this.toad.getYRot(), 0.0F);
            mob.finalizeSpawn(this.level, this.level.getCurrentDifficultyAt(this.toad.blockPosition()),
                    MobSpawnType.EVENT, null);
            this.level.addFreshEntity(mob);
        }
        this.toad.discard();
        finish();
    }

    private void abort() {
        if (this.toad.isAlive()) {
            this.toad.removeData(TOAD);
            this.toad.setNoAi(false);
        }
        finish();
    }

    private void finish() {
        this.witches.forEach(w -> w.removeData(CHANTING));
        running.remove(this);
    }

    private Vec3 slotOf(Witch witch) {
        return this.slots.get(Math.max(0, this.witches.indexOf(witch)) % this.slots.size());
    }

    private double angle(Vec3 p) {
        return Math.atan2(p.z - this.center.z, p.x - this.center.x);
    }

    @Nullable
    public static WitchRitual of(Witch witch) {
        for (WitchRitual r : running) {
            if (r.witches.contains(witch)) {
                return r;
            }
        }
        return null;
    }

    public static boolean isChanting(Entity entity) {
        return entity.getData(CHANTING);
    }

    @Nullable
    private static WitchRitual near(ServerLevel level, Vec3 at, double dist) {
        for (WitchRitual r : running) {
            if (r.level == level && r.center.closerThan(at, dist)) {
                return r;
            }
        }
        return null;
    }

    private static List<Witch> witchesAround(ServerLevel level, Vec3 at) {
        List<Witch> out = new ArrayList<>(level.getEntitiesOfClass(Witch.class, new AABB(at, at).inflate(REACH),
                w -> w.isAlive() && w.getTarget() == null && of(w) == null));
        out.sort(Comparator.comparingDouble(w -> w.distanceToSqr(at)));
        if (out.size() > MAX_WITCHES) {
            out = new ArrayList<>(out.subList(0, MAX_WITCHES));
        }
        out.sort(Comparator.comparingDouble(w -> Math.atan2(w.getZ() - at.z, w.getX() - at.x)));
        return out;
    }

    private static boolean inWitchCamp(ServerLevel level, BlockPos pos) {
        return level.registryAccess().registryOrThrow(Registries.STRUCTURE).stream()
                .filter(s -> s.type() == WildscapesStructures.WITCH_CAMP.get())
                .anyMatch(s -> level.structureManager().getStructureAt(pos, s).isValid());
    }

    private static BlockPos ground(ServerLevel level, BlockPos from) {
        for (int dy = 3; dy >= -4; dy--) {
            BlockPos p = from.above(dy);
            if (standable(level, p)) {
                return p;
            }
        }
        return from;
    }

    private static boolean standable(ServerLevel level, BlockPos p) {
        return !level.getBlockState(p).blocksMotion() && !level.getBlockState(p.above()).blocksMotion()
                && level.getBlockState(p.below()).isFaceSturdy(level, p.below(), Direction.UP);
    }

    public static class Gather extends Goal {
        private final Witch witch;

        public Gather(Witch witch) {
            this.witch = witch;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return of(this.witch) != null;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void stop() {
            this.witch.getNavigation().stop();
        }

        @Override
        public void tick() {
            WitchRitual r = of(this.witch);
            if (r == null) {
                return;
            }
            this.witch.setTarget(null);
            Vec3 slot = r.slotOf(this.witch);
            if (!r.chanting && this.witch.position().subtract(slot.x, this.witch.getY(), slot.z).horizontalDistanceSqr() >= 0.6D) {
                if (this.witch.getNavigation().isDone() || this.witch.tickCount % 20 == 0) {
                    this.witch.getNavigation().moveTo(slot.x, slot.y, slot.z, 0.9D);
                }
                return;
            }
            this.witch.getNavigation().stop();
            double dx = r.center.x - this.witch.getX();
            double dz = r.center.z - this.witch.getZ();
            float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
            this.witch.setYRot(yaw);
            this.witch.yBodyRot = yaw;
            this.witch.yHeadRot = yaw;
            this.witch.getLookControl().setLookAt(r.center.x, r.center.y + 0.2D, r.center.z);
        }
    }
}
