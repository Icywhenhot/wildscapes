package com.wildscapes.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.wildscapes.Wildscapes;
import com.wildscapes.block.WildscapesBlocks;
import com.wildscapes.entity.SwampVariants;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;
import net.minecraft.util.TriState;
import org.jspecify.annotations.Nullable;

public final class SwampMossModel extends DelegateBlockStateModel {
    private static final Map<Identifier, Identifier> TEXTURES = Map.of(
            Identifier.withDefaultNamespace("block/moss_block"), Wildscapes.id("block/swamp_moss_block"),
            Wildscapes.id("block/moss_drape"), Wildscapes.id("block/swamp_moss_drape"),
            Wildscapes.id("block/moss_drape_end"), Wildscapes.id("block/swamp_moss_drape_end"));

    private final Map<BlockStateModelPart, BlockStateModelPart> parts = new ConcurrentHashMap<>();

    private SwampMossModel(BlockStateModel model) {
        super(model);
    }

    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        event.getBakingResult().blockStateModels().replaceAll((state, model) -> {
            var block = state.getBlock();
            return block == Blocks.MOSS_BLOCK || block == Blocks.MOSS_CARPET
                    || block == WildscapesBlocks.MOSS_CARPET || block == WildscapesBlocks.HANGING_MOSS
                    ? new SwampMossModel(model) : model;
        });
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
            List<BlockStateModelPart> out) {
        if (!isSwamp(pos)) {
            super.collectParts(level, pos, state, random, out);
            return;
        }
        List<BlockStateModelPart> base = new ArrayList<>();
        super.collectParts(level, pos, state, random, base);
        for (BlockStateModelPart part : base) {
            out.add(parts.computeIfAbsent(part, SwampPart::new));
        }
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        Material.Baked base = super.particleMaterial(level, pos, state);
        return isSwamp(pos) ? swap(base) : base;
    }

    private static Material.Baked swap(Material.Baked base) {
        TextureAtlasSprite sprite = swampSprite(base.sprite());
        return sprite == null ? base : new Material.Baked(sprite, base.forceTranslucent());
    }

    private static @Nullable TextureAtlasSprite swampSprite(TextureAtlasSprite old) {
        Identifier id = TEXTURES.get(old.contents().name());
        return id == null ? null : Minecraft.getInstance().getAtlasManager().get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, id));
    }

    private static boolean isSwamp(BlockPos pos) {
        var level = Minecraft.getInstance().level;
        return level != null && level.getBiome(pos).is(SwampVariants.SWAMP_BIOMES);
    }

    private static final class SwampPart implements BlockStateModelPart {
        private final BlockStateModelPart base;
        private final Map<BakedQuad, BakedQuad> quads = new ConcurrentHashMap<>();

        private SwampPart(BlockStateModelPart base) {
            this.base = base;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable Direction direction) {
            return base.getQuads(direction).stream().map(quad -> quads.computeIfAbsent(quad, SwampPart::retexture)).toList();
        }

        @Override
        @SuppressWarnings("deprecation")
        public boolean useAmbientOcclusion() {
            return base.useAmbientOcclusion();
        }

        @Override
        public TriState ambientOcclusion() {
            return base.ambientOcclusion();
        }

        @Override
        public Material.Baked particleMaterial() {
            return swap(base.particleMaterial());
        }

        @Override
        public int materialFlags() {
            return base.materialFlags();
        }

        private static BakedQuad retexture(BakedQuad quad) {
            var info = quad.materialInfo();
            TextureAtlasSprite old = info.sprite();
            TextureAtlasSprite sprite = swampSprite(old);
            if (sprite == null) {
                return quad;
            }
            long[] uvs = new long[4];
            for (int i = 0; i < 4; i++) {
                long packed = quad.packedUV(i);
                float u = (UVPair.unpackU(packed) - old.getU0()) / (old.getU1() - old.getU0());
                float v = (UVPair.unpackV(packed) - old.getV0()) / (old.getV1() - old.getV0());
                uvs[i] = UVPair.pack(sprite.getU(u), sprite.getV(v));
            }
            var newInfo = new BakedQuad.MaterialInfo(sprite, info.layer(), info.itemRenderType(), info.itemGlintRenderType(),
                    info.itemGlintSpecialRenderType(), info.tintIndex(), info.shadeDirectionOverride(), info.lightEmission(),
                    info.ambientOcclusion());
            return new BakedQuad(quad.position0(), quad.position1(), quad.position2(), quad.position3(),
                    uvs[0], uvs[1], uvs[2], uvs[3], quad.direction(), newInfo, quad.bakedNormals(), quad.bakedColors());
        }
    }
}
