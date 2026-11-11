package com.wildscapes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wildscapes.entity.WildscapesEntities;
import com.wildscapes.item.WildscapesItems;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

public final class Backports {
    private Backports() {}

    private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Wildscapes.MODID);

    public static final RegistryObject<Codec<SwapBottle>> SWAP_BOTTLE =
            LOOT_MODIFIERS.register("swap_ominous_bottle", SwapBottle::codec0);

    private static Item bottle;
    private static EntityType<?> bogged;

    static void register(IEventBus bus) {
        LOOT_MODIFIERS.register(bus);
    }

    static void scan() {
        bottle = find(ForgeRegistries.ITEMS, "ominous_bottle");
        bogged = find(ForgeRegistries.ENTITY_TYPES, "bogged");
        if (bottle != null) {
            Wildscapes.LOGGER.info("found {}, handing ominous bottles over to it", ForgeRegistries.ITEMS.getKey(bottle));
        }
        if (bogged != null) {
            Wildscapes.LOGGER.info("found {}, handing bogged over to it", ForgeRegistries.ENTITY_TYPES.getKey(bogged));
        }
    }

    private static <T> T find(IForgeRegistry<T> registry, String path) {
        for (var entry : registry.getEntries()) {
            ResourceLocation id = entry.getKey().location();
            if (id.getPath().equals(path) && !id.getNamespace().equals(Wildscapes.MODID)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static boolean hasBottle() {
        return bottle != null;
    }

    public static boolean hasBogged() {
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

    public static void onJoin(EntityJoinLevelEvent event) {
        if (bogged == null || event.loadedFromDisk() || !(event.getLevel() instanceof ServerLevel level)
                || event.getEntity().getType() != WildscapesEntities.BOGGED.get()) {
            return;
        }
        Mob ours = (Mob) event.getEntity();
        Entity made = bogged.create(level);
        if (!(made instanceof Mob theirs)) {
            return;
        }
        event.setCanceled(true);
        theirs.moveTo(ours.getX(), ours.getY(), ours.getZ(), ours.getYRot(), ours.getXRot());
        theirs.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(ours.position())),
                MobSpawnType.CONVERSION, null, null);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack item = ours.getItemBySlot(slot);
            if (!item.isEmpty()) {
                theirs.setItemSlot(slot, item.copy());
            }
        }
        theirs.getPersistentData().merge(ours.getPersistentData());
        if (ours.hasCustomName()) {
            theirs.setCustomName(ours.getCustomName());
        }
        if (ours.isPersistenceRequired()) {
            theirs.setPersistenceRequired();
        }
        level.addFreshEntity(theirs);
    }

    public static class SwapBottle extends LootModifier {
        SwapBottle(LootItemCondition[] conditions) {
            super(conditions);
        }

        static Codec<SwapBottle> codec0() {
            return RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, SwapBottle::new));
        }

        @Override
        protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
            if (bottle == null) {
                return loot;
            }
            loot.replaceAll(stack -> stack.is(WildscapesItems.OMINOUS_BOTTLE.get()) ? swapBottle(stack) : stack);
            return loot;
        }

        @Override
        public Codec<? extends IGlobalLootModifier> codec() {
            return SWAP_BOTTLE.get();
        }
    }
}
