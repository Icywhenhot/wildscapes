package com.wildscapes.entity;

import java.util.List;

import net.minecraft.world.entity.monster.Illusioner;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

public final class IllusionerGoals {
    private IllusionerGoals() {}

    public static void onJoinLevel(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide()
                && event.getEntity() instanceof Illusioner illusioner
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
