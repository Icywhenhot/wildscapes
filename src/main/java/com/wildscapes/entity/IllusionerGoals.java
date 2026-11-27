package com.wildscapes.entity;

import java.util.List;

import net.minecraft.world.entity.monster.illager.Illusioner;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public final class IllusionerGoals {
    private IllusionerGoals() {}

    public static void onJoinLevel(Entity entity, ServerLevel level) {
        if (entity instanceof Illusioner illusioner
                && !hasMirageGoal(illusioner)) {
            var goals = List.copyOf(illusioner.goalSelector.getAvailableGoals());
            goals.forEach(goal -> illusioner.goalSelector.removeGoal(goal.getGoal()));
            illusioner.goalSelector.addGoal(1, new IllusionerMirageGoal(illusioner));
            goals.forEach(goal -> illusioner.goalSelector.addGoal(goal.getPriority(), goal.getGoal()));
        }
    }

    private static boolean hasMirageGoal(Illusioner illusioner) {
        return illusioner.goalSelector.getAvailableGoals().stream()
                .anyMatch(wrapped -> wrapped.getGoal() instanceof IllusionerMirageGoal);
    }
}
