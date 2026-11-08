package com.wildscapes.gametest;

import java.util.function.Consumer;

import com.wildscapes.Wildscapes;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;

public final class WildscapesTests {
    private WildscapesTests() {}

    public static void register() {
        register("wanders_back_to_its_anchor", AbominationAnchorTests::wandersBackToItsAnchor);
        register("anchor_survives_a_save", AbominationAnchorTests::anchorSurvivesASave);
        register("tongue_bends_around_a_wall", AbominationTongueTests::tongueBendsAroundAWall);
        register("tongue_gives_up_on_a_sealed_target", AbominationTongueTests::tongueGivesUpOnASealedTarget);
        register("reel_walks_the_whole_path", AbominationTongueTests::reelWalksTheWholePath);
        register("soup_needs_to_be_collected_from_cauldron", AdvancementTests::soupNeedsToBeCollectedFromCauldron);
        register("necklace_needs_to_be_activated", AdvancementTests::necklaceNeedsToBeActivated);
        register("templates_apply_both_patterns_and_duplicate", ArmorTrimTests::templatesApplyBothPatternsAndDuplicate);
        register("harvest_awards_both_trims_and_camps_keep_them_rare", ArmorTrimTests::harvestAwardsBothTrimsAndCampsKeepThemRare);
        register("patterns_craft_and_work_in_the_loom_and_on_shields", BannerPatternTests::patternsCraftAndWorkInTheLoomAndOnShields);
        register("cypress_set_and_polish_hold_together", CypressSetTests::cypressSetAndPolishHoldTogether);
        register("potion_forms_share_the_green_tint", GnashingTests::potionFormsShareTheGreenTint);
        register("structures_and_soul_harvest_give_common_gnashing_loot", GnashingTests::structuresAndSoulHarvestGiveCommonGnashingLoot);
        register("zombie_gets_mirage", IllusionerMirageTests::zombieGetsMirage);
        register("clone_cooldown_and_memory", IllusionerMirageTests::cloneCooldownAndMemory);
        register("mobs_lose_intangible_player", IntangibilityTests::mobsLoseIntangiblePlayer);
        register("necklace_wears_out_while_active", IntangibilityTests::necklaceWearsOutWhileActive);
        register("player_crosses_solid_blocks_and_cobwebs", IntangibilityTests::playerCrossesSolidBlocksAndCobwebs);
        register("player_passes_through_wall", IntangibilityTests::playerPassesThroughWall);
        register("necklace_makes_player_invisible", IntangibilityTests::necklaceMakesPlayerInvisible);
        register("air_jumps_use_the_boots_and_reset_on_landing", JumpEnchantmentTests::airJumpsUseTheBootsAndResetOnLanding);
        register("powered_jump_charges_for_the_length_of_its_sound", JumpEnchantmentTests::poweredJumpChargesForTheLengthOfItsSound);
        register("jump_books_load_in_their_loot_pools", JumpEnchantmentTests::jumpBooksLoadInTheirLootPools);
        register("bonemeal_grows_a_drape", MossCarpetTests::bonemealGrowsADrape);
        register("vanilla_carpet_is_handed_over", MossCarpetTests::vanillaCarpetIsHandedOver);
        register("pav_shrooms_share_an_item_and_drop_by_hand", MushroomTests::pavShroomsShareAnItemAndDropByHand);
        register("pav_shrooms_heal_within_three_blocks", MushroomTests::pavShroomsHealWithinThreeBlocks);
        register("shelf_mushrooms_stay_near_the_ground", MushroomTests::shelfMushroomsStayNearTheGround);
        register("ground_mushroom_needs_soil", MushroomTests::groundMushroomNeedsSoil);
        register("shelf_mushroom_needs_a_trunk", MushroomTests::shelfMushroomNeedsATrunk);
        register("mangrove_keeps_vanilla_generation", MushroomTests::mangroveKeepsVanillaGeneration);
        register("omen_starts_it_and_souls_finish_it", SoulHarvestTests::omenStartsItAndSoulsFinishIt);
        register("camp_mobs_feed_the_bar", SoulHarvestTests::campMobsFeedTheBar);
        register("gnashing_raises_fangs", SoulHarvestTests::gnashingRaisesFangs);
        register("cauldron_bottles_rise_and_hunt_players", SoulHarvestTests::cauldronBottlesRiseAndHuntPlayers);
        register("breaking_the_cauldron_removes_its_bottles", SoulHarvestTests::breakingTheCauldronRemovesItsBottles);
        register("incendiary_fire_spares_mobs", SoulHarvestTests::incendiaryFireSparesMobs);
        register("land_tree_grows_as_cypress", TreePlacementTests::landTreeGrowsAsCypress);
        register("no_vine_is_left_clinging_to_nothing", TreePlacementTests::noVineIsLeftClingingToNothing);
        register("sapling_tree_hangs_no_flat_vines", TreePlacementTests::saplingTreeHangsNoFlatVines);
        register("trees_refuse_to_grow_through_each_other", TreePlacementTests::treesRefuseToGrowThroughEachOther);
        register("land_tree_refuses_water", TreePlacementTests::landTreeRefusesWater);
        register("water_tree_refuses_dry_land", TreePlacementTests::waterTreeRefusesDryLand);
        register("water_tree_refuses_deep_water", TreePlacementTests::waterTreeRefusesDeepWater);
        register("water_tree_accepts_shallow_water", TreePlacementTests::waterTreeAcceptsShallowWater);
        register("moss_covers_some_of_the_tree_tops", TreePlacementTests::mossCoversSomeOfTheTreeTops);
        register("hanging_moss_always_has_a_carpet_holding_it", TreePlacementTests::hangingMossAlwaysHasACarpetHoldingIt);
        register("railings_meet_across_a_seam", VillageJoinTests::railingsMeetAcrossASeam);
        register("railings_meet_on_a_turned_piece", VillageJoinTests::railingsMeetOnATurnedPiece);
        register("a_run_of_railing_survives_being_turned", VillageJoinTests::aRunOfRailingSurvivesBeingTurned);
        register("a_lone_railing_stays_bare", VillageJoinTests::aLoneRailingStaysBare);
        register("a_turf_floor_survives_the_neighbour", VillageTerrainTests::aTurfFloorSurvivesTheNeighbour);
        register("ground_on_decking_survives_unannounced", VillageTerrainTests::groundOnDeckingSurvivesUnannounced);
        register("a_hillside_is_still_dug_out", VillageTerrainTests::aHillsideIsStillDugOut);
        register("map_keeps_its_destination_when_saved_and_copied", WitchCampMapTests::mapKeepsItsDestinationWhenSavedAndCopied);
        register("camp_joins_up", WitchCampTests::campJoinsUp);
        register("ritual_turns_the_toad_into_an_abomination", WitchRitualTests::ritualTurnsTheToadIntoAnAbomination);
    }

    private static void register(String id, Consumer<GameTestHelper> test) {
        Registry.register(BuiltInRegistries.TEST_FUNCTION, Wildscapes.id(id), test);
    }
}
