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
            var goals = List.copyOf(illusioner.getGoalSelector().getAvailableGoals());
            goals.forEach(goal -> illusioner.getGoalSelector().removeGoal(goal.getGoal()));
            illusioner.getGoalSelector().addGoal(1, new IllusionerMirageGoal(illusioner));
            goals.forEach(goal -> illusioner.getGoalSelector().addGoal(goal.getPriority(), goal.getGoal()));
        }
    }

    private static boolean hasMirageGoal(Illusioner illusioner) {
        return illusioner.getGoalSelector().getAvailableGoals().stream()
                .anyMatch(wrapped -> wrapped.getGoal() instanceof IllusionerMirageGoal);
    }
}
