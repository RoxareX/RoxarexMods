package net.roxarex.chat;

import net.minecraft.network.chat.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Manages chat filter state and the master chat history.
 *
 * Storage and visibility are separated:
 *  - {@link #track(Component)} is called exactly once per message, from the
 *    Fabric receive-message events (the single choke point through which all
 *    server chat messages pass). It stores the message in the master history
 *    deque. The Fabric handlers always let the message through; they never
 *    drop packets.
 *  - {@link #shouldShow(Component)} is a pure visibility check with no side
 *    effects. It drives the vanilla {@code visibleMessageFilter} predicate
 *    (installed on the {@code ChatComponent}) and the filter view returned by
 *    {@link #getFilteredMessagesArray()}.
 *
 * The vanilla ChatComponent only retains the messages its predicate accepts,
 * so while a filter is active the non-matching messages live exclusively in
 * the master deque. On every filter switch the chat display is rebuilt from
 * the master deque for the newly selected view, with full history and
 * correct ordering.
 */
public class ChatFilter {
    private static volatile boolean partyFilterEnabled = false;
    private static volatile boolean guildFilterEnabled = false;

    private static final Deque<Component> allMessages = new ArrayDeque<>();

    // Matches ChatPatches' default chatMaxMessages so that with that mod
    // installed we never hold less history than the vanilla cap they apply.
    private static final int MAX_MESSAGES = 16_384;

    // Hypixel prefixes party/guild chat with "Party > " / "Guild > ", optionally
    // after a "[HH:MM:SS] " timestamp. Match with find() so leading timestamps
    // and trailing content never break the match.
    private static final Pattern PARTY_PATTERN = Pattern.compile(".*Party > .*");
    private static final Pattern GUILD_PATTERN = Pattern.compile(".*Guild > .*");

    // Strips §X color codes from a raw Minecraft chat string
    private static final Pattern COLOR_CODE = Pattern.compile("§[0-9a-fk-orA-FK-OR]");

    /**
     * Pure visibility check with no side effects: returns whether the message
     * should be shown in the vanilla chat given the current filter state.
     * @param message the message to check
     * @return true if the message should be shown in vanilla chat
     */
    public static synchronized boolean shouldShow(Component message) {
        String text = toPlainText(message);
        boolean isParty = PARTY_PATTERN.matcher(text).find();
        boolean isGuild = !isParty && GUILD_PATTERN.matcher(text).find();

        if (partyFilterEnabled) return isParty;
        if (guildFilterEnabled) return isGuild;
        return true;
    }

    /**
     * Store a message in the master history. Called exactly once per message,
     * from the Fabric receive-message events.
     * @param message the message to store
     */
    public static synchronized void track(Component message) {
        if (allMessages.size() >= MAX_MESSAGES) allMessages.pollFirst();
        allMessages.addLast(message);
    }

    /**
     * Get the messages for the current filter, oldest first (the order in
     * which they must be re-added so the newest message ends up on top).
     * @return the history for the active filter as an array
     */
    public static synchronized Component[] getFilteredMessagesArray() {
        if (!partyFilterEnabled && !guildFilterEnabled) {
            return allMessages.toArray(Component[]::new);
        }
        List<Component> view = new ArrayList<>(allMessages.size());
        for (Component message : allMessages) {
            if (shouldShow(message)) view.add(message);
        }
        return view.toArray(new Component[0]);
    }

    /**
     * Clear the stored history. Called on disconnect so each world session
     * starts with a fresh history.
     */
    public static synchronized void clearAllMessages() {
        allMessages.clear();
    }

    /**
     * Extract the full plain text of a (possibly colored, multi-node) component
     * tree, with § color codes removed. Uses toFlatList() so Hypixel's colored
     * component trees are flattened correctly for pattern matching.
     */
    private static String toPlainText(Component component) {
        StringBuilder sb = new StringBuilder();
        for (Component part : component.toFlatList()) {
            sb.append(part.getString());
        }
        return COLOR_CODE.matcher(sb.toString()).replaceAll("");
    }

    /**
     * Get the current party filter state.
     */
    public static boolean isPartyFilterEnabled() {
        return partyFilterEnabled;
    }

    /**
     * Set the party filter state.
     */
    public static void setPartyFilterEnabled(boolean enabled) {
        partyFilterEnabled = enabled;
    }

    /**
     * Get the current guild filter state.
     */
    public static boolean isGuildFilterEnabled() {
        return guildFilterEnabled;
    }

    /**
     * Set the guild filter state.
     */
    public static void setGuildFilterEnabled(boolean enabled) {
        guildFilterEnabled = enabled;
    }
}
