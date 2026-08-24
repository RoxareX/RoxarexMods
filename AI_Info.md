# Chat Summary

## Task
Chat filter feature for RoxarexMods (Fabric, MC 26.2, Hypixel Skyblock): ALL / Party / Guild toggle widgets rendered in the chat HUD.

## Current Status
- ✅ **Vanilla (no other mods): WORKED** (old design: deques + `clearMessages(true)` + reflection re-add; dedup removed).
- ❌ **With Chat Patches (mrbuilder1961/ChatPatches, mod id `chatpatches`): WAS BROKEN.** Switching ALL → P → ALL did not remove non-matching messages and messages duplicated.
- ✅ **FIX IMPLEMENTED + COMPILES** (new design, see "New Design" below). Runtime test pass still pending (vanilla + ChatPatches × filter toggles).

## Root Cause (confirmed in ChatPatches source, branch `omnivers`, 26.2-fabric build)
ChatPatches injects at HEAD of `ChatComponent.clearMessages` (`obro1961.chatpatches.mixin.gui.ChatComponentMixin#clear`, cancellable).
- Default config `vanillaClearing = false` → ChatPatches **cancels `clearMessages(true)` WITHOUT clearing anything** (its custom clear only runs for `clearMessages(false)`).
- The old `switchFilterAndRebuild` relied on `chatComponent.clearMessages(true)` to wipe the chat before re-adding the filtered history via reflection into the private 4-arg `addMessage`.
- With ChatPatches installed the clear was a **no-op** → re-added messages stacked on top of the existing ones → **duplicates + unremoved messages**. Exactly the reported symptom.

## Key 26.2 Vanilla Facts (verified via `javap` on `minecraft-merged-deobf-26.2.jar`)
- `ChatComponent.visibleMessageFilter` is **not just a render-time filter**:
  - Private 4-arg `addMessage` checks it **at insertion time** (bytecode offsets 27–38): `if (visibleMessageFilter.test(guiMessage)) { logChatMessage; addMessageToDisplayQueue; addMessageToQueue; }` — a message failing the predicate is **dropped entirely** (never enters `allMessages`, the display, or the log).
  - `refreshTrimmedMessages()` rebuilds `trimmedMessages` from `allMessages` applying the same predicate.
  - `setVisibleMessageFilter(Predicate)` sets the field then calls `refreshTrimmedMessages()`.
  - **Default is an always-true lambda; null is NOT safe** — both call sites do `visibleMessageFilter.test(...)` with no null check → NPE. For "ALL" you must set a non-null always-true (or delegating) predicate, never `null`.
- `clearMessages(boolean)` does **not** touch `visibleMessageFilter` (ChatPatches cancels it; that's what broke the old design).
- `Hud.chat` is a `final` field created once in the `Hud` constructor → the `ChatComponent` instance (and any predicate we install on it) **persists across worlds**. Only its contents are cleared per world.
- `GuiMessage` is a public record (`(int addedTime, Component content, MessageSignature, GuiMessageSource, GuiMessageTag)`); `GuiMessage.Line` is a public record; `ChatComponent$DelayedMessageDeletion` is **package-private** (hence the raw-`List` accessor for `messageDeletionQueue` — Mixin selects target fields by name + erased descriptor, so raw generics match fine).
- `resetChatScroll()` is public (resets `chatScrollbarPos` + `newMessageSinceScroll`).
- Fabric API `0.157.0+26.2`: the old unified `ClientConnectionEvents` **no longer exists** — use `net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT` (callback: `onPlayDisconnect(ClientPacketListener, Minecraft)`).

## New Design (implemented)
- **Fabric handlers** (`ALLOW_CHAT` / `ALLOW_GAME` non-overlay): call `ChatFilter.track(message)` and **always return `true`** — no packet dropping, so ChatPatches chatlog/search keep full history.
- **`ChatFilter`**: single master history deque (`allMessages`, cap 16 384 = ChatPatches' `chatMaxMessages` default). `track` = store once per message; `shouldShow(Component)` = pure check; `getFilteredMessagesArray()` = current filter view, oldest-first. Cleared on `ClientPlayConnectionEvents.DISCONNECT` (fresh history per world; flags + predicate persist).
- **Live gating = vanilla predicate**: `chatComponent.setVisibleMessageFilter(msg -> ChatFilter.shouldShow(msg.content()))`, installed lazily from the client tick handler (tracks the `ChatComponent` instance; re-installs if the instance ever changes). Because the 26.2 predicate also drops messages at insertion time, non-matching messages while a filter is active live only in our master deque — they are restored on the next filter switch.
- **Filter switch** (`applyFilter`): on the main thread, wipe the display state **directly via `ChatComponentAccessor`** (`allMessages.clear()`, `trimmedMessages.clear()`, `messageDeletionQueue.clear()` — bypasses the ChatPatches-cancelled `clearMessages`), then re-add `getFilteredMessagesArray()` oldest-first via reflection into the private 4-arg `addMessage` (null signature/source/tag, as before), then `resetChatScroll()`. No `clearMessages`, no duplicates, works with or without ChatPatches.
- **Old HEAD `ChatComponentMixin` (addMessage cancel) removed** — the vanilla predicate makes it redundant (and it also gates every `addMessage` entry point, incl. ChatLog restores and system messages).
- `ChatComponentAccessor` (accessor mixin, replaces the old filter mixin): getters for `allMessages` (`List<GuiMessage>`), `trimmedMessages` (`List<GuiMessage.Line>`), `messageDeletionQueue` (raw `List` — target type is package-private).

## Test matrix (STILL PENDING)
vanilla + ChatPatches × (ALL→P→G→ALL, repeated messages vs condenser, world rejoin / ChatLog restore, F3+D, chat log & search still functional, scroll position after toggle, message-deletion markers on signed servers).

## Project / Build
- Dir: `D:\Documents\Skyblock Mods Github\RoxarexMods`
- MC 26.2 · Fabric Loader 0.19.3 · Loom 1.17-SNAPSHOT · Fabric API 0.157.0+26.2 · `release 25` (Java 25) · mixin `compatibilityLevel: JAVA_21`
- Compile: `.\gradlew.bat compileJava` (last run after new design: BUILD SUCCESSFUL)
- Filter files: `src\main\java\net\roxarex\chat\{ChatFilter,WidgetManager,WidgetsInitialization,BaseWidget,SimpleActionWidget,InfoWidget}.java`; accessor mixin: `src\main\java\net\roxarex\mixin\ChatComponentAccessor.java`; config: `src\main\resources\roxarexmods.mixins.json`

## References
- ChatPatches: https://github.com/mrbuilder1961/ChatPatches (default branch `omnivers`, multi-version; 26.2 variant in `versions/26.2-fabric`)
  - `src/main/java/obro1961/chatpatches/mixin/gui/ChatComponentMixin.java` — `clear` (clearMessages hook), `queueMessageWhileLoading`, `modifyMessage`, `moreMessages`
  - `ChatLog.java` (restore/queue/save), `ChatUtil.java` (`modifyMessage`, `tryCondenseDupes`), `config/Config.java` (defaults: `vanillaClearing=false`, `counter=true`, `chatlog=true`, `time=true`, `name=true`, `boundary=true`, `chatMaxMessages=16384`)
- 26.2 `ChatComponent` disassembly: `javap -c -p -cp .../minecraft-merged-deobf-26.2.jar net.minecraft.client.gui.components.ChatComponent`
  - Private 4-arg `addMessage`: predicate gate at insertion → `logChatMessage` / `addMessageToDisplayQueue` / `addMessageToQueue` (allMessages capped at 100, raised to 16384 by ChatPatches).
  - `refreshTrimmedMessages`: `trimmedMessages.clear(); for (GuiMessage m : Lists.reverse(allMessages)) if (visibleMessageFilter.test(m)) addMessageToDisplayQueue(m);`
  - `clearMessages(boolean)`: flush chat queue, clear `messageDeletionQueue`/`trimmedMessages`/`allMessages` (+ `recentChat` if true) — does not touch the predicate.
  - Constructor: `visibleMessageFilter = <lambda> iconst_1; ireturn` (always-true default).
