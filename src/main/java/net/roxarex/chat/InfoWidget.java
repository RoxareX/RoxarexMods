package net.roxarex.chat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class InfoWidget extends BaseWidget {
    public InfoWidget(int x, int y, int width, int height, Component label) {
        super(x, y, width, height, label);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        // Draw semi-transparent background for the info widget
//        graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x88000000);

        // Draw the info text centered in the widget (no super call to avoid duplicate rendering)
        String text = this.getMessage().getString();
        int textWidth = Minecraft.getInstance().font.width(text);
        graphics.text(Minecraft.getInstance().font,
                text,
                this.getX() + this.width / 2 - textWidth / 2 + 5,
                this.getY() + (this.height - 7) / 2,
                0xFFFFFFFF, false);
    }
}
