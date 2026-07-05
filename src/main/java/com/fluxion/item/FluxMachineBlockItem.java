package com.fluxion.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Block item for machines. Shows carried-over stored energy (from breaking a
 * charged machine or crafting a tier upgrade) in the tooltip.
 */
public class FluxMachineBlockItem extends BlockItem {
    public FluxMachineBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        CompoundTag beTag = getBlockEntityData(stack);
        if (beTag != null && beTag.contains("Energy") && beTag.getInt("Energy") > 0) {
            tooltip.add(Component.translatable("tooltip.fluxion.stored_energy",
                    String.format("%,d", beTag.getInt("Energy"))).withStyle(ChatFormatting.GRAY));
        }
    }
}
