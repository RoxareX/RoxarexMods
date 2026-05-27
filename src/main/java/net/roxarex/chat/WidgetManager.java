package net.roxarex.chat;

import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class WidgetManager {
    private static final WidgetManager INSTANCE = new WidgetManager();
    private final List<BaseWidget> widgets = new CopyOnWriteArrayList<>();
    private volatile Screen attachedScreen = null;
    private boolean setOriginalPositionFlag = false;
    private int originalY = 0;

    private WidgetManager() {}

    public static WidgetManager get() { return INSTANCE; }

    public void register(BaseWidget w) { widgets.add(w); }

    public void renderHud(GuiGraphics graphics, float tickDelta, BaseWidget w) {
        Minecraft client = Minecraft.getInstance();
        if (client.screen == null) {
            int mx = (int) client.mouseHandler.xpos();
            int my = (int) client.mouseHandler.ypos();

            int margin = 4;
            int windowHeight = client.getWindow().getGuiScaledHeight();

            int widgetY = windowHeight - margin - w.getWidgetHeight() - 14;
            w.setPosition(w.getX(), widgetY);
            w.render(graphics, mx, my, tickDelta);
        }
    }

    public void attachToScreen(Screen screen) {
        if (screen == attachedScreen) return;

        Minecraft client = Minecraft.getInstance();
        int windowHeight = client.getWindow().getGuiScaledHeight();

        // Screens.getButtons() is Fabric's safe, remapping-agnostic way to add
        // interactive widgets to any screen — no reflection needed
        List<AbstractWidget> buttons = Screens.getButtons(screen);

        for (BaseWidget w : widgets) {
            if (!setOriginalPositionFlag) {
                originalY = w.getY();
                setOriginalPositionFlag = true;
            }
            w.setPosition(w.getX(), windowHeight - w.getWidgetHeight() - originalY);
            if (!buttons.contains(w)) {
                buttons.add(w);
            }
        }
        attachedScreen = screen;
    }

    public void detach() {
        attachedScreen = null;
    }
}