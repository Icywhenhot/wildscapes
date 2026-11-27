package com.wildscapes.client;

import java.util.Map;
import java.util.function.Supplier;
import com.wildscapes.Wildscapes;
import com.wildscapes.entity.SwampVariants;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public final class SwampMossModel extends ForwardingBakedModel {
    private static final Map<ResourceLocation, ResourceLocation> TEXTURES = Map.of(
            new ResourceLocation("block/moss_block"), Wildscapes.id("block/swamp_moss_block"),
            Wildscapes.id("block/moss_drape"), Wildscapes.id("block/swamp_moss_drape"),
            Wildscapes.id("block/moss_drape_end"), Wildscapes.id("block/swamp_moss_drape_end"));

    private SwampMossModel(BakedModel model) {
        wrapped = model;
    }

    public static void register() {
        ModelLoadingPlugin.register(plugin -> plugin.modifyModelAfterBake().register((model, context) -> {
            if (!(context.id() instanceof ModelResourceLocation location) || location.getVariant().equals("inventory")) {
                return model;
            }
            var id = new ResourceLocation(location.getNamespace(), location.getPath());
            return id.equals(new ResourceLocation("moss_block"))
                    || id.equals(new ResourceLocation("moss_carpet"))
                    || id.equals(Wildscapes.id("moss_carpet")) || id.equals(Wildscapes.id("hanging_moss"))
                    ? new SwampMossModel(model) : model;
        }));
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter view, BlockState state, BlockPos pos,
            Supplier<RandomSource> random, RenderContext context) {
        if (Minecraft.getInstance().level == null || !Minecraft.getInstance().level.getBiome(pos).is(SwampVariants.SWAMP_BIOMES)) {
            super.emitBlockQuads(view, state, pos, random, context);
            return;
        }
        var atlas = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
        var finder = SpriteFinder.get(atlas);
        context.pushTransform(quad -> {
            var old = finder.find(quad);
            var id = TEXTURES.get(old.contents().name());
            if (id != null) {
                var sprite = atlas.getSprite(id);
                for (int i = 0; i < 4; i++) {
                    float u = (quad.u(i) - old.getU0()) / (old.getU1() - old.getU0());
                    float v = (quad.v(i) - old.getV0()) / (old.getV1() - old.getV0());
                    quad.uv(i, sprite.getU(u * 16.0F), sprite.getV(v * 16.0F));
                }
            }
            return true;
        });
        try {
            super.emitBlockQuads(view, state, pos, random, context);
        } finally {
            context.popTransform();
        }
    }
}
