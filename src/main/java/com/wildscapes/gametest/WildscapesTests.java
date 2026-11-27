package com.wildscapes.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public final class WildscapesTests {

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 600)
    public void wanders_back_to_its_anchor(GameTestHelper helper) {
        AbominationAnchorTests.wandersBackToItsAnchor(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void anchor_survives_a_save(GameTestHelper helper) {
        AbominationAnchorTests.anchorSurvivesASave(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void tongue_bends_around_a_wall(GameTestHelper helper) {
        AbominationTongueTests.tongueBendsAroundAWall(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void tongue_gives_up_on_a_sealed_target(GameTestHelper helper) {
        AbominationTongueTests.tongueGivesUpOnASealedTarget(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void reel_walks_the_whole_path(GameTestHelper helper) {
        AbominationTongueTests.reelWalksTheWholePath(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void soup_needs_to_be_collected_from_cauldron(GameTestHelper helper) {
        AdvancementTests.soupNeedsToBeCollectedFromCauldron(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void necklace_needs_to_be_activated(GameTestHelper helper) {
        AdvancementTests.necklaceNeedsToBeActivated(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void templates_apply_both_patterns_and_duplicate(GameTestHelper helper) {
        ArmorTrimTests.templatesApplyBothPatternsAndDuplicate(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void harvest_awards_both_trims_and_camps_keep_them_rare(GameTestHelper helper) {
        ArmorTrimTests.harvestAwardsBothTrimsAndCampsKeepThemRare(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void patterns_craft_and_work_in_the_loom_and_on_shields(GameTestHelper helper) {
        BannerPatternTests.patternsCraftAndWorkInTheLoomAndOnShields(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void cypress_set_and_polish_hold_together(GameTestHelper helper) {
        CypressSetTests.cypressSetAndPolishHoldTogether(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void potion_forms_share_the_green_tint(GameTestHelper helper) {
        GnashingTests.potionFormsShareTheGreenTint(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void structures_and_soul_harvest_give_common_gnashing_loot(GameTestHelper helper) {
        GnashingTests.structuresAndSoulHarvestGiveCommonGnashingLoot(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 80)
    public void zombie_gets_mirage(GameTestHelper helper) {
        IllusionerMirageTests.zombieGetsMirage(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 540)
    public void clone_cooldown_and_memory(GameTestHelper helper) {
        IllusionerMirageTests.cloneCooldownAndMemory(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void mobs_lose_intangible_player(GameTestHelper helper) {
        IntangibilityTests.mobsLoseIntangiblePlayer(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void necklace_wears_out_while_active(GameTestHelper helper) {
        IntangibilityTests.necklaceWearsOutWhileActive(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void player_crosses_solid_blocks_and_cobwebs(GameTestHelper helper) {
        IntangibilityTests.playerCrossesSolidBlocksAndCobwebs(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void player_passes_through_wall(GameTestHelper helper) {
        IntangibilityTests.playerPassesThroughWall(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void necklace_makes_player_invisible(GameTestHelper helper) {
        IntangibilityTests.necklaceMakesPlayerInvisible(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void air_jumps_use_the_boots_and_reset_on_landing(GameTestHelper helper) {
        JumpEnchantmentTests.airJumpsUseTheBootsAndResetOnLanding(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void powered_jump_charges_for_the_length_of_its_sound(GameTestHelper helper) {
        JumpEnchantmentTests.poweredJumpChargesForTheLengthOfItsSound(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void jump_books_load_in_their_loot_pools(GameTestHelper helper) {
        JumpEnchantmentTests.jumpBooksLoadInTheirLootPools(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void bonemeal_grows_a_drape(GameTestHelper helper) {
        MossCarpetTests.bonemealGrowsADrape(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void vanilla_carpet_is_handed_over(GameTestHelper helper) {
        MossCarpetTests.vanillaCarpetIsHandedOver(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void pav_shrooms_share_an_item_and_drop_by_hand(GameTestHelper helper) {
        MushroomTests.pavShroomsShareAnItemAndDropByHand(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void pav_shrooms_heal_within_three_blocks(GameTestHelper helper) {
        MushroomTests.pavShroomsHealWithinThreeBlocks(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void shelf_mushrooms_stay_near_the_ground(GameTestHelper helper) {
        MushroomTests.shelfMushroomsStayNearTheGround(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void ground_mushroom_needs_soil(GameTestHelper helper) {
        MushroomTests.groundMushroomNeedsSoil(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void shelf_mushroom_needs_a_trunk(GameTestHelper helper) {
        MushroomTests.shelfMushroomNeedsATrunk(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void mangrove_keeps_vanilla_generation(GameTestHelper helper) {
        MushroomTests.mangroveKeepsVanillaGeneration(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void captains_drop_an_ominous_bottle(GameTestHelper helper) {
        SoulHarvestTests.captainsDropAnOminousBottle(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 4000)
    public void omen_starts_it_and_souls_finish_it(GameTestHelper helper) {
        SoulHarvestTests.omenStartsItAndSoulsFinishIt(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 1200)
    public void camp_mobs_feed_the_bar(GameTestHelper helper) {
        SoulHarvestTests.campMobsFeedTheBar(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 200)
    public void gnashing_raises_fangs(GameTestHelper helper) {
        SoulHarvestTests.gnashingRaisesFangs(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 600)
    public void cauldron_bottles_rise_and_hunt_players(GameTestHelper helper) {
        SoulHarvestTests.cauldronBottlesRiseAndHuntPlayers(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 60)
    public void breaking_the_cauldron_removes_its_bottles(GameTestHelper helper) {
        SoulHarvestTests.breakingTheCauldronRemovesItsBottles(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void incendiary_fire_spares_mobs(GameTestHelper helper) {
        SoulHarvestTests.incendiaryFireSparesMobs(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void land_tree_grows_as_cypress(GameTestHelper helper) {
        TreePlacementTests.landTreeGrowsAsCypress(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void no_vine_is_left_clinging_to_nothing(GameTestHelper helper) {
        TreePlacementTests.noVineIsLeftClingingToNothing(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void sapling_tree_hangs_no_flat_vines(GameTestHelper helper) {
        TreePlacementTests.saplingTreeHangsNoFlatVines(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void trees_refuse_to_grow_through_each_other(GameTestHelper helper) {
        TreePlacementTests.treesRefuseToGrowThroughEachOther(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void land_tree_refuses_water(GameTestHelper helper) {
        TreePlacementTests.landTreeRefusesWater(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void water_tree_refuses_dry_land(GameTestHelper helper) {
        TreePlacementTests.waterTreeRefusesDryLand(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void water_tree_refuses_deep_water(GameTestHelper helper) {
        TreePlacementTests.waterTreeRefusesDeepWater(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void water_tree_accepts_shallow_water(GameTestHelper helper) {
        TreePlacementTests.waterTreeAcceptsShallowWater(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void moss_covers_some_of_the_tree_tops(GameTestHelper helper) {
        TreePlacementTests.mossCoversSomeOfTheTreeTops(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void hanging_moss_always_has_a_carpet_holding_it(GameTestHelper helper) {
        TreePlacementTests.hangingMossAlwaysHasACarpetHoldingIt(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void railings_meet_across_a_seam(GameTestHelper helper) {
        VillageJoinTests.railingsMeetAcrossASeam(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void railings_meet_on_a_turned_piece(GameTestHelper helper) {
        VillageJoinTests.railingsMeetOnATurnedPiece(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void a_run_of_railing_survives_being_turned(GameTestHelper helper) {
        VillageJoinTests.aRunOfRailingSurvivesBeingTurned(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void a_lone_railing_stays_bare(GameTestHelper helper) {
        VillageJoinTests.aLoneRailingStaysBare(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void a_turf_floor_survives_the_neighbour(GameTestHelper helper) {
        VillageTerrainTests.aTurfFloorSurvivesTheNeighbour(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void ground_on_decking_survives_unannounced(GameTestHelper helper) {
        VillageTerrainTests.groundOnDeckingSurvivesUnannounced(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void a_hillside_is_still_dug_out(GameTestHelper helper) {
        VillageTerrainTests.aHillsideIsStillDugOut(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void map_keeps_its_destination_when_saved_and_copied(GameTestHelper helper) {
        WitchCampMapTests.mapKeepsItsDestinationWhenSavedAndCopied(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 400)
    public void camp_joins_up(GameTestHelper helper) {
        WitchCampTests.campJoinsUp(helper);
    }

    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 600)
    public void ritual_turns_the_toad_into_an_abomination(GameTestHelper helper) {
        WitchRitualTests.ritualTurnsTheToadIntoAnAbomination(helper);
    }
    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void shelf_stores_items_and_swaps_hotbars(GameTestHelper helper) {
        CypressShelfTests.storesItemsAndSwapsHotbars(helper);
    }
    @GameTest(template = "wildscapes:gametest/tree_platform", timeoutTicks = 100)
    public void boats_keep_their_type_when_placed_and_dispensed(GameTestHelper helper) {
        CypressBoatTests.placingAndDispensingKeepTheType(helper);
    }
}
