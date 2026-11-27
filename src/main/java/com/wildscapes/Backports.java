package com.wildscapes;

import com.wildscapes.entity.WildscapesEntityData;

import com.wildscapes.entity.WildscapesEntities;
import com.wildscapes.item.WildscapesItems;

import org.jetbrains.annotations.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.util.RandomSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class Backports {
    private Backports() {}

    private static Item bottle;
    private static EntityType<?> bogged;
    private static boolean scanned;

    private static void scan() {
        if (scanned) {
            return;
        }
        scanned = true;
        bottle = find(BuiltInRegistries.ITEM, "ominous_bottle");
        bogged = find(BuiltInRegistries.ENTITY_TYPE, "bogged");
        if (bottle != null) {
            Wildscapes.LOGGER.info("found {}, handing ominous bottles over to it", BuiltInRegistries.ITEM.getKey(bottle));
        }
        if (bogged != null) {
            Wildscapes.LOGGER.info("found {}, handing bogged over to it", BuiltInRegistries.ENTITY_TYPE.getKey(bogged));
        }
    }

    private static <T> T find(Registry<T> registry, String path) {
        for (var entry : registry.entrySet()) {
            ResourceLocation id = entry.getKey().location();
            if (id.getPath().equals(path) && !id.getNamespace().equals(Wildscapes.MODID)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static boolean hasBottle() {
        scan();
        return bottle != null;
    }

    public static boolean hasBogged() {
        scan();
        return bogged != null;
    }

    public static ItemStack swapBottle(ItemStack ours) {
        ItemStack out = new ItemStack(bottle, ours.getCount());
        int amp = ours.hasTag() ? ours.getTag().getInt("OminousBottleAmplifier") : 0;
        CompoundTag tag = out.getOrCreateTag();
        tag.putInt("OminousAmplifier", amp);
        tag.putInt("OminousBottleAmplifier", amp);
        return out;
    }

    public static boolean replaceOnJoin(ServerLevel level, Entity entity) {
        if (!hasBogged() || entity.getType() != WildscapesEntities.BOGGED.get()) {
            return false;
        }
        Mob ours = (Mob) entity;
        Entity made = bogged.create(level);
        if (!(made instanceof Mob theirs)) {
            return false;
        }
        theirs.moveTo(ours.getX(), ours.getY(), ours.getZ(), ours.getYRot(), ours.getXRot());
        theirs.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(ours.position())),
                MobSpawnType.CONVERSION, null, null);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack item = ours.getItemBySlot(slot);
            if (!item.isEmpty()) {
                theirs.setItemSlot(slot, item.copy());
            }
        }
        WildscapesEntityData.of(theirs).merge(WildscapesEntityData.of(ours));
        if (ours.hasCustomName()) {
            theirs.setCustomName(ours.getCustomName());
        }
        if (ours.isPersistenceRequired()) {
            theirs.setPersistenceRequired();
        }
        level.addFreshEntity(theirs);
        return true;
    }

    public static ItemStack captainBottle(RandomSource random) {
        ItemStack bottle = new ItemStack(WildscapesItems.OMINOUS_BOTTLE.get());
        bottle.getOrCreateTag().putInt("OminousBottleAmplifier", random.nextInt(5));
        return bottle;
    }

    @Nullable
    public static MobEffectInstance omen(LivingEntity entity) {
        MobEffectInstance vanilla = entity.getEffect(MobEffects.BAD_OMEN);
        if (vanilla != null) {
            return vanilla;
        }
        for (MobEffectInstance effect : entity.getActiveEffects()) {
            ResourceLocation id = BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect());
            if (id != null && id.getPath().equals("bad_omen")) {
                return effect;
            }
        }
        return null;
    }

    public static ItemStack swapLoot(ItemStack stack) {
        return hasBottle() && stack.is(WildscapesItems.OMINOUS_BOTTLE.get()) ? swapBottle(stack) : stack;
    }
}
