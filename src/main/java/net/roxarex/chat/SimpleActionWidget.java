package net.roxarex.chat;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class SimpleActionWidget extends BaseWidget {
    public SimpleActionWidget(int x, int y, int width, int height, Component label, ClickAction action) {
        super(x, y, width, height, label);
        this.clickAction = action;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.renderWidget(graphics, mouseX, mouseY, delta);
        // Additional visuals (border)
        graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + 1, 0x99FFFFFF);
        graphics.fill(this.getX(), this.getY() + this.height - 1, this.getX() + this.width, this.getY() + this.height, 0x99FFFFFF);
    }
}
