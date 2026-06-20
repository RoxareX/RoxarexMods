package net.roxarex.chat;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.roxarex.RoxareXMods;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

public class WidgetsInitialization {

    private static boolean partyFilterEnabled = false;
    private static boolean guildFilterEnabled = false;

    private static final Deque<Component> allMessages   = new ArrayDeque<>();
    private static final Deque<Component> partyMessages = new ArrayDeque<>();
    private static final Deque<Component> guildMessages = new ArrayDeque<>();

    private static final int MAX_MESSAGES  = 10_000;
    private static final int DISPLAY_LIMIT = 100;

    private static InfoWidget infoWidget;

    private static final Pattern PARTY_PATTERN =
            Pattern.compile("^(.\\d{2}:\\d{2}:\\d{2}]\\s)?Party > .*");
    private static final Pattern GUILD_PATTERN =
            Pattern.compile("^(.\\d{2}:\\d{2}:\\d{2}]\\s)?Guild > .*");

    // Strips §X color codes from a raw Minecraft chat string
    private static final Pattern COLOR_CODE = Pattern.compile("§[0-9a-fk-orA-FK-OR]");

    private static String strip(String s) {
        return COLOR_CODE.matcher(s).replaceAll("");
    }

    public static void init() {
        WidgetManager manager = WidgetManager.get();
        Minecraft mc = Minecraft.getInstance();

        int ww = 11, wh = 11;
        int ax = 300, ay = 22;

        SimpleActionWidget allBtn = new SimpleActionWidget(
                ax, ay, ww, wh,
                Component.literal("A"),
                Component.literal("Switch to all chat"),
                () -> !partyFilterEnabled && !guildFilterEnabled,
                (mx, my) -> {
                    if (partyFilterEnabled || guildFilterEnabled) {
                        partyFilterEnabled = false;
                        guildFilterEnabled = false;
//                        rebuildChat(mc, allMessages);
                        infoWidget.setMessage(Component.literal("Filtering > All"));
                        RoxareXMods.LOGGER.info("Filter OFF");
                    }
                }
        );

        SimpleActionWidget partyBtn = new SimpleActionWidget(
                ax + ww + 3, ay, ww, wh,
                Component.literal("P"),
                Component.literal("Switch to party chat only"),
                () -> partyFilterEnabled,
                (mx, my) -> {
                    if (!partyFilterEnabled) {
                        partyFilterEnabled = true;
                        guildFilterEnabled = false;
//                        rebuildChat(mc, partyMessages);
                        infoWidget.setMessage(Component.literal("Filtering > Party"));
                        RoxareXMods.LOGGER.info("Party filter ON");
                    }
                }
        );

        SimpleActionWidget guildBtn = new SimpleActionWidget(
                ax + (ww + 3) * 2, ay, ww, wh,
                Component.literal("G"),
                Component.literal("Switch to guild chat only"),
                () -> guildFilterEnabled,
                (mx, my) -> {
                    if (!guildFilterEnabled) {
                        guildFilterEnabled = true;
                        partyFilterEnabled = false;
//                        rebuildChat(mc, guildMessages);
                        infoWidget.setMessage(Component.literal("Filtering > Guild"));
                        RoxareXMods.LOGGER.info("Guild filter ON");
                    }
                }
        );

        infoWidget = new InfoWidget(
                4 + (ww + 3) * 2, ay, ww, wh,
                Component.literal("Filtering > All")
        );

        // Register HUD element for rendering widgets on HUD
//        manager.setHudLayout(ax, ay, ww, wh);
        WidgetManager.registerHudElement(infoWidget);

        manager.register(allBtn);
        manager.register(partyBtn);
        manager.register(guildBtn);

        registerChatFilter();
        AttachToScreen(manager);
    }

    private static void rebuildChat(Minecraft mc, Deque<Component> source) {
        // Note: ChatComponent.addMessage signature changed in 26.1
        // This method is no longer compatible with the new API
        mc.execute(() -> {
            // The new addMessage requires Component, MessageSignature, GuiMessageSource, GuiMessageTag
            // For now, we skip chat rebuilding until proper integration
//            ChatComponent chat = mc.gui.getChat();
//            chat.clearMessages(false);
//
//            Component[] snapshot = source.toArray(new Component[0]);
//            int start = Math.max(0, snapshot.length - DISPLAY_LIMIT);
//            for (int i = start; i < snapshot.length; i++) {
//                chat.addMessage(snapshot[i]);
//            }
        });
    }

    private static void registerChatFilter() {
        // Signed player chat (vanilla servers)
        ClientReceiveMessageEvents.ALLOW_CHAT.register(
                (message, signedMessage, sender, params, receptionTimestamp) ->
                        filterMessage(message)
        );

        // System/game messages — Hypixel sends ALL chat this way (party, guild, etc.)
        ClientReceiveMessageEvents.ALLOW_GAME.register(
                (message, overlay) -> {
                    if (overlay) return true; // never filter the action bar
                    return filterMessage(message);
                }
        );
    }

    /**
     * Stores the message in the appropriate history lists and returns
     * whether it should be shown given the current filter state.
     */
    private static boolean filterMessage(Component message) {
        String text = strip(message.getString());
        boolean isParty = PARTY_PATTERN.matcher(text).matches();
        boolean isGuild = !isParty && GUILD_PATTERN.matcher(text).matches();

        store(allMessages, message);
        if (isParty) store(partyMessages, message);
        if (isGuild) store(guildMessages, message);

        if (partyFilterEnabled) return isParty;
        if (guildFilterEnabled) return isGuild;
        return true;
    }

    private static void store(Deque<Component> deque, Component message) {
        if (deque.size() >= MAX_MESSAGES) deque.pollFirst();
        deque.addLast(message);
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