package net.roxarex.mixin;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.roxarex.chat.ChatFilter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Captures every message that reaches the private 4-arg
 * {@code addMessage} — the single choke point through which every displayed
 * message passes (network messages, ChatPatches' chatlog restore, its queued
 * drain, and our own filter re-adds, which the filter ignores) — into the
 * ChatFilter master history. Capturing here, instead of the Fabric
 * receive-message events, is what keeps the filter's re-add complete when
 * ChatPatches restores the saved chatlog: those restored messages never
 * pass through the network handlers, only through this entry point.
 */
@Mixin(ChatComponent.class)
public abstract class ChatMessageCaptureMixin {

    @Inject(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V",
            at = @At("HEAD")
    )
    private void roxarex_captureMessage(Component message, MessageSignature signature, GuiMessageSource source, GuiMessageTag tag, CallbackInfo ci) {
        ChatFilter.track(message);
    }
}
