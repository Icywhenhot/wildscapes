package com.wildscapes;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.wildscapes.block.WildscapesBlocks;
import com.wildscapes.entity.AbominationEntity;
import com.wildscapes.entity.SwampVariants;
import com.wildscapes.entity.WildscapesEntities;
import com.wildscapes.item.WildscapesItems;
import com.wildscapes.sound.WildscapesSounds;
import com.wildscapes.worldgen.WildscapesFeatures;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(Wildscapes.MODID)
public class Wildscapes {
    public static final String MODID = "wildscapes";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> WILDSCAPES_TAB =
            CREATIVE_MODE_TABS.register("wildscapes", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.wildscapes"))
                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                    .icon(() -> WildscapesBlocks.CYPRESS_SAPLING.get().asItem().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(WildscapesBlocks.CYPRESS_LOG.get());
                        output.accept(WildscapesBlocks.STRIPPED_CYPRESS_LOG.get());
                        output.accept(WildscapesBlocks.CYPRESS_WOOD.get());
                        output.accept(WildscapesBlocks.STRIPPED_CYPRESS_WOOD.get());
                        output.accept(WildscapesBlocks.CYPRESS_PLANKS.get());
                        output.accept(WildscapesBlocks.CYPRESS_LEAVES.get());
                        output.accept(WildscapesBlocks.CYPRESS_SAPLING.get());
                        output.accept(WildscapesBlocks.CYPRESS_DOOR.get());
                        output.accept(WildscapesBlocks.CYPRESS_TRAPDOOR.get());
                        output.accept(WildscapesBlocks.DUCKWEED.get());
                        output.accept(WildscapesBlocks.RUSHES.get());
                        output.accept(WildscapesBlocks.BONFIRE.get());
                        output.accept(WildscapesBlocks.WITCH_CAULDRON.get());
                        output.accept(WildscapesItems.FROG_LEGS.get());
                        output.accept(WildscapesItems.ABOMINATION_SPAWN_EGG.get());
                    }).build());

    public Wildscapes(IEventBus modEventBus) {
        WildscapesBlocks.register(modEventBus);
        WildscapesItems.register(modEventBus);
        WildscapesEntities.register(modEventBus);
        SwampVariants.register(modEventBus);
        WildscapesSounds.register(modEventBus);
        WildscapesFeatures.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerEntityAttributes);

        NeoForge.EVENT_BUS.addListener(this::onLivingDrops);
    }

    /** Make regular vanilla frogs drop frog legs when killed. */
    private void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getType() != EntityType.FROG) {
            return;
        }
        int count = 1 + entity.getRandom().nextInt(2); // 1–2 legs
        ItemStack stack = new ItemStack(WildscapesItems.FROG_LEGS.get(), count);
        event.getDrops().add(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), stack));
    }

    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(WildscapesEntities.ABOMINATION.get(), AbominationEntity.createAttributes().build());
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            FireBlock fire = (FireBlock) Blocks.FIRE;
            fire.setFlammable(WildscapesBlocks.CYPRESS_LOG.get(), 5, 5);
            fire.setFlammable(WildscapesBlocks.STRIPPED_CYPRESS_LOG.get(), 5, 5);
            fire.setFlammable(WildscapesBlocks.CYPRESS_WOOD.get(), 5, 5);
            fire.setFlammable(WildscapesBlocks.STRIPPED_CYPRESS_WOOD.get(), 5, 5);
            fire.setFlammable(WildscapesBlocks.CYPRESS_PLANKS.get(), 5, 20);
            fire.setFlammable(WildscapesBlocks.CYPRESS_LEAVES.get(), 30, 60);
        });
    }
}
