package com.fluxion.content.combustion;

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
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.SlotItemHandler;

public class CombustionGeneratorMenu extends AbstractContainerMenu {
    private static final int DATA_ENERGY = 0;
    private static final int DATA_CAPACITY = 1;
    private static final int DATA_LIT_TIME = 2;
    private static final int DATA_LIT_DURATION = 3;
    private static final int LOGICAL_DATA_COUNT = 4;

    private static final int MACHINE_SLOTS = 1;
    private static final int PLAYER_INV_START = MACHINE_SLOTS;
    private static final int HOTBAR_START = PLAYER_INV_START + 27;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final CombustionGeneratorBlockEntity blockEntity;
    private final SplitContainerData data;

    /** Client-side constructor: the position was written by {@code NetworkHooks.openScreen}. */
    public CombustionGeneratorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, clientBlockEntity(playerInventory, buf),
                new SimpleContainerData(LOGICAL_DATA_COUNT));
    }

    /** Server-side constructor. */
    public CombustionGeneratorMenu(int containerId, Inventory playerInventory, CombustionGeneratorBlockEntity be) {
        this(containerId, playerInventory, be, new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case DATA_ENERGY -> be.getEnergyStored();
                    case DATA_CAPACITY -> be.getMaxEnergyStored();
                    case DATA_LIT_TIME -> be.getLitTime();
                    case DATA_LIT_DURATION -> be.getLitDuration();
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

    private CombustionGeneratorMenu(int containerId, Inventory playerInventory,
                                    CombustionGeneratorBlockEntity be, ContainerData logicalData) {
        super(ModMenus.COMBUSTION_GENERATOR.get(), containerId);
        this.blockEntity = be;
        this.data = new SplitContainerData(logicalData);

        addSlot(new SlotItemHandler(be.getFuelHandler(), CombustionGeneratorBlockEntity.FUEL_SLOT, 80, 35));

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

    private static CombustionGeneratorBlockEntity clientBlockEntity(Inventory playerInventory, FriendlyByteBuf buf) {
        BlockEntity be = playerInventory.player.level().getBlockEntity(buf.readBlockPos());
        if (be instanceof CombustionGeneratorBlockEntity generator) {
            return generator;
        }
        throw new IllegalStateException("Expected a combustion generator block entity");
    }

    public int getEnergy() {
        return data.getCombined(DATA_ENERGY);
    }

    public int getCapacity() {
        return data.getCombined(DATA_CAPACITY);
    }

    public boolean isLit() {
        return data.getCombined(DATA_LIT_TIME) > 0;
    }

    /** Remaining burn as 0..height pixels for the flame indicator. */
    public int getLitProgress(int height) {
        int duration = data.getCombined(DATA_LIT_DURATION);
        if (duration == 0) {
            return 0;
        }
        return Math.min(height, data.getCombined(DATA_LIT_TIME) * height / duration);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index < MACHINE_SLOTS) {
            if (!moveItemStackTo(stack, PLAYER_INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.getBurnTime(RecipeType.SMELTING) > 0
                && moveItemStackTo(stack, 0, MACHINE_SLOTS, false)) {
            // moved into the fuel slot
        } else if (index < HOTBAR_START) {
            if (!moveItemStackTo(stack, HOTBAR_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, PLAYER_INV_START, HOTBAR_START, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity != null && !blockEntity.isRemoved()
                && player.distanceToSqr(Vec3.atCenterOf(blockEntity.getBlockPos())) <= 64.0;
    }
}
