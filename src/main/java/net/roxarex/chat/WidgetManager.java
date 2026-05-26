package net.roxarex.chat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class WidgetManager {
    private static final WidgetManager INSTANCE = new WidgetManager();
    private final List<BaseWidget> widgets = new CopyOnWriteArrayList<>();
    private volatile Screen attachedScreen = null;
    private Method addRenderableMethod = null;
    private boolean positionSetFlag = false;


    private WidgetManager() {}

    public static WidgetManager get() { return INSTANCE; }

    public void register(BaseWidget w) { widgets.add(w); }

    // unused but might want to use later
    public void renderHud(GuiGraphics graphics, float tickDelta) {
        Minecraft client = Minecraft.getInstance();
        if (client.screen == null) {
            int mx = (int) client.mouseHandler.xpos();
            int my = (int) client.mouseHandler.ypos();

            int margin = 4;
            int spacing = 4;
            int startX = margin;
            int windowHeight = client.getWindow().getGuiScaledHeight(); // used to anchor from bottom

            // Stack widgets to the right from bottom-left (all aligned to bottom)
            for (BaseWidget w : widgets) {
                int widgetY = windowHeight - margin - w.getWidgetHeight() - 14;
                w.setPosition(startX, widgetY);
                w.render(graphics, mx, my, tickDelta);
                startX += w.getWidgetWidth() + spacing;
            }
        }
    }

    public void attachToScreen(Screen screen) {
        if (screen == attachedScreen) return;
        try {
            if (addRenderableMethod == null) {
                addRenderableMethod = java.util.Arrays.stream(Screen.class.getDeclaredMethods())
                        .filter(md -> md.getName().equals("addRenderableWidget") && md.getParameterCount() == 1)
                        .findFirst().orElseThrow();
                addRenderableMethod.setAccessible(true);
            }
            Minecraft client = Minecraft.getInstance();
            int windowHeight = client.getWindow().getGuiScaledHeight();
            for (BaseWidget w : widgets) {
                if (!positionSetFlag) {
                    w.setPosition(w.getX(), windowHeight - w.getWidgetHeight() - w.getY());
                }
                addRenderableMethod.invoke(screen, w);
            }
            attachedScreen = screen;
            positionSetFlag = true;
        } catch (ReflectiveOperationException e) {
            net.roxarex.RoxareXMods.LOGGER.error("WidgetManager failed to attach widgets", e);
        }
    }

    public void detach() {
        attachedScreen = null;
    }
}
