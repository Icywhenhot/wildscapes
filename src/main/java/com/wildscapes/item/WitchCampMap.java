package com.wildscapes.item;

import com.wildscapes.Wildscapes;

import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecoration;

public final class WitchCampMap {
    private WitchCampMap() {}

    public static final String MARKER = "wildscapes:witch_camp";

    public static final TagKey<Structure> DESTINATION = TagKey.create(Registries.STRUCTURE,
            new ResourceLocation(Wildscapes.MODID, "on_witch_camp_maps"));

    public static void register() {
        VillagerTrades.TreasureMapForEmeralds map = new VillagerTrades.TreasureMapForEmeralds(
                14, DESTINATION, "filled_map.wildscapes.witch_camp", MapDecoration.Type.RED_X, 12, 30);
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.CARTOGRAPHER, 4, offers ->
            offers.add((trader, random) -> {
                var offer = map.getOffer(trader, random);
                if (offer != null) {
                    var data = MapItem.getSavedData(offer.getResult(), trader.level());
                    ListTag decorations = offer.getResult().getOrCreateTag().getList("Decorations", Tag.TAG_COMPOUND);
                    for (int i = 0; i < decorations.size(); i++) {
                        var tag = decorations.getCompound(i);
                        tag.putString("id", MARKER);
                        if (data != null) {
                            tag.putInt("center_x", data.centerX);
                            tag.putInt("center_z", data.centerZ);
                        }
                    }
                }
                return offer;
            }));
        TradeOfferHelper.registerWanderingTraderOffers(1, offers -> offers.add(
                (trader, random) -> new MerchantOffer(new ItemStack(Items.EMERALD, 8),
                        new ItemStack(WildscapesItems.MERCHANTS_GUIDE.get()), 1, 1, 0.05F)));
    }

    public static boolean isWitchCampMap(ItemStack stack) {
        if (!stack.hasTag()) {
            return false;
        }
        ListTag decorations = stack.getTag().getList("Decorations", Tag.TAG_COMPOUND);
        for (int i = 0; i < decorations.size(); i++) {
            if (MARKER.equals(decorations.getCompound(i).getString("id"))) {
                return true;
            }
        }
        return false;
    }
}
