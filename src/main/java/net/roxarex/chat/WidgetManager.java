package net.roxarex.chat;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;

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

    public void renderHud(GuiGraphicsExtractor graphics, float tickDelta, BaseWidget w) {
        Minecraft client = Minecraft.getInstance();
        if (client.gui.screen() == null) {
            int mx = (int) client.mouseHandler.xpos();
            int my = (int) client.mouseHandler.ypos();

            int margin = 4;
            int windowHeight = client.getWindow().getGuiScaledHeight();

            int widgetY = windowHeight - margin - w.getWidgetHeight() - 14;
            w.setPosition(w.getX(), widgetY);
            w.extractWidgetRenderState(graphics, mx, my, tickDelta);
        }
    }

    /**
     * Registers a HUD element that renders widgets on the HUD.
     * This should be called once during initialization.
     */
    public static void registerHudElement(BaseWidget w) {
        HudElementRegistry.attachElementBefore(
            VanillaHudElements.CHAT,
            Identifier.fromNamespaceAndPath("roxarexmods", "chat_filters"),
            (graphics, deltaTracker) -> WidgetManager.get().renderHud(graphics, deltaTracker.getGameTimeDeltaPartialTick(false), w)
        );
    }

    public void attachToScreen(Screen screen) {
        if (screen == attachedScreen) return;

        Minecraft client = Minecraft.getInstance();
        int windowHeight = client.getWindow().getGuiScaledHeight();

        // Get widgets list via Screens.getWidgets() and add our widgets
        @SuppressWarnings("unchecked")
        List<AbstractWidget> screenWidgets = Screens.getWidgets(screen);

        // Remove widgets from screen first to avoid duplicates
        screenWidgets.removeAll(widgets);

        for (BaseWidget w : widgets) {
            if (!setOriginalPositionFlag) {
                originalY = w.getY();
                setOriginalPositionFlag = true;
            }
            w.setPosition(w.getX(), windowHeight - w.getWidgetHeight() - originalY);
            if (!screenWidgets.contains(w)) {
                screenWidgets.add(w);
            }
        }
        attachedScreen = screen;
    }

    public void detach() {
        attachedScreen = null;
    }
}