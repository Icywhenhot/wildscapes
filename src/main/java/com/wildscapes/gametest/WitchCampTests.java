package com.wildscapes.gametest;

import java.util.ArrayList;
import java.util.List;

import com.wildscapes.worldgen.BuriedPiece;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
        int center = -1;
        List<StructurePiece> shafts = new ArrayList<>();
        List<StructurePiece> rooms = new ArrayList<>();
        List<StructurePiece> props = new ArrayList<>();
        int squares = 0;
        int hideouts = 0;
        int cells = 0;
        for (StructurePiece piece : start.getPieces()) {
            String id = ((PoolElementStructurePiece) piece).getElement().toString();
            boolean dungeon = id.contains("witch_camp/dungeon/");
            helper.assertTrue(dungeon == piece instanceof BuriedPiece,
                    "buried the wrong piece: " + id);
            if (id.contains("centrallocation")) center = piece.getBoundingBox().minY();
            if (id.contains("centralsquare")) squares++;
            if (id.contains("cabinwithentrance") || id.contains("shootingrange8")) hideouts++;
            if (id.contains("dungeonentrance")) shafts.add(piece);
            if (id.contains("dungeonloot")) rooms.add(piece);
            if (id.contains("dungeonpath")) cells++;
            if (id.contains("decor") || id.contains("streetlight")) props.add(piece);
        }

        helper.assertTrue(squares >= 3 && squares <= 5, "squares: " + squares);
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
        for (StructurePiece prop : props) {
            helper.assertTrue(prop.getBoundingBox().minY() == center,
                    "a prop stands at " + prop.getBoundingBox().minY() + " instead of the plaza's " + center);
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
