package com.wildscapes.entity;

import java.lang.reflect.Method;
import java.lang.reflect.Field;

import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.entity.monster.SpellcasterIllager;

final class IllusionerSpellBridge {
    private static final Method SET_SPELL;
    private static final Field SPELL_TICKS;
    private static final Object NONE;
    private static final Object DISAPPEAR;

    static {
        try {
            Class<?> spellClass = Class.forName("net.minecraft.world.entity.monster.SpellcasterIllager$IllagerSpell");
            SET_SPELL = SpellcasterIllager.class.getDeclaredMethod("setIsCastingSpell", spellClass);
            SET_SPELL.setAccessible(true);
            SPELL_TICKS = SpellcasterIllager.class.getDeclaredField("spellCastingTickCount");
            SPELL_TICKS.setAccessible(true);
            NONE = spellConstant(spellClass, "NONE");
            DISAPPEAR = spellConstant(spellClass, "DISAPPEAR");
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("illusioner spellcasting api changed", e);
        }
    }

    private IllusionerSpellBridge() {}

    private static Object spellConstant(Class<?> spellClass, String name) {
        for (Object constant : spellClass.getEnumConstants()) {
            if (((Enum<?>) constant).name().equals(name)) {
                return constant;
            }
        }
        throw new IllegalStateException("missing spell constant " + name);
    }

    static void startCasting(Illusioner illusioner, int ticks) {
        invoke(illusioner, DISAPPEAR, ticks);
    }

    static void stopCasting(Illusioner illusioner) {
        invoke(illusioner, NONE, 0);
    }

    private static void invoke(Illusioner illusioner, Object spell, int ticks) {
        try {
            SPELL_TICKS.setInt(illusioner, ticks);
            SET_SPELL.invoke(illusioner, spell);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("failed to drive illusioner spellcasting", e);
        }
    }
}
