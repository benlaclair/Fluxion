package com.fluxion.content.combustion;

import com.fluxion.FluxionConfig;
import com.fluxion.energy.ModEnergyStorage;
import com.fluxion.machine.AbstractFluxMachineBE;
import com.fluxion.machine.MachineTier;
import com.fluxion.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Burns anything with a vanilla furnace burn time. Generates FE while lit,
 * only starts new fuel when the buffer has room, and pushes FE to adjacent
 * receivers every tick. Tier scales output, buffer, transfer rate, and fuel
 * efficiency (burn-time multiplier).
 */
public class CombustionGeneratorBlockEntity extends AbstractFluxMachineBE implements MenuProvider {
    public static final int FUEL_SLOT = 0;

    private final ItemStackHandler fuelItems = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.getBurnTime(RecipeType.SMELTING) > 0;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final LazyOptional<ItemStackHandler> fuelCap = LazyOptional.of(() -> fuelItems);

    private int litTime;
    private int litDuration;

    public CombustionGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMBUSTION_GENERATOR.get(), pos, state, tierOf(state));
    }

    private static MachineTier tierOf(BlockState state) {
        return ((CombustionGeneratorBlock) state.getBlock()).getTier();
    }

    @Override
    protected ModEnergyStorage createEnergyStorage() {
        // External receive is 0: generators produce, they don't accept.
        return new ModEnergyStorage(
                FluxionConfig.COMBUSTION_BUFFER[tier.index()].get(),
                0,
                FluxionConfig.COMBUSTION_TRANSFER_RATE[tier.index()].get(),
                this::setChanged);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CombustionGeneratorBlockEntity be) {
        be.tickServer(state);
    }

    private void tickServer(BlockState state) {
        boolean wasLit = isLit();

        if (litTime > 0) {
            litTime--;
            energy.addEnergy(getFePerTick());
        }

        if (litTime <= 0 && energy.getEnergyStored() < energy.getMaxEnergyStored()) {
            ItemStack fuel = fuelItems.getStackInSlot(FUEL_SLOT);
            int burnTime = fuel.getBurnTime(RecipeType.SMELTING);
            if (burnTime > 0) {
                litDuration = Math.max(1, (int) (burnTime * getFuelEfficiency()));
                litTime = litDuration;
                if (fuel.hasCraftingRemainingItem()) {
                    fuelItems.setStackInSlot(FUEL_SLOT, fuel.getCraftingRemainingItem());
                } else {
                    fuel.shrink(1);
                    fuelItems.setStackInSlot(FUEL_SLOT, fuel);
                }
            }
        }

        if (isLit() != wasLit) {
            level.setBlock(worldPosition, state.setValue(CombustionGeneratorBlock.LIT, isLit()), Block.UPDATE_ALL);
        }

        pushEnergy(FluxionConfig.COMBUSTION_TRANSFER_RATE[tier.index()].get());
    }

    private int getFePerTick() {
        return FluxionConfig.COMBUSTION_FE_PER_TICK[tier.index()].get();
    }

    private double getFuelEfficiency() {
        return FluxionConfig.COMBUSTION_FUEL_EFFICIENCY[tier.index()].get();
    }

    public boolean isLit() {
        return litTime > 0;
    }

    public int getLitTime() {
        return litTime;
    }

    public int getLitDuration() {
        return litDuration;
    }

    public ItemStackHandler getFuelHandler() {
        return fuelItems;
    }

    @Override
    public void dropContents(Level level, BlockPos pos) {
        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), fuelItems.getStackInSlot(FUEL_SLOT));
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return fuelCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fuelCap.invalidate();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        fuelItems.deserializeNBT(tag.getCompound("Inventory"));
        litTime = tag.getInt("LitTime");
        litDuration = tag.getInt("LitDuration");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", fuelItems.serializeNBT());
        tag.putInt("LitTime", litTime);
        tag.putInt("LitDuration", litDuration);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new CombustionGeneratorMenu(containerId, playerInventory, this);
    }
}
