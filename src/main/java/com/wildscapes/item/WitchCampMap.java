package com.wildscapes.item;

import com.wildscapes.Wildscapes;

import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

import net.minecraft.core.Registry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;

public final class WitchCampMap {
    private WitchCampMap() {}

    public static final Holder.Reference<MapDecorationType> WITCH_CAMP =
            Registry.registerForHolder(BuiltInRegistries.MAP_DECORATION_TYPE, Wildscapes.id("witch_camp"),
                    new MapDecorationType(Wildscapes.id("witch_camp"), true, -1, true, false));

    public static final TagKey<Structure> DESTINATION = TagKey.create(Registries.STRUCTURE,
            Identifier.fromNamespaceAndPath(Wildscapes.MODID, "on_witch_camp_maps"));

    public static void register() {
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.CARTOGRAPHER, 4, offers ->
                offers.add(new VillagerTrades.TreasureMapForEmeralds(14, DESTINATION,
                        "filled_map.wildscapes.witch_camp", WITCH_CAMP, 12, 30)));
        TradeOfferHelper.registerWanderingTraderOffers(offers -> offers.addOffersToPool(
                TradeOfferHelper.WanderingTraderOffersBuilder.SELL_COMMON_ITEMS_POOL,
                (level, trader, random) -> new MerchantOffer(new ItemCost(Items.EMERALD, 8),
                        new ItemStack(WildscapesItems.MERCHANTS_GUIDE), 1, 1, 0.05F)));
    }

    public static boolean isWitchCampMap(ItemStack stack) {
        return stack.getOrDefault(DataComponents.MAP_DECORATIONS, MapDecorations.EMPTY)
                .decorations().values().stream().anyMatch(entry -> entry.type().is(WITCH_CAMP.key()));
    }
}
