package com.fluxion.content.combustion;

import com.fluxion.Fluxion;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CombustionGeneratorScreen extends AbstractContainerScreen<CombustionGeneratorMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Fluxion.MOD_ID, "textures/gui/combustion_generator.png");

    // Energy bar fill area
    private static final int ENERGY_X = 152;
    private static final int ENERGY_Y = 17;
    private static final int ENERGY_WIDTH = 16;
    private static final int ENERGY_HEIGHT = 52;
    // Sprites to the right of the 176px GUI panel in the texture
    private static final int FLAME_U = 176;
    private static final int FLAME_V = 0;
    private static final int ENERGY_FILL_U = 176;
    private static final int ENERGY_FILL_V = 14;

    public CombustionGeneratorScreen(CombustionGeneratorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        if (menu.isLit()) {
            int flame = menu.getLitProgress(14);
            graphics.blit(TEXTURE, x + 81, y + 18 + (14 - flame), FLAME_U, FLAME_V + (14 - flame), 14, flame);
        }

        int capacity = menu.getCapacity();
        if (capacity > 0) {
            int fill = (int) ((long) menu.getEnergy() * ENERGY_HEIGHT / capacity);
            graphics.blit(TEXTURE, x + ENERGY_X, y + ENERGY_Y + (ENERGY_HEIGHT - fill),
                    ENERGY_FILL_U, ENERGY_FILL_V + (ENERGY_HEIGHT - fill), ENERGY_WIDTH, fill);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);

        if (isHovering(ENERGY_X, ENERGY_Y, ENERGY_WIDTH, ENERGY_HEIGHT, mouseX, mouseY)) {
            graphics.renderTooltip(font,
                    Component.translatable("gui.fluxion.energy",
                            String.format("%,d", menu.getEnergy()),
                            String.format("%,d", menu.getCapacity())),
                    mouseX, mouseY);
        }
    }
}
