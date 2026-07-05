package com.fluxion.content.cell;

import com.fluxion.registry.ModMenus;
import com.fluxion.util.SplitContainerData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class EnergyCellMenu extends AbstractContainerMenu {
    private static final int DATA_ENERGY = 0;
    private static final int DATA_CAPACITY = 1;
    private static final int LOGICAL_DATA_COUNT = 2;

    private final EnergyCellBlockEntity blockEntity;
    private final SplitContainerData data;

    /** Client-side constructor. */
    public EnergyCellMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, clientBlockEntity(playerInventory, buf),
                new SimpleContainerData(LOGICAL_DATA_COUNT));
    }

    /** Server-side constructor. */
    public EnergyCellMenu(int containerId, Inventory playerInventory, EnergyCellBlockEntity be) {
        this(containerId, playerInventory, be, new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case DATA_ENERGY -> be.getEnergyStored();
                    case DATA_CAPACITY -> be.getMaxEnergyStored();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return LOGICAL_DATA_COUNT;
            }
        });
    }

    private EnergyCellMenu(int containerId, Inventory playerInventory,
                           EnergyCellBlockEntity be, ContainerData logicalData) {
        super(ModMenus.ENERGY_CELL.get(), containerId);
        this.blockEntity = be;
        this.data = new SplitContainerData(logicalData);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }

        addDataSlots(this.data);
    }

    private static EnergyCellBlockEntity clientBlockEntity(Inventory playerInventory, FriendlyByteBuf buf) {
        BlockEntity be = playerInventory.player.level().getBlockEntity(buf.readBlockPos());
        if (be instanceof EnergyCellBlockEntity cell) {
            return cell;
        }
        throw new IllegalStateException("Expected an energy cell block entity");
    }

    public int getEnergy() {
        return data.getCombined(DATA_ENERGY);
    }

    public int getCapacity() {
        return data.getCombined(DATA_CAPACITY);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // No machine slots to move into or out of.
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity != null && !blockEntity.isRemoved()
                && player.distanceToSqr(Vec3.atCenterOf(blockEntity.getBlockPos())) <= 64.0;
    }
}
