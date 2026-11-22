package com.wildscapes.entity;

import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.entity.monster.SpellcasterIllager;

final class IllusionerSpellBridge {
    private IllusionerSpellBridge() {}

    static void startCasting(Illusioner illusioner, int ticks) {
        cast(illusioner, SpellcasterIllager.IllagerSpell.DISAPPEAR, ticks);
    }

    static void stopCasting(Illusioner illusioner) {
        cast(illusioner, SpellcasterIllager.IllagerSpell.NONE, 0);
    }

    private static void cast(Illusioner illusioner, SpellcasterIllager.IllagerSpell spell, int ticks) {
        illusioner.spellCastingTickCount = ticks;
        illusioner.setIsCastingSpell(spell);
    }
}
