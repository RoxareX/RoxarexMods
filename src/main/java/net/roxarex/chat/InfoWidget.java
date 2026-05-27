package net.roxarex.chat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class InfoWidget extends BaseWidget {
    public InfoWidget(int x, int y, int width, int height, Component label) {
        super(x, y, width, height, label);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.renderWidget(graphics, mouseX, mouseY, delta);
        // maybe draw an info icon or extra indicator later
//        graphics.fill(this.getX(), this.getY(),  this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0xFFFFFFFF );
        graphics.drawCenteredString(
                Minecraft.getInstance().font,
                this.getMessage(),
                // X + 1 for screen offset + 4 for spacing = 5
                this.getX() + this.width / 2 + 5,
                this.getY() + (this.height - 7) / 2,
                0xFFFFFFFF
        );
    }
}
