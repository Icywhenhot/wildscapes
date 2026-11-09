package com.wildscapes.item;

import java.util.UUID;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.wildscapes.entity.MirelashHook;
import com.wildscapes.sound.WildscapesSounds;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;

public final class MirelashItem extends Item {
    private final Multimap<Attribute, AttributeModifier> attributes = ImmutableMultimap.of(
            Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 5.0,
                    AttributeModifier.Operation.ADDITION),
            Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2.4,
                    AttributeModifier.Operation.ADDITION));
    private static final String HOOK_KEY = "wildscapes:mirelash_hook";
    public static final double ATTACK_RANGE = 10.0;

    public MirelashItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }
        MirelashHook hook = findHook(serverLevel, player);
        if (hook != null) {
            hook.discard();
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_RETRIEVE,
                    SoundSource.PLAYERS, 1.0F, 0.9F + level.random.nextFloat() * 0.2F);
        } else {
            hook = new MirelashHook(serverLevel, player, stack);
            serverLevel.addFreshEntity(hook);
            player.getPersistentData().putUUID(HOOK_KEY, hook.getUUID());
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            level.playSound(null, player.getX(), player.getY(), player.getZ(), WildscapesSounds.MIRELASH_THROW.get(),
                    SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.is(WildscapesItems.ABOMINATION_TONGUE.get());
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 12;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment == Enchantments.UNBREAKING || enchantment == Enchantments.MENDING
                || enchantment == WildscapesEnchantments.ELASTICITY.get()
                || enchantment == WildscapesEnchantments.SLIME_SNARE.get();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? attributes : super.getDefaultAttributeModifiers(slot);
    }

    public static LivingEntity findAttackTarget(Level level, Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(ATTACK_RANGE));
        HitResult blockHit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, player));
        double nearest = blockHit.getType() == HitResult.Type.MISS
                ? ATTACK_RANGE * ATTACK_RANGE
                : eye.distanceToSqr(blockHit.getLocation());
        LivingEntity found = null;
        AABB search = player.getBoundingBox().expandTowards(look.scale(ATTACK_RANGE)).inflate(1.0);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, search,
                target -> target != player && target.isAlive() && target.isPickable()
                        && !player.isAlliedTo(target) && !target.isAlliedTo(player))) {
            AABB bounds = target.getBoundingBox().inflate(target.getPickRadius());
            Vec3 hit = bounds.contains(eye) ? eye : bounds.clip(eye, end).orElse(null);
            if (hit == null) {
                continue;
            }
            double distance = eye.distanceToSqr(hit);
            if (distance <= nearest) {
                found = target;
                nearest = distance;
            }
        }
        return found;
    }

    public static boolean launchAttack(Player player, LivingEntity target) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof MirelashItem) || player.getAttackStrengthScale(0.5F) < 0.9F
                || player.getCooldowns().isOnCooldown(stack.getItem())
                || findAttackTarget(player.level(), player) != target) {
            return false;
        }
        if (player.level() instanceof ServerLevel level) {
            level.addFreshEntity(new MirelashHook(level, player, stack, target));
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(EquipmentSlot.MAINHAND));
            level.playSound(null, player.getX(), player.getY(), player.getZ(), WildscapesSounds.MIRELASH_THROW.get(),
                    SoundSource.PLAYERS, 1.0F, 1.0F);
            player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
            player.getCooldowns().addCooldown(stack.getItem(), 40);
            player.resetAttackStrengthTicker();
        }
        return true;
    }

    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (event.phase != TickEvent.Phase.END || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        MirelashHook hook = findHook(level, player);
        if (hook == null || !hook.isAnchored()) {
            return;
        }
        player.resetFallDistance();
        Vec3 pull = hook.position().subtract(player.getEyePosition());
        double distance = pull.length();
        if (distance <= hook.getTetherLength()) {
            return;
        }
        double strength = distance / hook.getTetherLength() * 0.1;
        Vec3 movement = player.getDeltaMovement().multiply(0.99, 0.995, 0.99);
        Vec3 tug = pull.normalize().multiply(strength, strength * 1.1, strength);
        player.setDeltaMovement(movement.add(tug));
        player.hasImpulse = true;
        player.hurtMarked = true;
    }

    public static void clearHook(Player player, UUID hookId) {
        if (player.getPersistentData().hasUUID(HOOK_KEY)
                && player.getPersistentData().getUUID(HOOK_KEY).equals(hookId)) {
            player.getPersistentData().remove(HOOK_KEY);
        }
    }

    public static boolean isHookOut(Level level, LivingEntity holder) {
        return !level.getEntitiesOfClass(MirelashHook.class, holder.getBoundingBox().inflate(96.0),
                hook -> hook.getOwner() == holder).isEmpty();
    }

    private static MirelashHook findHook(ServerLevel level, Player player) {
        if (!player.getPersistentData().hasUUID(HOOK_KEY)) {
            return null;
        }
        UUID hookId = player.getPersistentData().getUUID(HOOK_KEY);
        Entity entity = level.getEntity(hookId);
        if (entity instanceof MirelashHook hook && hook.getOwner() == player) {
            return hook;
        }
        player.getPersistentData().remove(HOOK_KEY);
        return null;
    }
}
