package com.wildscapes.entity;

import net.minecraft.world.entity.monster.illager.Illusioner;
import net.minecraft.world.entity.monster.illager.SpellcasterIllager.IllagerSpell;

final class IllusionerSpellBridge {
    private IllusionerSpellBridge() {}

    static void startCasting(Illusioner illusioner, int ticks) {
        illusioner.spellCastingTickCount = ticks;
        illusioner.setIsCastingSpell(IllagerSpell.DISAPPEAR);
    }

    static void stopCasting(Illusioner illusioner) {
        illusioner.spellCastingTickCount = 0;
        illusioner.setIsCastingSpell(IllagerSpell.NONE);
    }
}
