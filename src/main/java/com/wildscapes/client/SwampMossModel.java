package com.wildscapes.client;

import com.wildscapes.Wildscapes;
import com.wildscapes.block.WildscapesBlocks;
import com.wildscapes.entity.SwampVariants;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

@EventBusSubscriber(modid = Wildscapes.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class SwampMossModel extends BakedModelWrapper<BakedModel> {
    private static final ModelProperty<Boolean> SWAMP = new ModelProperty<>();
    private final Map<ResourceLocation, TextureAtlasSprite> textures;
    private final Map<BakedQuad, BakedQuad> quads = new ConcurrentHashMap<>();

    private SwampMossModel(BakedModel model, Map<ResourceLocation, TextureAtlasSprite> textures) {
        super(model);
        this.textures = textures;
    }

    @SubscribeEvent
    static void onModels(ModelEvent.ModifyBakingResult event) {
        var textures = Map.of(
                ResourceLocation.withDefaultNamespace("block/moss_block"), sprite(event, "swamp_moss_block"),
                ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "block/moss_drape"), sprite(event, "swamp_moss_drape"),
                ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "block/moss_drape_end"), sprite(event, "swamp_moss_drape_end"));
        for (var block : List.of(Blocks.MOSS_BLOCK, Blocks.MOSS_CARPET,
                WildscapesBlocks.MOSS_CARPET.get(), WildscapesBlocks.HANGING_MOSS.get())) {
            for (var state : block.getStateDefinition().getPossibleStates()) {
                var id = BlockModelShaper.stateToModelLocation(state);
                event.getModels().computeIfPresent(id, (key, model) -> new SwampMossModel(model, textures));
            }
        }
    }

    private static TextureAtlasSprite sprite(ModelEvent.ModifyBakingResult event, String name) {
        return event.getTextureGetter().apply(new Material(TextureAtlas.LOCATION_BLOCKS,
                ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "block/" + name)));
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter view, BlockPos pos, BlockState state, ModelData data) {
        var level = Minecraft.getInstance().level;
        return super.getModelData(view, pos, state, data).derive()
                .with(SWAMP, level != null && level.getBiome(pos).is(SwampVariants.SWAMP_BIOMES)).build();
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random, ModelData data, RenderType type) {
        var faces = super.getQuads(state, side, random, data, type);
        if (!Boolean.TRUE.equals(data.get(SWAMP))) {
            return faces;
        }
        return faces.stream().map(face -> quads.computeIfAbsent(face, this::retexture)).toList();
    }

    @Override
    public TextureAtlasSprite getParticleIcon(ModelData data) {
        var sprite = super.getParticleIcon(data);
        return Boolean.TRUE.equals(data.get(SWAMP)) ? textures.getOrDefault(sprite.contents().name(), sprite) : sprite;
    }

    private BakedQuad retexture(BakedQuad quad) {
        var old = quad.getSprite();
        var sprite = textures.get(old.contents().name());
        if (sprite == null) {
            return quad;
        }
        var vertices = quad.getVertices().clone();
        for (int i = 0; i < 4; i++) {
            int uv = i * IQuadTransformer.STRIDE + IQuadTransformer.UV0;
            float u = old.getUOffset(Float.intBitsToFloat(vertices[uv]));
            float v = old.getVOffset(Float.intBitsToFloat(vertices[uv + 1]));
            vertices[uv] = Float.floatToRawIntBits(sprite.getU(u));
            vertices[uv + 1] = Float.floatToRawIntBits(sprite.getV(v));
        }
        return new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), sprite,
                quad.isShade(), quad.hasAmbientOcclusion());
    }
}
