package net.roxarex.mixin;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/**
 * Exposes the private message lists of ChatComponent so the chat filter can
 * wipe the display state directly. This deliberately bypasses
 * {@code clearMessages(boolean)}: ChatPatches hooks that method (cancellable,
 * HEAD) and its default config cancels the full clear, which would leave the
 * old messages in place when the filter rebuilds the display.
 */
@Mixin(ChatComponent.class)
public interface ChatComponentAccessor {

    @Accessor("allMessages")
    List<GuiMessage> roxarex_getAllMessages();

    @Accessor("trimmedMessages")
    List<GuiMessage.Line> roxarex_getTrimmedMessages();

    @SuppressWarnings("rawtypes")
    @Accessor("messageDeletionQueue")
    List roxarex_getMessageDeletionQueue();
}
