package net.roxarex.chat;

import net.minecraft.network.chat.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Manages chat filtering state and provides filtering logic for the ChatComponentMixin.
 * This class stores messages and determines which messages should be shown in the vanilla chat.
 */
public class ChatFilter {
    private static boolean partyFilterEnabled = false;
    private static boolean guildFilterEnabled = false;
    private static boolean skipStoreOnShouldShow = false;

    private static final Deque<Component> allMessages = new ArrayDeque<>();
    private static final Deque<Component> partyMessages = new ArrayDeque<>();
    private static final Deque<Component> guildMessages = new ArrayDeque<>();

    private static final int MAX_MESSAGES = 10_000;
    // Deduplication set: tracks recently seen message texts to prevent storing duplicates
    // when the same message arrives via both ALLOW_CHAT and ALLOW_GAME events.
    private static final Set<String> recentMessageTexts = new HashSet<>();
    private static final int MAX_RECENT_TEXTS = 500;

    private static final Pattern PARTY_PATTERN =
            Pattern.compile("^(.\\d{2}:\\d{2}:\\d{2}]\\s)?Party > .*");
    private static final Pattern GUILD_PATTERN =
            Pattern.compile("^(.\\d{2}:\\d{2}:\\d{2}]\\s)?Guild > .*");

    // Strips §X color codes from a raw Minecraft chat string
    private static final Pattern COLOR_CODE = Pattern.compile("§[0-9a-fk-orA-FK-OR]");

    private static String strip(String s) {
        return COLOR_CODE.matcher(s).replaceAll("");
    }

    /**
     * Called by ChatComponentMixin to determine if a message should be shown.
     * @param message the message to check
     * @return true if the message should be shown in vanilla chat
     */
    public static boolean shouldShow(Component message) {
        String text = strip(message.getString());
        boolean isParty = PARTY_PATTERN.matcher(text).matches();
        boolean isGuild = !isParty && GUILD_PATTERN.matcher(text).matches();

        // Deduplication: skip if we've already seen this exact message text
        // This prevents storing duplicates when the same message arrives via both
        // ALLOW_CHAT and ALLOW_GAME event channels.
        if (!recentMessageTexts.add(text)) {
            // Duplicate detected - still check filter rules but don't store
            if (partyFilterEnabled) return isParty;
            if (guildFilterEnabled) return isGuild;
            return true;
        }

        // Store the message in appropriate history (skip during rebuild)
        if (!skipStoreOnShouldShow) {
            store(allMessages, message);
            if (isParty) store(partyMessages, message);
            if (isGuild) store(guildMessages, message);
            // Clean up old texts if set gets too large
            if (recentMessageTexts.size() > MAX_RECENT_TEXTS) {
                recentMessageTexts.clear();
            }
        }

        // Check if message should be shown based on current filter
        if (partyFilterEnabled) return isParty;
        if (guildFilterEnabled) return isGuild;
        return true;
    }

    /**
     * Store a message in the deque, trimming if necessary.
     */
    private static void store(Deque<Component> deque, Component message) {
        if (deque.size() >= MAX_MESSAGES) deque.pollFirst();
        deque.addLast(message);
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

    /**
     * Clear all stored messages. Called when switching filters.
     */
    public static void clearAllMessages() {
        allMessages.clear();
        partyMessages.clear();
        guildMessages.clear();
        recentMessageTexts.clear();
    }

    /**
     * Get the messages for the current filter.
     * Used to rebuild the chat display when switching filters.
     */
    public static Deque<Component> getMessagesForFilter() {
        if (partyFilterEnabled) return partyMessages;
        if (guildFilterEnabled) return guildMessages;
        return allMessages;
    }

    /**
     * Get a copy of the messages for the current filter as an array.
     * Returns messages in display order (oldest first).
     */
    public static Component[] getFilteredMessagesArray() {
        Deque<Component> messages = getMessagesForFilter();
        return messages.toArray(Component[]::new);
    }

    /**
     * Set whether to skip storing messages when shouldShow is called.
     * Used during rebuild to prevent duplication.
     */
    public static void setSkipStoreOnShouldShow(boolean skip) {
        skipStoreOnShouldShow = skip;
    }
}
