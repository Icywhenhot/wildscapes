package com.wildscapes.client;

import java.util.Map;
import java.util.function.Predicate;

import com.wildscapes.Wildscapes;
import com.wildscapes.block.WildscapesBlocks;
import com.wildscapes.entity.SwampVariants;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class SwampMossModel extends WrapperBlockStateModel {
    private static final Map<Identifier, Identifier> TEXTURES = Map.of(
            Identifier.withDefaultNamespace("block/moss_block"), Wildscapes.id("block/swamp_moss_block"),
            Wildscapes.id("block/moss_drape"), Wildscapes.id("block/swamp_moss_drape"),
            Wildscapes.id("block/moss_drape_end"), Wildscapes.id("block/swamp_moss_drape_end"));

    private SwampMossModel(BlockStateModel model) {
        super(model);
    }

    public static void register() {
        ModelLoadingPlugin.register(plugin -> plugin.modifyBlockModelAfterBake().register((model, context) -> {
            var block = context.state().getBlock();
            return block == Blocks.MOSS_BLOCK || block == Blocks.MOSS_CARPET
                    || block == WildscapesBlocks.MOSS_CARPET || block == WildscapesBlocks.HANGING_MOSS
                    ? new SwampMossModel(model) : model;
        }));
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter view, BlockPos pos, BlockState state,
            RandomSource random, Predicate<Direction> cull) {
        if (!isSwamp(pos)) {
            super.emitQuads(emitter, view, pos, state, random, cull);
            return;
        }

        var atlases = Minecraft.getInstance().getAtlasManager();
        var finder = atlases.getAtlasOrThrow(AtlasIds.BLOCKS).spriteFinder();
        emitter.pushTransform(quad -> {
            TextureAtlasSprite old = finder.find(quad);
            Identifier id = TEXTURES.get(old.contents().name());
            if (id != null) {
                TextureAtlasSprite sprite = atlases.get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, id));
                for (int i = 0; i < 4; i++) {
                    float u = (quad.u(i) - old.getU0()) / (old.getU1() - old.getU0());
                    float v = (quad.v(i) - old.getV0()) / (old.getV1() - old.getV0());
                    quad.uv(i, sprite.getU(u), sprite.getV(v));
                }
            }
            return true;
        });
        try {
            super.emitQuads(emitter, view, pos, state, random, cull);
        } finally {
            emitter.popTransform();
        }
    }

    @Override
    public Object createGeometryKey(BlockAndTintGetter view, BlockPos pos, BlockState state, RandomSource random) {
        Object key = super.createGeometryKey(view, pos, state, random);
        return key != null && isSwamp(pos) ? new SwampKey(key) : key;
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter view, BlockPos pos, BlockState state) {
        Material.Baked base = super.particleMaterial(view, pos, state);
        Identifier id = isSwamp(pos) ? TEXTURES.get(base.sprite().contents().name()) : null;
        return id == null ? base : new Material.Baked(Minecraft.getInstance().getAtlasManager()
                .get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, id)), base.forceTranslucent());
    }

    private static boolean isSwamp(BlockPos pos) {
        var level = Minecraft.getInstance().level;
        return level != null && level.getBiome(pos).is(SwampVariants.SWAMP_BIOMES);
    }

    private record SwampKey(Object base) {}
}
