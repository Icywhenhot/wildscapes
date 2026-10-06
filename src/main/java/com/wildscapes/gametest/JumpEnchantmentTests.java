package com.wildscapes.gametest;

import com.wildscapes.entity.JumpEnchantments;

import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("wildscapes")
@PrefixGameTestTemplate(false)
public final class JumpEnchantmentTests {
    private static final String PLATFORM = "gametest/tree_platform";

    private JumpEnchantmentTests() {}

    @GameTest(template = PLATFORM, batch = "jump_enchantments")
    public static void airJumpsUseTheBootsAndResetOnLanding(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        var enchantment = helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(JumpEnchantments.MULTI_JUMP);
        ItemStack boots = new ItemStack(Items.DIAMOND_BOOTS);
        boots.enchant(enchantment, 1);
        player.setItemSlot(EquipmentSlot.FEET, boots);
        player.setOnGround(true);
        helper.assertFalse(JumpEnchantments.jump(player, false), "air jump worked on the ground");
        player.setOnGround(false);
        player.setDeltaMovement(0.1, -0.8, 0.2);
        player.fallDistance = 12.0F;
        helper.assertTrue(JumpEnchantments.jump(player, false), "level one could not jump while falling");
        helper.assertTrue(player.getDeltaMovement().y > 0.0 && player.fallDistance == 0.0F,
                "air jump did not stop the fall");
        helper.assertFalse(JumpEnchantments.jump(player, false), "level one allowed a second air jump");
        player.setOnGround(true);
        JumpEnchantments.onTick(new PlayerTickEvent.Post(player));
        boots.enchant(enchantment, 2);
        player.setOnGround(false);
        helper.assertTrue(JumpEnchantments.jump(player, false), "landing did not restore air jumps");
        helper.assertTrue(JumpEnchantments.jump(player, false), "level two did not allow two air jumps");
        helper.assertFalse(JumpEnchantments.jump(player, false), "level two allowed a third air jump");
        player.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
        player.setOnGround(true);
        JumpEnchantments.onTick(new PlayerTickEvent.Post(player));
        player.setOnGround(false);
        helper.assertFalse(JumpEnchantments.jump(player, false), "air jump worked without enchanted boots");
        helper.assertTrue(enchantment.value().isSupportedItem(new ItemStack(Items.IRON_BOOTS)), "boots rejected multi jump");
        helper.assertFalse(enchantment.value().isSupportedItem(new ItemStack(Items.IRON_LEGGINGS)), "leggings accepted multi jump");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, batch = "jump_enchantments")
    public static void poweredJumpChargesFromThreeToTenSeconds(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        var enchantment = helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(JumpEnchantments.EMPOWERED_JUMP);
        ItemStack leggings = new ItemStack(Items.DIAMOND_LEGGINGS);
        leggings.enchant(enchantment, 1);
        player.setItemSlot(EquipmentSlot.LEGS, leggings);
        player.setOnGround(true);
        player.setShiftKeyDown(true);
        for (int i = 0; i < 59; i++) {
            JumpEnchantments.onTick(new PlayerTickEvent.Post(player));
        }
        helper.assertFalse(JumpEnchantments.jump(player, true), "powered jump launched before three seconds");
        JumpEnchantments.onTick(new PlayerTickEvent.Post(player));
        player.setDeltaMovement(0.4, 0.0, -0.3);
        helper.assertTrue(JumpEnchantments.jump(player, true), "powered jump did not launch at three seconds");
        double low = player.getDeltaMovement().y;
        helper.assertTrue(low > 0.42 && player.getDeltaMovement().x == 0.0 && player.getDeltaMovement().z == 0.0,
                "powered jump did not launch straight up");
        helper.assertFalse(JumpEnchantments.jump(player, true), "powered jump reused its charge in midair");
        player.setOnGround(true);
        for (int i = 0; i < 200; i++) {
            JumpEnchantments.onTick(new PlayerTickEvent.Post(player));
        }
        helper.assertTrue(JumpEnchantments.jump(player, true), "full charge did not launch");
        double full = player.getDeltaMovement().y;
        helper.assertTrue(full > low, "charging longer did not increase jump height");
        player.setOnGround(true);
        for (int i = 0; i < 240; i++) {
            JumpEnchantments.onTick(new PlayerTickEvent.Post(player));
        }
        helper.assertTrue(JumpEnchantments.jump(player, true), "overcharged jump did not launch");
        helper.assertTrue(player.getDeltaMovement().y == full, "charge kept growing beyond ten seconds");
        player.setOnGround(true);
        for (int i = 0; i < 60; i++) {
            JumpEnchantments.onTick(new PlayerTickEvent.Post(player));
        }
        player.setShiftKeyDown(false);
        JumpEnchantments.onTick(new PlayerTickEvent.Post(player));
        player.setShiftKeyDown(true);
        helper.assertFalse(JumpEnchantments.jump(player, true), "uncrouching kept a stored charge");
        helper.assertTrue(enchantment.value().isSupportedItem(new ItemStack(Items.IRON_LEGGINGS)), "leggings rejected empowered jump");
        helper.assertFalse(enchantment.value().isSupportedItem(new ItemStack(Items.IRON_BOOTS)), "boots accepted empowered jump");
        player.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
        helper.assertFalse(JumpEnchantments.canCharge(player), "charge worked without enchanted leggings");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, batch = "jump_enchantments")
    public static void jumpBooksLoadInTheirLootPools(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        LootParams params = new LootParams.Builder(helper.getLevel())
                .withParameter(LootContextParams.ORIGIN, Vec3.ZERO)
                .withParameter(LootContextParams.THIS_ENTITY, player)
                .create(LootContextParamSets.VAULT);
        for (String tier : new String[] {"low", "high"}) {
            ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE,
                    ResourceLocation.fromNamespaceAndPath("wildscapes", "gameplay/soul_harvest_" + tier));
            LootTable table = helper.getLevel().getServer().reloadableRegistries().getLootTable(key);
            int books = 0;
            for (int i = 0; i < 100; i++) {
                for (ItemStack stack : table.getRandomItems(params)) {
                    helper.assertTrue(stack.is(Items.ENCHANTED_BOOK), "jump pool returned a non-book reward");
                    var enchantments = stack.get(net.minecraft.core.component.DataComponents.STORED_ENCHANTMENTS);
                    var expected = tier.equals("low") ? JumpEnchantments.MULTI_JUMP : JumpEnchantments.EMPOWERED_JUMP;
                    helper.assertTrue(enchantments != null && enchantments.keySet().stream().allMatch(e -> e.is(expected)),
                            "jump book appeared in the wrong tier");
                    books++;
                }
            }
            helper.assertTrue(books > 0 && books < 100, "jump reward pool was missing or had no chance roll");
        }
        helper.succeed();
    }
}
