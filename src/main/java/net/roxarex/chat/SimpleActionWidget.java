package net.roxarex.chat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

public class SimpleActionWidget extends BaseWidget {
    private final Component hint;
    private final BooleanSupplier isActive;

    public SimpleActionWidget(int x, int y, int width, int height, Component label, Component hint, BooleanSupplier isActive, ClickAction action) {
        super(x, y, width, height, label);
        this.hint = hint;
        this.isActive = isActive;
        this.clickAction = action;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        // Draw background first (before text so it's visible)
        int bg = isActive.getAsBoolean() ? 0x8800AA44 : 0x88222222;
        graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bg);

        // Border lines
        graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + 1, 0x99FFFFFF);
        graphics.fill(this.getX(), this.getY() + this.height - 1, this.getX() + this.width, this.getY() + this.height, 0x99FFFFFF);

        // Draw label centered in the widget (no super call to avoid duplicate rendering)
        // Font height is 8px; (height - 8) / 2 vertically centers it
        String text = this.getMessage().getString();
        int textWidth = Minecraft.getInstance().font.width(text);
        graphics.text(Minecraft.getInstance().font,
                text,
                this.getX() + this.width / 2 - textWidth / 2 + 1,
                this.getY() + (this.height - 7) / 2,
                0xFFFFFFFF,
                false
        );
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput builder) {
        builder.add(NarratedElementType.HINT, hint);
    }
}