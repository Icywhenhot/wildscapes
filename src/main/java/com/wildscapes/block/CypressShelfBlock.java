package com.wildscapes.block;

import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import com.wildscapes.block.entity.CypressShelfBlockEntity;
import net.minecraft.world.ItemInteractionResult;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Optional;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class CypressShelfBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<CypressShelfBlock> CODEC = simpleCodec(CypressShelfBlock::new);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<ShelfPart> SIDE_CHAIN_PART = EnumProperty.create("side_chain", ShelfPart.class);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final Map<Direction, VoxelShape> SHAPES = Map.of(
        Direction.NORTH, Shapes.or(Block.box(0, 12, 11, 16, 16, 13), Block.box(0, 0, 13, 16, 16, 16), Block.box(0, 0, 11, 16, 4, 13)),
        Direction.SOUTH, Shapes.or(Block.box(0, 12, 3, 16, 16, 5), Block.box(0, 0, 0, 16, 16, 3), Block.box(0, 0, 3, 16, 4, 5)),
        Direction.EAST, Shapes.or(Block.box(3, 12, 0, 5, 16, 16), Block.box(0, 0, 0, 3, 16, 16), Block.box(3, 0, 0, 5, 4, 16)),
        Direction.WEST, Shapes.or(Block.box(11, 12, 0, 13, 16, 16), Block.box(13, 0, 0, 16, 16, 16), Block.box(11, 0, 0, 13, 4, 16)));


    @Override
    public MapCodec<CypressShelfBlock> codec() {
        return CODEC;
    }

    public CypressShelfBlock(final BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(FACING, Direction.NORTH)
                .setValue(POWERED, false)
                .setValue(SIDE_CHAIN_PART, ShelfPart.UNCONNECTED)
                .setValue(WATERLOGGED, false)
        );
    }

    @Override
    protected VoxelShape getShape(final BlockState state, final BlockGetter level, final BlockPos pos, final CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    protected boolean useShapeForLightOcclusion(final BlockState state) {
        return true;
    }

    @Override
    protected boolean isPathfindable(final BlockState state, final PathComputationType type) {
        return type == PathComputationType.WATER && state.getFluidState().is(FluidTags.WATER);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(final BlockPos worldPosition, final BlockState blockState) {
        return new CypressShelfBlockEntity(worldPosition, blockState);
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, SIDE_CHAIN_PART, WATERLOGGED);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moved) {
        if (!state.is(next.getBlock())) {
            if (level.getBlockEntity(pos) instanceof CypressShelfBlockEntity shelf) Containers.dropContents(level, pos, shelf);
            level.updateNeighbourForOutputSignal(pos, this);
            this.updateNeighborsAfterPoweringDown(level, pos, state);
            super.onRemove(state, level, pos, next, moved);
        }
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void neighborChanged(
        final BlockState state, final Level level, final BlockPos pos, final Block block, final BlockPos neighbor, final boolean movedByPiston
    ) {
        if (!level.isClientSide) {
            boolean signal = level.hasNeighborSignal(pos);
            if (state.getValue(POWERED) != signal) {
                BlockState newState = state.setValue(POWERED, signal);
                if (!signal) {
                    newState = newState.setValue(SIDE_CHAIN_PART, ShelfPart.UNCONNECTED);
                }

                level.setBlock(pos, newState, 3);
                this.playSound(level, pos, signal ? com.wildscapes.sound.WildscapesSounds.SHELF_ACTIVATE : com.wildscapes.sound.WildscapesSounds.SHELF_DEACTIVATE);
                level.gameEvent(signal ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, pos, GameEvent.Context.of(newState));
            }
        }
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        FluidState replacedFluidState = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState()
            .setValue(FACING, context.getHorizontalDirection().getOpposite())
            .setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()))
            .setValue(WATERLOGGED, replacedFluidState.is(Fluids.WATER));
    }

    @Override
    public BlockState rotate(final BlockState state, final Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(final BlockState state, final Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    public int getRows() {
        return 1;
    }

    public int getColumns() {
        return 3;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        InteractionResult result = swapItems(stack, state, level, pos, player, hand, hit);
        return result.consumesAction() ? ItemInteractionResult.sidedSuccess(level.isClientSide) : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return swapItems(ItemStack.EMPTY, state, level, pos, player, InteractionHand.MAIN_HAND, hit);
    }

    private InteractionResult swapItems(
        final ItemStack itemStack,
        final BlockState state,
        final Level level,
        final BlockPos pos,
        final Player player,
        final InteractionHand hand,
        final BlockHitResult hitResult
    ) {
        if (level.getBlockEntity(pos) instanceof CypressShelfBlockEntity shelfBlockEntity && !hand.equals(InteractionHand.OFF_HAND)) {
            OptionalInt hitSlot = this.getHitSlot(hitResult, state.getValue(FACING));
            if (hitSlot.isEmpty()) {
                return InteractionResult.PASS;
            }

            Inventory inventory = player.getInventory();
            if (level.isClientSide) {
                return inventory.getSelected().isEmpty() ? InteractionResult.PASS : InteractionResult.SUCCESS;
            }

            if (!state.getValue(POWERED)) {
                boolean itemRemoved = swapSingleItem(itemStack, player, shelfBlockEntity, hitSlot.getAsInt(), inventory);
                if (itemRemoved) {
                    this.playSound(level, pos, itemStack.isEmpty() ? com.wildscapes.sound.WildscapesSounds.SHELF_TAKE_ITEM : com.wildscapes.sound.WildscapesSounds.SHELF_SINGLE_SWAP);
                } else {
                    if (itemStack.isEmpty()) {
                        return InteractionResult.PASS;
                    }

                    this.playSound(level, pos, com.wildscapes.sound.WildscapesSounds.SHELF_PLACE_ITEM);
                }

                return InteractionResult.SUCCESS;
            } else {
                ItemStack previousItem = inventory.getSelected();
                boolean anySwapped = this.swapHotbar(level, pos, inventory);
                if (!anySwapped) {
                    return InteractionResult.CONSUME;
                }

                this.playSound(level, pos, com.wildscapes.sound.WildscapesSounds.SHELF_MULTI_SWAP);
                return previousItem == inventory.getSelected()
                    ? InteractionResult.SUCCESS
                    : InteractionResult.SUCCESS;
            }
        } else {
            return InteractionResult.PASS;
        }
    }

    private static boolean swapSingleItem(
        final ItemStack itemStack, final Player player, final CypressShelfBlockEntity shelfBlockEntity, final int hitSlot, final Inventory inventory
    ) {
        ItemStack removedItem = shelfBlockEntity.swapItemNoUpdate(hitSlot, itemStack);
        ItemStack newInventoryItem = player.hasInfiniteMaterials() && removedItem.isEmpty() ? itemStack.copy() : removedItem;
        inventory.setItem(inventory.selected, newInventoryItem);
        inventory.setChanged();
        shelfBlockEntity.setChanged(GameEvent.ITEM_INTERACT_FINISH);

        return !removedItem.isEmpty();
    }

    private boolean swapHotbar(final Level level, final BlockPos pos, final Inventory inventory) {
        List<BlockPos> connectedBlocks = this.getAllBlocksConnectedTo(level, pos);
        if (connectedBlocks.isEmpty()) {
            return false;
        }

        boolean anySwapped = false;

        for (int shelfPartIndex = 0; shelfPartIndex < connectedBlocks.size(); shelfPartIndex++) {
            CypressShelfBlockEntity shelfPart = (CypressShelfBlockEntity)level.getBlockEntity(connectedBlocks.get(shelfPartIndex));
            if (shelfPart != null) {
                for (int slot = 0; slot < shelfPart.getContainerSize(); slot++) {
                    int inventorySlot = 9 - (connectedBlocks.size() - shelfPartIndex) * shelfPart.getContainerSize() + slot;
                    if (inventorySlot >= 0 && inventorySlot <= inventory.getContainerSize()) {
                        ItemStack placedInventoryItem = inventory.removeItemNoUpdate(inventorySlot);
                        ItemStack removedShelfItem = shelfPart.swapItemNoUpdate(slot, placedInventoryItem);
                        if (!placedInventoryItem.isEmpty() || !removedShelfItem.isEmpty()) {
                            inventory.setItem(inventorySlot, removedShelfItem);
                            anySwapped = true;
                        }
                    }
                }

                inventory.setChanged();
                shelfPart.setChanged(GameEvent.ENTITY_INTERACT);
            }
        }

        return anySwapped;
    }

    public ShelfPart getShelfPart(final BlockState state) {
        return state.getValue(SIDE_CHAIN_PART);
    }

    public BlockState setShelfPart(final BlockState state, final ShelfPart newPart) {
        return state.setValue(SIDE_CHAIN_PART, newPart);
    }

    public Direction getFacing(final BlockState state) {
        return state.getValue(FACING);
    }

    public boolean isConnectable(final BlockState state) {
        return state.is(WildscapesBlocks.WOODEN_SHELVES) && state.hasProperty(POWERED) && state.getValue(POWERED);
    }

    public int getMaxChainLength() {
        return 3;
    }

    @Override
    protected void onPlace(final BlockState state, final Level level, final BlockPos pos, final BlockState oldState, final boolean movedByPiston) {
        if (state.getValue(POWERED)) {
            this.updateSelfAndNeighborsOnPoweringUp(level, pos, state, oldState);
        } else {
            this.updateNeighborsAfterPoweringDown(level, pos, state);
        }
    }

    private void playSound(final LevelAccessor level, final BlockPos pos, final SoundEvent sound) {
        level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    @Override
    protected FluidState getFluidState(final BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    @Override
    protected boolean hasAnalogOutputSignal(final BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(final BlockState state, final Level level, final BlockPos pos) {
        if (level.isClientSide) {
            return 0;
        } else if (level.getBlockEntity(pos) instanceof CypressShelfBlockEntity blockEntity) {
            int item1Bit = blockEntity.getItem(0).isEmpty() ? 0 : 1;
            int item2Bit = blockEntity.getItem(1).isEmpty() ? 0 : 1;
            int item3Bit = blockEntity.getItem(2).isEmpty() ? 0 : 1;
            return item1Bit | item2Bit << 1 | item3Bit << 2;
        } else {
            return 0;
        }
    }
    public List<BlockPos> getAllBlocksConnectedTo(final LevelAccessor level, final BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!this.isConnectable(state)) {
            return List.of();
        }

        CypressShelfBlock.Neighbors neighbors = this.getNeighbors(level, pos, this.getFacing(state));
        List<BlockPos> results = new LinkedList<>();
        results.add(pos);
        this.addBlocksConnectingTowards(neighbors::left, ShelfPart.LEFT, results::addFirst);
        this.addBlocksConnectingTowards(neighbors::right, ShelfPart.RIGHT, results::addLast);
        return results;
    }

    private void addBlocksConnectingTowards(
        final IntFunction<CypressShelfBlock.Neighbor> getNeighbor, final ShelfPart endPart, final Consumer<BlockPos> accumulator
    ) {
        for (int steps = 1; steps < this.getMaxChainLength(); steps++) {
            CypressShelfBlock.Neighbor neighbor = getNeighbor.apply(steps);
            if (neighbor.connectsTowards(endPart)) {
                accumulator.accept(neighbor.pos());
            }

            if (neighbor.isUnconnectableOrChainEnd()) {
                break;
            }
        }
    }

    public void updateNeighborsAfterPoweringDown(final LevelAccessor level, final BlockPos pos, final BlockState state) {
        CypressShelfBlock.Neighbors neighbors = this.getNeighbors(level, pos, this.getFacing(state));
        neighbors.left().disconnectFromRight();
        neighbors.right().disconnectFromLeft();
    }

    public void updateSelfAndNeighborsOnPoweringUp(final LevelAccessor level, final BlockPos pos, final BlockState state, final BlockState oldState) {
        if (this.isConnectable(state)) {
            if (!this.isBeingUpdatedByNeighbor(state, oldState)) {
                CypressShelfBlock.Neighbors neighbors = this.getNeighbors(level, pos, this.getFacing(state));
                ShelfPart newPartForSelf = ShelfPart.UNCONNECTED;
                int existingChainOnTheLeft = neighbors.left().isConnectable() ? this.getAllBlocksConnectedTo(level, neighbors.left().pos()).size() : 0;
                int existingChainOnTheRight = neighbors.right().isConnectable() ? this.getAllBlocksConnectedTo(level, neighbors.right().pos()).size() : 0;
                int currentChainLength = 1;
                if (this.canConnect(existingChainOnTheLeft, currentChainLength)) {
                    newPartForSelf = newPartForSelf.whenConnectedToTheLeft();
                    neighbors.left().connectToTheRight();
                    currentChainLength += existingChainOnTheLeft;
                }

                if (this.canConnect(existingChainOnTheRight, currentChainLength)) {
                    newPartForSelf = newPartForSelf.whenConnectedToTheRight();
                    neighbors.right().connectToTheLeft();
                }

                this.setPart(level, pos, newPartForSelf);
            }
        }
    }

    private boolean canConnect(final int newBlocksToConnectTo, final int currentChainLength) {
        return newBlocksToConnectTo > 0 && currentChainLength + newBlocksToConnectTo <= this.getMaxChainLength();
    }

    private boolean isBeingUpdatedByNeighbor(final BlockState state, final BlockState oldState) {
        boolean isGettingConnected = this.getShelfPart(state).isConnected();
        boolean hasBeenConnectedBefore = this.isConnectable(oldState) && this.getShelfPart(oldState).isConnected();
        return isGettingConnected || hasBeenConnectedBefore;
    }

    private CypressShelfBlock.Neighbors getNeighbors(final LevelAccessor level, final BlockPos center, final Direction facing) {
        return new CypressShelfBlock.Neighbors(this, level, facing, center, new HashMap<>());
    }

    private void setPart(final LevelAccessor level, final BlockPos pos, final ShelfPart newPart) {
        BlockState state = level.getBlockState(pos);
        if (this.getShelfPart(state) != newPart) {
            level.setBlock(pos, this.setShelfPart(state, newPart), 3);
        }
    }

    record EmptyNeighbor(BlockPos pos) implements CypressShelfBlock.Neighbor {
        @Override
        public boolean isConnectable() {
            return false;
        }

        @Override
        public boolean isUnconnectableOrChainEnd() {
            return true;
        }

        @Override
        public boolean connectsTowards(final ShelfPart endPart) {
            return false;
        }
    }

    sealed interface Neighbor permits CypressShelfBlock.EmptyNeighbor, CypressShelfBlock.SideChainNeighbor {
        BlockPos pos();

        boolean isConnectable();

        boolean isUnconnectableOrChainEnd();

        boolean connectsTowards(final ShelfPart endPart);

        default void connectToTheRight() {
        }

        default void connectToTheLeft() {
        }

        default void disconnectFromRight() {
        }

        default void disconnectFromLeft() {
        }
    }

    record Neighbors(CypressShelfBlock block, LevelAccessor level, Direction facing, BlockPos center, Map<BlockPos, CypressShelfBlock.Neighbor> cache) {
        private boolean isConnectableToThisBlock(final BlockState neighbor) {
            return this.block.isConnectable(neighbor) && this.block.getFacing(neighbor) == this.facing;
        }

        private CypressShelfBlock.Neighbor createNewNeighbor(final BlockPos pos) {
            BlockState neighbor = this.level.getBlockState(pos);
            ShelfPart part = this.isConnectableToThisBlock(neighbor) ? this.block.getShelfPart(neighbor) : null;
            return part == null ? new CypressShelfBlock.EmptyNeighbor(pos) : new CypressShelfBlock.SideChainNeighbor(this.level, this.block, pos, part);
        }

        private CypressShelfBlock.Neighbor getOrCreateNeighbor(final Direction dir, final Integer steps) {
            return this.cache.computeIfAbsent(this.center.relative(dir, steps), this::createNewNeighbor);
        }

        public CypressShelfBlock.Neighbor left(final int steps) {
            return this.getOrCreateNeighbor(this.facing.getClockWise(), steps);
        }

        public CypressShelfBlock.Neighbor right(final int steps) {
            return this.getOrCreateNeighbor(this.facing.getCounterClockWise(), steps);
        }

        public CypressShelfBlock.Neighbor left() {
            return this.left(1);
        }

        public CypressShelfBlock.Neighbor right() {
            return this.right(1);
        }
    }

    record SideChainNeighbor(LevelAccessor level, CypressShelfBlock block, BlockPos pos, ShelfPart part) implements CypressShelfBlock.Neighbor {
        @Override
        public boolean isConnectable() {
            return true;
        }

        @Override
        public boolean isUnconnectableOrChainEnd() {
            return this.part.isChainEnd();
        }

        @Override
        public boolean connectsTowards(final ShelfPart endPart) {
            return this.part.isConnectionTowards(endPart);
        }

        @Override
        public void connectToTheRight() {
            this.block.setPart(this.level, this.pos, this.part.whenConnectedToTheRight());
        }

        @Override
        public void connectToTheLeft() {
            this.block.setPart(this.level, this.pos, this.part.whenConnectedToTheLeft());
        }

        @Override
        public void disconnectFromRight() {
            this.block.setPart(this.level, this.pos, this.part.whenDisconnectedFromTheRight());
        }

        @Override
        public void disconnectFromLeft() {
            this.block.setPart(this.level, this.pos, this.part.whenDisconnectedFromTheLeft());
        }
    }

    private OptionalInt getHitSlot(final BlockHitResult hitResult, final Direction blockFacing) {
        return getRelativeHitCoordinatesForBlockFace(hitResult, blockFacing).map(hitCoords -> {
            int row = getSection(1.0F - hitCoords.y, this.getRows());
            int column = getSection(hitCoords.x, this.getColumns());
            return OptionalInt.of(column + row * this.getColumns());
        }).orElseGet(OptionalInt::empty);
    }

    private static Optional<Vec2> getRelativeHitCoordinatesForBlockFace(final BlockHitResult hitResult, final Direction blockFacing) {
        Direction hitDirection = hitResult.getDirection();
        if (blockFacing != hitDirection) {
            return Optional.empty();
        }

        BlockPos hitBlockPos = hitResult.getBlockPos().relative(hitDirection);
        Vec3 relativeHit = hitResult.getLocation().subtract(hitBlockPos.getX(), hitBlockPos.getY(), hitBlockPos.getZ());
        double relativeX = relativeHit.x();
        double relativeY = relativeHit.y();
        double relativeZ = relativeHit.z();

        return switch (hitDirection) {
            case NORTH -> Optional.of(new Vec2((float)(1.0 - relativeX), (float)relativeY));
            case SOUTH -> Optional.of(new Vec2((float)relativeX, (float)relativeY));
            case WEST -> Optional.of(new Vec2((float)relativeZ, (float)relativeY));
            case EAST -> Optional.of(new Vec2((float)(1.0 - relativeZ), (float)relativeY));
            case DOWN, UP -> Optional.empty();
        };
    }

    private static int getSection(final float relativeCoordinate, final int maxSections) {
        float targetedPixel = relativeCoordinate * 16.0F;
        float sectionSize = 16.0F / maxSections;
        return Mth.clamp(Mth.floor(targetedPixel / sectionSize), 0, maxSections - 1);
    }
}
