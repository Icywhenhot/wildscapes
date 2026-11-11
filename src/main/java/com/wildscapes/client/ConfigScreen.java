package com.wildscapes.client;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.wildscapes.WildscapesConfig;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.common.ForgeConfigSpec.ValueSpec;

public final class ConfigScreen {
    private ConfigScreen() {}

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("config.wildscapes.title"));
        ConfigEntryBuilder entries = builder.entryBuilder();

        add(builder, entries, WildscapesConfig.CLIENT_SPEC);

        boolean editable = WildscapesConfig.SERVER_SPEC.isLoaded() && Minecraft.getInstance().hasSingleplayerServer();
        if (editable) {
            add(builder, entries, WildscapesConfig.SERVER_SPEC);
        } else {
            builder.getOrCreateCategory(Component.translatable("config.wildscapes.server"))
                    .addEntry(entries.startTextDescription(Component.translatable(
                            Minecraft.getInstance().level == null
                                    ? "config.wildscapes.server.no_world"
                                    : "config.wildscapes.server.remote")).build());
        }

        builder.setSavingRunnable(() -> {
            WildscapesConfig.CLIENT_SPEC.save();
            if (editable) {
                WildscapesConfig.SERVER_SPEC.save();
            }
        });
        return builder.build();
    }

    private static <E extends Enum<E>> AbstractConfigListEntry<E> enumEntry(
            ConfigEntryBuilder entries, Component label, Component[] tip, EnumValue<E> value) {
        return entries.startEnumSelector(label, value.getDefault().getDeclaringClass(), value.get())
                .setDefaultValue(value.getDefault())
                .setEnumNameProvider(e -> Component.translatable("config.wildscapes.enum." + e.name().toLowerCase()))
                .setTooltip(tip)
                .setSaveConsumer(value::set)
                .build();
    }

    private static void add(ConfigBuilder builder, ConfigEntryBuilder entries, ForgeConfigSpec spec) {
        for (UnmodifiableConfig.Entry group : spec.getValues().entrySet()) {
            String name = group.getKey();
            ConfigCategory cat = builder.getOrCreateCategory(Component.translatable("config.wildscapes." + name));
            UnmodifiableConfig values = group.getValue();
            for (UnmodifiableConfig.Entry e : values.entrySet()) {
                ConfigValue<?> value = e.getValue();
                ValueSpec vs = spec.getSpec().get(value.getPath());
                Component label = Component.translatable("config.wildscapes." + name + "." + e.getKey());
                Component[] tip = vs.getComment() == null ? new Component[0]
                        : new Component[] {Component.literal(vs.getComment())};

                if (value instanceof BooleanValue b) {
                    cat.addEntry(entries.startBooleanToggle(label, b.get())
                            .setDefaultValue(b.getDefault())
                            .setTooltip(tip)
                            .setSaveConsumer(b::set)
                            .build());
                } else if (value instanceof IntValue i) {
                    ForgeConfigSpec.Range<Integer> range = vs.getRange();
                    cat.addEntry(entries.startIntField(label, i.get())
                            .setDefaultValue(i.getDefault())
                            .setMin(range.getMin())
                            .setMax(range.getMax())
                            .setTooltip(tip)
                            .setSaveConsumer(i::set)
                            .build());
                } else if (value instanceof DoubleValue d) {
                    ForgeConfigSpec.Range<Double> range = vs.getRange();
                    cat.addEntry(entries.startDoubleField(label, d.get())
                            .setDefaultValue(d.getDefault())
                            .setMin(range.getMin())
                            .setMax(range.getMax())
                            .setTooltip(tip)
                            .setSaveConsumer(d::set)
                            .build());
                } else if (value instanceof EnumValue<?> en) {
                    cat.addEntry(enumEntry(entries, label, tip, en));
                }
            }
        }
    }
}
