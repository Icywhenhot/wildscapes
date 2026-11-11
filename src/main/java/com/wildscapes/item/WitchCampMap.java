package com.wildscapes.item;

import com.wildscapes.Wildscapes;

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
import net.minecraftforge.event.village.VillagerTradesEvent;

public final class WitchCampMap {
    private WitchCampMap() {}

    public static final String MARKER = "wildscapes:witch_camp";

    public static final TagKey<Structure> DESTINATION = TagKey.create(Registries.STRUCTURE,
            ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "on_witch_camp_maps"));

    public static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() == VillagerProfession.CARTOGRAPHER) {
            VillagerTrades.TreasureMapForEmeralds map = new VillagerTrades.TreasureMapForEmeralds(
                    14, DESTINATION, "filled_map.wildscapes.witch_camp", MapDecoration.Type.RED_X, 12, 30);
            event.getTrades().get(4).add((trader, random) -> {
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
            });
        }
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
