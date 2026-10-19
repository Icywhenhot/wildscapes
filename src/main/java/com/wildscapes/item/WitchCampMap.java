package com.wildscapes.item;

import com.wildscapes.Wildscapes;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class WitchCampMap {
    private WitchCampMap() {}

    private static final DeferredRegister<MapDecorationType> DECORATIONS =
            DeferredRegister.create(Registries.MAP_DECORATION_TYPE, Wildscapes.MODID);

    public static final DeferredHolder<MapDecorationType, MapDecorationType> WITCH_CAMP =
            DECORATIONS.register("witch_camp", () -> new MapDecorationType(
                    ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "witch_camp"), true, -1, true, false));

    public static final TagKey<Structure> DESTINATION = TagKey.create(Registries.STRUCTURE,
            ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "on_witch_camp_maps"));

    public static void register(IEventBus bus) {
        DECORATIONS.register(bus);
    }

    public static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() == VillagerProfession.CARTOGRAPHER) {
            event.getTrades().get(4).add(new VillagerTrades.TreasureMapForEmeralds(
                    14, DESTINATION, "filled_map.wildscapes.witch_camp", WITCH_CAMP, 12, 30));
        }
    }

    public static boolean isWitchCampMap(ItemStack stack) {
        return stack.getOrDefault(DataComponents.MAP_DECORATIONS, MapDecorations.EMPTY)
                .decorations().values().stream().anyMatch(entry -> entry.type().is(WITCH_CAMP.getKey()));
    }
}
