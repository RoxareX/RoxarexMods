package net.roxarex.chat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public abstract class BaseWidget extends AbstractWidget {
    protected ClickAction clickAction;

    public BaseWidget(int x, int y, int width, int height, Component label) {
        super(x, y, width, height, label);
    }

    public void setClickAction(ClickAction action) { this.clickAction = action; }

    @Override
    protected void updateWidgetNarration(@NonNull NarrationElementOutput builder) { }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        int startColor = 0xFF222222;
        int endColor   = 0xFF555555;
        if (isHovered()) {
            graphics.fillGradient(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, startColor, endColor);
        }

        if (!this.getMessage().getString().isEmpty()) {
            graphics.drawString(Minecraft.getInstance().font, this.getMessage().getString(), this.getX() + 4, this.getY() + (this.height - 8) / 2, 0xFFFFFF, false);
        }
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent mouseButtonEvent, boolean bl) {
        double mouseX = mouseButtonEvent.x();
        double mouseY = mouseButtonEvent.y();
        if (isMouseOver(mouseX, mouseY)) {
            if (clickAction != null) clickAction.onClick((int) mouseX, (int) mouseY);
            return true;
        }
        return false;
    }

    public boolean isMouseOver(double mx, double my) {
        return mx >= this.getX() && mx < this.getX() + this.width
                && my >= this.getY() && my < this.getY() + this.height;
    }

    // Delegates to AbstractWidget's own setX/setY — no reflection, no remapping issues
    @Override
    public void setPosition(int x, int y) {
        super.setPosition(x, y);
    }

    public int getWidgetWidth()  { return this.width; }
    public int getWidgetHeight() { return this.height; }
}