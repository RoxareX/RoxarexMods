package net.roxarex.chat;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.roxarex.RoxareXMods;

import java.util.NoSuchElementException;

public class WidgetsInitialization {

    private static InfoWidget infoWidget;

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
                        switchFilterAndRebuild(Component.literal("Filtering > All"));
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
                        switchFilterAndRebuild(Component.literal("Filtering > Party"));
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
                        switchFilterAndRebuild(Component.literal("Filtering > Guild"));
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
     * Clears the vanilla chat by accessing ChatComponent via the Minecraft instance.
     */
    /**
     * Combined method to clear and rebuild chat display atomically.
     * This prevents message duplication that occurs with separate async calls.
     */
    private static void switchFilterAndRebuild(Component infoMessage) {
        net.minecraft.client.Minecraft.getInstance().execute(() -> {
            try {
                var mc = net.minecraft.client.Minecraft.getInstance();
                var chatComponent = mc.gui.getChat();
                
                // Clear internal messages list using reflection to ensure proper clearing.
                // clearMessages(false) does not reliably clear the internal messages deque
                // in some Minecraft versions, causing message duplication on filter switch.
                try {
                    java.lang.reflect.Field messagesField = ChatComponent.class.getDeclaredField("messages");
                    messagesField.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    java.util.Collection<?> chatMessages = (java.util.Collection<?>) messagesField.get(chatComponent);
                    chatMessages.clear();
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    // Fall back to clearMessages if reflection fails
                    chatComponent.clearMessages(true);
                }
                
                // Skip storing messages during rebuild to prevent duplication
                ChatFilter.setSkipStoreOnShouldShow(true);
                
                Component[] filteredMessages = ChatFilter.getFilteredMessagesArray();
                
                // Use reflection to call the private addMessage method
                java.lang.reflect.Method addMessageMethod = ChatComponent.class.getDeclaredMethod(
                    "addMessage",
                    Component.class,
                    net.minecraft.network.chat.MessageSignature.class,
                    net.minecraft.client.multiplayer.chat.GuiMessageSource.class,
                    net.minecraft.client.multiplayer.chat.GuiMessageTag.class
                );
                addMessageMethod.setAccessible(true);
                
                for (Component msg : filteredMessages) {
                    addMessageMethod.invoke(chatComponent, msg, null, null, null);
                }
                
                // Re-enable storing messages
                ChatFilter.setSkipStoreOnShouldShow(false);
                
                // Update info widget
                infoWidget.setMessage(infoMessage);
            } catch (Exception e) {
                ChatFilter.setSkipStoreOnShouldShow(false);
                RoxareXMods.LOGGER.error("Failed to switch filter and rebuild: {}", e.getMessage());
            }
        });
    }

    private static void registerChatFilter() {
        // Signed player chat (vanilla servers)
        ClientReceiveMessageEvents.ALLOW_CHAT.register(
                (message, signedMessage, sender, params, receptionTimestamp) ->
                        ChatFilter.shouldShow(message)
        );

        // System/game messages — Hypixel sends ALL chat this way (party, guild, etc.)
        ClientReceiveMessageEvents.ALLOW_GAME.register(
                (message, overlay) -> {
                    if (overlay) return true; // never filter the action bar
                    return ChatFilter.shouldShow(message);
                }
        );
    }

    private static void AttachToScreen(WidgetManager manager) {
        ClientTickEvents.END_CLIENT_TICK.register(clientTick -> {
            Screen current = clientTick.screen;
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
