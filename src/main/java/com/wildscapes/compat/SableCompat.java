package com.wildscapes.compat;

import org.jetbrains.annotations.Nullable;

import dev.ryanhcode.sable.companion.SableCompanion;
import net.minecraft.core.Position;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;

public final class SableCompat {
    private static final boolean LOADED = ModList.get().isLoaded("sable");

    private SableCompat() {}

    public static boolean inPlot(Level level, Vec3 pos) {
        return LOADED && Sable.inPlot(level, pos);
    }

    public static Vec3 toWorld(Level level, Vec3 pos) {
        return LOADED ? Sable.toWorld(level, pos) : pos;
    }

    @Nullable
    public static Vec3 follow(Level level, Vec3 local) {
        return LOADED ? Sable.follow(level, local) : local;
    }

    private static final class Sable {
        static boolean inPlot(Level level, Vec3 pos) {
            return SableCompanion.INSTANCE.isInPlotGrid(level, (Position) pos);
        }

        static Vec3 toWorld(Level level, Vec3 pos) {
            return SableCompanion.INSTANCE.projectOutOfSubLevel(level, pos);
        }

        @Nullable
        static Vec3 follow(Level level, Vec3 local) {
            if (SableCompanion.INSTANCE.getContaining(level, (Position) local) == null) {
                return inPlot(level, local) ? null : local;
            }
            return toWorld(level, local);
        }
    }
}
