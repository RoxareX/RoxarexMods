package net.roxarex.chat;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.roxarex.RoxareXMods;
import net.roxarex.mixin.ChatComponentAccessor;

import java.lang.reflect.Method;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class WidgetsInitialization {

    private static InfoWidget infoWidget;

    private static ChatComponent predicateTarget;

    /**
     * The vanilla message filter installed on the ChatComponent. It is checked
     * by 26.2 both at insertion time (private 4-arg addMessage) and when the
     * trimmed display list is refreshed, so a single delegate covers live
     * gating. Must never be set to null: ChatComponent dereferences it
     * without a null check (its own default is an always-true lambda).
     */
    private static final Predicate<GuiMessage> VISIBLE_MESSAGE_FILTER = msg -> ChatFilter.shouldShow(msg.content());

    /**
     * The private addMessage(Component, MessageSignature, GuiMessageSource, GuiMessageTag)
     * used to re-add the stored history when the filter switches.
     */
    private static final Method ADD_MESSAGE;

    static {
        try {
            ADD_MESSAGE = ChatComponent.class.getDeclaredMethod(
                    "addMessage",
                    Component.class,
                    MessageSignature.class,
                    GuiMessageSource.class,
                    GuiMessageTag.class
            );
            ADD_MESSAGE.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static void init() {
        WidgetManager manager = WidgetManager.get();

        int ww = 11, wh = 11;
        int ax = 300, ay = 22;

        SimpleActionWidget allBtn = new SimpleActionWidget(
                ax, ay, ww, wh,
                Component.literal("A"),
                Component.literal("Switch to all chat"),
                () -> !ChatFilter.isPartyFilterEnabled() && !ChatFilter.isGuildFilterEnabled(),
                (mx, my) -> {
                    if (ChatFilter.isPartyFilterEnabled() || ChatFilter.isGuildFilterEnabled()) {
                        ChatFilter.setPartyFilterEnabled(false);
                        ChatFilter.setGuildFilterEnabled(false);
                        applyFilter(Component.literal("Filtering > All"));
                        RoxareXMods.LOGGER.info("Filter OFF");
                    }
                }
        );

        SimpleActionWidget partyBtn = new SimpleActionWidget(
                ax + ww + 3, ay, ww, wh,
                Component.literal("P"),
                Component.literal("Switch to party chat only"),
                () -> ChatFilter.isPartyFilterEnabled(),
                (mx, my) -> {
                    if (!ChatFilter.isPartyFilterEnabled()) {
                        ChatFilter.setPartyFilterEnabled(true);
                        ChatFilter.setGuildFilterEnabled(false);
                        applyFilter(Component.literal("Filtering > Party"));
                        RoxareXMods.LOGGER.info("Party filter ON");
                    }
                }
        );

        SimpleActionWidget guildBtn = new SimpleActionWidget(
                ax + (ww + 3) * 2, ay, ww, wh,
                Component.literal("G"),
                Component.literal("Switch to guild chat only"),
                () -> ChatFilter.isGuildFilterEnabled(),
                (mx, my) -> {
                    if (!ChatFilter.isGuildFilterEnabled()) {
                        ChatFilter.setGuildFilterEnabled(true);
                        ChatFilter.setPartyFilterEnabled(false);
                        applyFilter(Component.literal("Filtering > Guild"));
                        RoxareXMods.LOGGER.info("Guild filter ON");
                    }
                }
        );

        infoWidget = new InfoWidget(
                4 + (ww + 3) * 2, ay, ww, wh,
                Component.literal("Filtering > All")
        );

        // Register HUD element for rendering widgets on HUD
        WidgetManager.registerHudElement(infoWidget);

        manager.register(allBtn);
        manager.register(partyBtn);
        manager.register(guildBtn);

        registerChatFilter();
        AttachToScreen(manager);
    }

    /**
     * Applies the currently active filter to the vanilla chat display.
     *
     * The display lists are wiped through direct field access (NOT
     * clearMessages, which ChatPatches cancels by default) and then the
     * master history for the active filter is re-added, oldest first, so the
     * newest message ends up on top. Because the wipe and the re-add happen
     * in a single main-thread step, no duplication is possible — with or
     * without ChatPatches installed.
     */
    private static void applyFilter(Component infoMessage) {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            try {
                ChatComponent chatComponent = mc.gui.hud.getChat();
                ChatComponentAccessor accessor = (ChatComponentAccessor) chatComponent;

                accessor.roxarex_getAllMessages().clear();
                accessor.roxarex_getTrimmedMessages().clear();
                accessor.roxarex_getMessageDeletionQueue().clear();

                ChatFilter.beginReadding();
                try {
                    for (Component message : ChatFilter.getFilteredMessagesArray()) {
                        ADD_MESSAGE.invoke(chatComponent, message, null, null, null);
                    }
                } finally {
                    ChatFilter.endReadding();
                }

                chatComponent.resetChatScroll();

                // Update info widget
                infoWidget.setMessage(infoMessage);
            } catch (Exception e) {
                RoxareXMods.LOGGER.error("Failed to apply chat filter: {}", e.toString());
                e.printStackTrace();
            }
        });
    }

    private static void registerChatFilter() {
        // Fresh history per world session; filter flags and the installed
        // predicate intentionally persist across worlds.
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ChatFilter.clearAllMessages());
    }

    private static void AttachToScreen(WidgetManager manager) {
        ClientTickEvents.END_CLIENT_TICK.register(clientTick -> {
            ChatComponent chatComponent = clientTick.gui.hud.getChat();
            if (chatComponent != null && chatComponent != predicateTarget) {
                predicateTarget = chatComponent;
                chatComponent.setVisibleMessageFilter(VISIBLE_MESSAGE_FILTER);
            }

            Screen current = clientTick.gui.screen();
            if (current instanceof ChatScreen) {
                try {
                    manager.attachToScreen(current);
                } catch (NoSuchElementException e) {
                    RoxareXMods.LOGGER.warn("Could not attach widgets to chat screen (modified by another mod?): {}", e.getMessage());
                }
            } else {
                manager.detach();
            }
        });
    }
}
