package com.wildscapes.entity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public final class TonguePath {
    private static final int BUDGET = 700;
    private static final double SPREAD = 20.0D;
    private static final int LOOKAHEAD = 8;

    private TonguePath() {}

    public static boolean clear(Level level, Vec3 from, Vec3 to) {
        return level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
                CollisionContext.empty())).getType() == HitResult.Type.MISS;
    }

    @Nullable
    public static List<Vec3> find(Level level, Vec3 from, Vec3 to) {
        if (clear(level, from, to)) {
            return List.of(from, to);
        }

        BlockPos start = BlockPos.containing(from);
        BlockPos goal = BlockPos.containing(to);
        if (!passable(level, goal)) {
            return null;
        }

        Map<BlockPos, BlockPos> came = new HashMap<>();
        Map<BlockPos, Double> cost = new HashMap<>();
        PriorityQueue<BlockPos> open = new PriorityQueue<>(
                Comparator.comparingDouble(p -> cost.getOrDefault(p, 0.0D) + Math.sqrt(p.distSqr(goal))));
        cost.put(start, 0.0D);
        open.add(start);

        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        int visited = 0;
        while (!open.isEmpty() && visited++ < BUDGET) {
            BlockPos cur = open.poll();
            if (cur.equals(goal)) {
                return smooth(level, from, to, trace(came, start, cur));
            }
            double base = cost.get(cur);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) {
                            continue;
                        }
                        m.set(cur.getX() + dx, cur.getY() + dy, cur.getZ() + dz);
                        if (Math.sqrt(m.distSqr(start)) > SPREAD || !passable(level, m)) {
                            continue;
                        }
                        double next = base + Math.sqrt(dx * dx + dy * dy + dz * dz);
                        BlockPos pos = m.immutable();
                        Double seen = cost.get(pos);
                        if (seen == null || next < seen) {
                            cost.put(pos, next);
                            came.put(pos, cur);
                            open.add(pos);
                        }
                    }
                }
            }
        }
        return null;
    }

    public static Vec3 pointAt(List<Vec3> path, double u) {
        if (path.size() < 2) {
            return path.isEmpty() ? Vec3.ZERO : path.get(0);
        }
        double total = 0.0D;
        for (int i = 1; i < path.size(); i++) {
            total += path.get(i).distanceTo(path.get(i - 1));
        }
        double want = total * u;
        for (int i = 1; i < path.size(); i++) {
            double leg = path.get(i).distanceTo(path.get(i - 1));
            if (want <= leg || i == path.size() - 1) {
                return path.get(i - 1).lerp(path.get(i), leg < 1.0E-6D ? 0.0D : want / leg);
            }
            want -= leg;
        }
        return path.get(path.size() - 1);
    }

    private static boolean passable(Level level, BlockPos pos) {
        return !level.getBlockState(pos).blocksMotion();
    }

    private static List<BlockPos> trace(Map<BlockPos, BlockPos> came, BlockPos start, BlockPos end) {
        Deque<BlockPos> out = new ArrayDeque<>();
        BlockPos cur = end;
        while (cur != null && !cur.equals(start)) {
            out.addFirst(cur);
            cur = came.get(cur);
        }
        return new ArrayList<>(out);
    }

    private static List<Vec3> smooth(Level level, Vec3 from, Vec3 to, List<BlockPos> nodes) {
        List<Vec3> pts = new ArrayList<>();
        pts.add(from);
        for (BlockPos p : nodes) {
            pts.add(Vec3.atCenterOf(p));
        }
        pts.set(pts.size() - 1, to);

        List<Vec3> out = new ArrayList<>();
        out.add(pts.get(0));
        int i = 0;
        while (i < pts.size() - 1) {
            int far = i + 1;
            for (int j = Math.min(pts.size() - 1, i + LOOKAHEAD); j > i + 1; j--) {
                if (clear(level, pts.get(i), pts.get(j))) {
                    far = j;
                    break;
                }
            }
            out.add(pts.get(far));
            i = far;
        }
        return out;
    }
}
