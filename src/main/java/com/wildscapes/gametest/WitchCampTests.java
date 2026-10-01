package com.wildscapes.gametest;

import java.util.ArrayList;
import java.util.List;

import com.wildscapes.entity.WildscapesEntities;
import com.wildscapes.worldgen.BuriedPiece;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("wildscapes")
@PrefixGameTestTemplate(false)
public class WitchCampTests {
    private static final String PLATFORM = "gametest/tree_platform";

    @GameTest(template = PLATFORM, timeoutTicks = 400)
    public static void campJoinsUp(GameTestHelper helper) {
        ServerLevel level = helper.getLevel().getServer().getLevel(Level.NETHER);
        for (int camp = 0; camp < 6; camp++) {
            StructureStart start = null;
            for (int i = 0; i < 40 && (start == null || !start.isValid()); i++) {
                start = generate(level, new ChunkPos(400 + i * 16, 400 + camp * 64));
            }
            helper.assertTrue(start != null && start.isValid(), "no witch camp came out of 40 tries");
            place(level, start);
            check(helper, level, start);
        }
        helper.succeed();
    }

    private static void check(GameTestHelper helper, ServerLevel level, StructureStart start) {
        List<StructurePiece> shafts = new ArrayList<>();
        List<StructurePiece> rooms = new ArrayList<>();
        List<StructurePiece> prisons = new ArrayList<>();
        List<StructurePiece> props = new ArrayList<>();
        int center = -1;
        int lamps = 0;
        int abominations = 0;
        int vindicators = 0;
        int witches = 0;
        int squares = 0;
        int hideouts = 0;
        int cells = 0;
        for (StructurePiece piece : start.getPieces()) {
            String id = ((PoolElementStructurePiece) piece).getElement().toString();
            boolean dungeon = id.contains("witch_camp/dungeon/");
            helper.assertTrue(dungeon == piece instanceof BuriedPiece,
                    "buried the wrong piece: " + id);
            if (id.contains("centralsquare")) squares++;
            if (id.contains("cabinwithentrance") || id.contains("strandedhut")) hideouts++;
            if (id.contains("dungeonentrance")) shafts.add(piece);
            if (id.contains("dungeonloot")) rooms.add(piece);
            if (id.contains("dungeonpath")) cells++;
            if (id.contains("dungeonpath1")) prisons.add(piece);
            if (id.contains("centrallocation")) center = piece.getBoundingBox().minY();
            if (id.contains("streetlight")) lamps++;
            if (id.contains("streetlight") || id.contains("decor")) props.add(piece);
            if (id.contains("mob_abomination")) abominations++;
            if (id.contains("mob_vindicator")) vindicators++;
            if (id.contains("mob_witch")) witches++;
        }

        helper.assertTrue(squares >= 3 && squares <= 5, "squares: " + squares);
        helper.assertTrue(abominations == 1, abominations + " abominations guarding the centre");
        helper.assertTrue(vindicators >= 2 && witches >= 2,
                vindicators + " vindicators and " + witches + " witches, the centre alone needs 2 of each");
        helper.assertTrue(level.getEntities(WildscapesEntities.ABOMINATION.get(), AABB.of(start.getBoundingBox()),
                e -> e.isPersistenceRequired()).size() == 1, "the abomination did not turn up, or will despawn");
        helper.assertTrue(!shafts.isEmpty(), "no dungeon entrance at all");
        helper.assertTrue(shafts.size() <= hideouts, shafts.size() + " shafts for " + hideouts + " trapdoors");
        helper.assertTrue(rooms.size() == shafts.size(), rooms.size() + " loot rooms under " + shafts.size() + " shafts");
        helper.assertTrue(cells >= rooms.size(), cells + " cells off " + rooms.size() + " loot rooms");

        for (StructurePiece shaft : shafts) {
            BoundingBox box = shaft.getBoundingBox();
            BlockPos top = new BlockPos(box.getCenter().getX(), box.maxY(), box.getCenter().getZ());
            helper.assertTrue(level.getBlockState(top).is(Blocks.LADDER), "no ladder at the top of the shaft " + top);
            boolean hatch = false;
            for (int up = 1; up <= 3 && !hatch; up++) {
                hatch = level.getBlockState(top.above(up)).getBlock() instanceof TrapDoorBlock;
            }
            helper.assertTrue(hatch, "the shaft at " + top + " does not open under a trapdoor");
        }
        for (StructurePiece room : rooms) {
            BoundingBox box = room.getBoundingBox();
            boolean rung = false;
            for (BlockPos p : BlockPos.betweenClosed(box.minX(), box.maxY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
                if (level.getBlockState(p).is(Blocks.LADDER) && level.getBlockState(p.above()).is(Blocks.LADDER)) {
                    rung = true;
                }
            }
            helper.assertTrue(rung, "the loot room at " + box + " has no ladder meeting the shaft");
        }
        helper.assertTrue(lamps >= 3, "only " + lamps + " lamps, the centre alone has 3");
        for (StructurePiece prop : props) {
            helper.assertTrue(prop.getBoundingBox().minY() == center,
                    "a lamp or patch stands at " + prop.getBoundingBox().minY() + " instead of the ground at " + center);
        }
        for (StructurePiece prison : prisons) {
            AABB inside = AABB.of(prison.getBoundingBox());
            helper.assertTrue(level.getEntities(EntityType.VILLAGER, inside, e -> true).size() == 1
                    && level.getEntities(EntityType.ZOMBIE_VILLAGER, inside, e -> true).size() >= 1,
                    "the first dungeon path should hold one villager and a zombie villager");
        }
    }

    private static StructureStart generate(ServerLevel level, ChunkPos chunk) {
        Structure camp = level.registryAccess().registryOrThrow(Registries.STRUCTURE)
                .get(ResourceLocation.fromNamespaceAndPath("wildscapes", "witch_camp"));
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        return camp.generate(level.registryAccess(), generator, generator.getBiomeSource(),
                level.getChunkSource().randomState(), level.getStructureManager(), level.getSeed(),
                chunk, 0, level, biome -> true);
    }

    private static void place(ServerLevel level, StructureStart start) {
        BoundingBox all = start.getBoundingBox();
        for (int cx = (all.minX() >> 4) - 1; cx <= (all.maxX() >> 4) + 1; cx++) {
            for (int cz = (all.minZ() >> 4) - 1; cz <= (all.maxZ() >> 4) + 1; cz++) {
                level.getChunk(cx, cz);
            }
        }
        for (int cx = all.minX() >> 4; cx <= all.maxX() >> 4; cx++) {
            for (int cz = all.minZ() >> 4; cz <= all.maxZ() >> 4; cz++) {
                ChunkPos chunk = new ChunkPos(cx, cz);
                level.getChunk(cx, cz);
                BoundingBox box = new BoundingBox(chunk.getMinBlockX(), level.getMinBuildHeight(), chunk.getMinBlockZ(),
                        chunk.getMaxBlockX(), level.getMaxBuildHeight(), chunk.getMaxBlockZ());
                start.placeInChunk(level, level.structureManager(), level.getChunkSource().getGenerator(),
                        level.getRandom(), box, chunk);
            }
        }
    }
}
