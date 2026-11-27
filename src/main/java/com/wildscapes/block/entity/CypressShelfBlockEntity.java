package com.wildscapes.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class CypressShelfBlockEntity extends BlockEntity implements Container {
    private final NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
    private boolean alignItemsToBottom;

    public CypressShelfBlockEntity(BlockPos pos, BlockState state) {
        super(WildscapesBlockEntities.CYPRESS_SHELF.get(), pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.clear();
        ContainerHelper.loadAllItems(tag, items);
        alignItemsToBottom = tag.getBoolean("align_items_to_bottom");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items, true);
        tag.putBoolean("align_items_to_bottom", alignItemsToBottom);
    }

    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
    @Override public int getContainerSize() { return 3; }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }
    @Override public ItemStack removeItem(int slot, int count) { return ContainerHelper.removeItem(items, slot, count); }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items, slot); }
    @Override public void setItem(int slot, ItemStack stack) { items.set(slot, stack); setChanged(); }
    @Override public void clearContent() { items.clear(); setChanged(); }
    @Override public boolean stillValid(Player player) { return Container.stillValidBlockEntity(this, player); }

    public ItemStack swapItemNoUpdate(int slot, ItemStack stack) {
        ItemStack old = items.get(slot);
        items.set(slot, stack);
        return old;
    }

    public void setChanged(GameEvent event) {
        super.setChanged();
        if (level != null) {
            if (event != null) level.gameEvent(event, worldPosition, GameEvent.Context.of(getBlockState()));
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    @Override public void setChanged() { setChanged(GameEvent.BLOCK_ACTIVATE); }

    public boolean getAlignItemsToBottom() { return alignItemsToBottom; }
}
