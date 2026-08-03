package com.wildscapes.item;

import java.util.List;

import com.mojang.serialization.Codec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffectInstance;

/**
 * The effects carried by a bowl of magic soup (see {@link MagicSoupItem}) and, while it is
 * being brewed, by the {@link com.wildscapes.block.entity.CauldronBlockEntity}. A soup holds
 * up to three effects, each with a short duration compared to a real potion.
 *
 * <p>Stored as a data component so two bowls only stack when their effect lists match, exactly
 * like potions stacking by their {@code PotionContents}.
 */
public record SoupContents(List<MobEffectInstance> effects) {

    public static final SoupContents EMPTY = new SoupContents(List.of());

    public static final Codec<SoupContents> CODEC = MobEffectInstance.CODEC
            .listOf()
            .xmap(SoupContents::new, SoupContents::effects);

    public static final StreamCodec<RegistryFriendlyByteBuf, SoupContents> STREAM_CODEC =
            MobEffectInstance.STREAM_CODEC
                    .apply(ByteBufCodecs.list())
                    .map(SoupContents::new, SoupContents::effects);

    public boolean isEmpty() {
        return effects.isEmpty();
    }
}
