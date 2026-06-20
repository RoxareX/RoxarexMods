package net.roxarex.skyblock.rift;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import net.minecraft.resources.Identifier;
import net.roxarex.ConfigManager;
import net.roxarex.ModConfig;
import net.roxarex.RoxareXMods;
import net.roxarex.injected.SkyblockerStack;

import java.lang.reflect.Method;
import java.time.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RiftUbixCooldown {
    private static final Identifier RIFT_UBIX_COOLDOWN = SkyblockerMod.id("rift_ubix_cooldown");
    private static String message = "";
    private static boolean playedReadySound = false;
    private static boolean initialized = false;

    private static boolean ubixUsed = false; // Track if the Ubix was used

    private static final Pattern SPLIT_COOLDOWN_PATTERN = Pattern.compile(
            "SPLIT! You need to wait (?:(\\d+)h )?(?:(\\d+)m )?(?:(\\d+)s) before you can play again\\."
    );
    private static final Pattern PLAYER_MOTES_PATTERN = Pattern.compile("You earned \\d{1,3}(,\\d{3})* Motes in this match!");
    private static final Pattern OPPONENT_MOTES_PATTERN = Pattern.compile("Your opponent earned \\d{1,3}(,\\d{3})* Motes in this match!");

    @Init
    public static void init() {
        if (initialized) return;
        initialized = true;

        ModConfig.LIVE.UbixNextAvailable = LocalDateTime.now().plusSeconds(22);

        // Register the UseItemCallback to listen for item usage
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!world.isClientSide()) return InteractionResult.PASS;
            if (player.getItemInHand(hand).getItem() == Items.PLAYER_HEAD) {
                try {
                    ItemStack stack = player.getItemInHand(hand);
                    if ("UBIKS_CUBE".equals(((SkyblockerStack) (Object) stack).getNeuName())) {
                        ubixUsed = true;

                    }
                } catch (Exception ignored) {}
            }
            return InteractionResult.PASS;
        });

        // Listen for chat messages
        ClientReceiveMessageEvents.GAME.register((message, bool) -> {
            String msg = message.getString();

            // Match result messages
            if (PLAYER_MOTES_PATTERN.matcher(msg).find() ||
                    OPPONENT_MOTES_PATTERN.matcher(msg).find()) {
                if (ubixUsed) {
                    ubixUsed = false;
                    // Save ubixUsedAt as a date and add plus 2 hours
                    ModConfig.LIVE.UbixNextAvailable = LocalDateTime.now().plusHours(2);
                    ConfigManager.saveConfig();

                }
            }

            // SPLIT! cooldown message
            Matcher matcher = SPLIT_COOLDOWN_PATTERN.matcher(msg);
            if (matcher.find()) {
                if (ubixUsed) {
                    ubixUsed = false;
                    // Get the time from chat message and later assign it to the date
                    int hours = matcher.group(1) != null ? Integer.parseInt(matcher.group(1)) : 0;
                    int minutes = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 0;
                    int seconds = matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : 0;

                    ModConfig.LIVE.UbixNextAvailable = LocalDateTime.now()
                            .plusHours(hours)
                            .plusMinutes(minutes)
                            .plusSeconds(seconds)
                            .withNano(0);
                    ConfigManager.saveConfig();

                }
            }
        });

        // Register HUD element to render the cooldown (uses same system as DungeonScoreHUD)
        HudElementRegistry.attachElementAfter(VanillaHudElements.OVERLAY_MESSAGE, RIFT_UBIX_COOLDOWN, (context, tickCounter) -> render(context));
    }

    private static void render(Object context) {
        if (!ModConfig.get().showTimers) return;
        Minecraft client = Minecraft.getInstance();


        if (client.player == null) return;
        if (!(Utils.isOnSkyblock() && Utils.isOnHypixel())) return;

        var remainingTimeInDate = Duration.between(LocalDateTime.now(), ModConfig.LIVE.UbixNextAvailable).getSeconds();
        int remainingTime = (int) remainingTimeInDate;

        if (remainingTime > 0) {
            playedReadySound = false;
            if (remainingTime >= 3600) {
                int hours = remainingTime / 3600;
                int minutes = (remainingTime % 3600) / 60;
                message = String.format("Rift Ubix: %d h %d min", hours, minutes);
            } else {
                int minutes = remainingTime / 60;
                int seconds = remainingTime % 60;
                message = String.format("Rift Ubix: %d min %d sec", minutes, seconds);
            }
        } else {
            message = "Rift Ubix: Ready";
            if (!playedReadySound && client.player != null) {
                client.gui.setTitle(Component.literal("Rift Ubik's!!!"));
                client.gui.setSubtitle(Component.literal("Hopefully this didn't block something important. :D"));
				client.player.playSound(SoundEvents.AMETHYST_BLOCK_BREAK, (float) ModConfig.get().alertVolume, ModConfig.get().alertPitch);
                playedReadySound = true;
            }
        }

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        int itemX = ModConfig.get().UbixCooldownUIX;
        int itemY = ModConfig.get().UbixCooldownUIY;

        int x = (int) (screenWidth * (itemX / 100f));
        int y = (int) (screenHeight * (itemY / 100f));

        int textX = x + 20;
        int textY = y + 6;


        // Obtain item to render
        Object ubixIconObj = ItemRepository.getItemStack("UBIKS_CUBE");
        ItemStack ubixIcon = null;
        try {
            if (ubixIconObj instanceof ItemStack) {
                ubixIcon = (ItemStack) ubixIconObj;
            } else if (ubixIconObj != null) {

                // Try common conversion methods
                String[] methods = new String[] {"toItemStack","asItemStack","getItemStack","getStack","getStackSnapshot","asItem","getItem","getMinecraftItemStack","toStack","getStackFromInstance"};
                for (String mName : methods) {
                    try {
                        java.lang.reflect.Method m = ubixIconObj.getClass().getMethod(mName);
                        Object res = m.invoke(ubixIconObj);
                        if (res instanceof ItemStack) { ubixIcon = (ItemStack) res; break; }
                        if (res instanceof Item) { ubixIcon = new ItemStack((Item) res); break; }
                        if (res instanceof String) {
                            try {
                                String sres = (String) res;
                                Identifier id = null;
                                try {
                                    // try Identifier.tryParse(String)
                                    java.lang.reflect.Method tp = Identifier.class.getMethod("tryParse", String.class);
                                    id = (Identifier) tp.invoke(null, sres);
                                } catch (NoSuchMethodException nsme) {
                                    // fallback: split ns:path
                                    if (sres.contains(":")) {
                                        String[] parts = sres.split(":", 2);
                                        id = Identifier.fromNamespaceAndPath(parts[0], parts[1]);
                                    } else {
                                        id = Identifier.fromNamespaceAndPath("minecraft", sres);
                                    }
                                }

                                if (id != null) {
                                    // reflectively find the ITEM registry (Registries.ITEM or Registry.ITEM)
                                    Object itemRegistry = null;
                                    try {
                                        Class<?> regs = Class.forName("net.minecraft.core.registries.Registries");
                                        java.lang.reflect.Field fld = regs.getField("ITEM"); itemRegistry = fld.get(null);
                                    } catch (Throwable t1) {
                                        try {
                                            Class<?> regc = Class.forName("net.minecraft.core.Registry");
                                            java.lang.reflect.Field fld2 = regc.getField("ITEM"); itemRegistry = fld2.get(null);
                                        } catch (Throwable t2) { itemRegistry = null; }
                                    }

                                    if (itemRegistry != null) {
                                        try {
                                            java.lang.reflect.Method getm = itemRegistry.getClass().getMethod("get", Identifier.class);
                                            Object it = getm.invoke(itemRegistry, id);
                                            if (it instanceof Item) { ubixIcon = new ItemStack((Item) it); break; }
                                        } catch (Throwable idEx) { /* ignore */ }
                                    }
                                }
                            } catch (Throwable idEx) {}
                        }
                    } catch (Throwable ex) { /* ignore */ }
                }

                // Try common fields
                if (ubixIcon == null) {
                    String[] fields = new String[] {"itemStack","stack","item","minecraftStack"};
                    for (String fName : fields) {
                        try {
                            java.lang.reflect.Field f = ubixIconObj.getClass().getDeclaredField(fName);
                            f.setAccessible(true);
                            Object res = f.get(ubixIconObj);
                            if (res instanceof ItemStack) { ubixIcon = (ItemStack) res; break; }
                            if (res instanceof Item) { ubixIcon = new ItemStack((Item) res); break; }
                            if (res instanceof String sres) {
                                try {
                                    Identifier id = null;
                                    try {
                                        java.lang.reflect.Method tp = Identifier.class.getMethod("tryParse", String.class);
                                        id = (Identifier) tp.invoke(null, sres);
                                    } catch (NoSuchMethodException nsme) {
                                        if (sres.contains(":")) { String[] parts = sres.split(":", 2); id = Identifier.fromNamespaceAndPath(parts[0], parts[1]); }
                                        else id = Identifier.fromNamespaceAndPath("minecraft", sres);
                                    }
                                    if (id != null) {
                                        Object itemRegistry = null;
                                        try {
                                            Class<?> regs = Class.forName("net.minecraft.core.registries.Registries");
                                            java.lang.reflect.Field fld = regs.getField("ITEM"); itemRegistry = fld.get(null);
                                        } catch (Throwable t1) {
                                            try { Class<?> regc = Class.forName("net.minecraft.core.Registry"); java.lang.reflect.Field fld2 = regc.getField("ITEM"); itemRegistry = fld2.get(null); } catch (Throwable t2) { itemRegistry = null; }
                                        }
                                        if (itemRegistry != null) {
                                            try { java.lang.reflect.Method getm = itemRegistry.getClass().getMethod("get", Identifier.class); Object it = getm.invoke(itemRegistry, id); if (it instanceof Item) { ubixIcon = new ItemStack((Item) it); break; } } catch (Throwable idEx) {}
                                        }
                                    }
                                } catch (Throwable idEx) {}
                            }
                        } catch (Throwable ex) { /* ignore */ }
                    }
                }

                // If ubixIcon is still null, try resolving from known ids
                if (ubixIcon == null) {
                    try {
                        Identifier guess = Identifier.fromNamespaceAndPath("skyblocker", "ubiks_cube");
                        Object itemRegistry = null;
                        try { Class<?> regs = Class.forName("net.minecraft.core.registries.Registries"); java.lang.reflect.Field fld = regs.getField("ITEM"); itemRegistry = fld.get(null); } catch (Throwable t1) { try { Class<?> regc = Class.forName("net.minecraft.core.Registry"); java.lang.reflect.Field fld2 = regc.getField("ITEM"); itemRegistry = fld2.get(null); } catch (Throwable t2) { itemRegistry = null; } }
                        if (itemRegistry != null) { try { java.lang.reflect.Method getm = itemRegistry.getClass().getMethod("get", Identifier.class); Object it = getm.invoke(itemRegistry, guess); if (it instanceof Item) { ubixIcon = new ItemStack((Item) it); } } catch (Throwable ignored) {} }
                    } catch (Throwable ignored) {}
                }
            }
        } catch (Throwable ignored) {}

        if (ubixIcon == null) ubixIcon = new ItemStack(Items.PLAYER_HEAD);


        Object fontObj = getFont(client);

        // Prefer using GuiGraphics if provided by the HUD system
        boolean drawn = false;


        if (context != null) {
            Class<?> ctxClass = context.getClass();
            try {
                // First pass: explicitly invoke item/itemDecorations if available so item draws before text
                for (Method m : ctxClass.getMethods()) {
                    if (!m.getName().equals("item") && !m.getName().equals("itemDecorations") && !m.getName().startsWith("handler$bbe000")) continue;

                    String name = m.getName();
                    Class<?>[] p = m.getParameterTypes();
                    try {
                        if (name.equals("item")) {
                            if (p.length == 3 && p[0].getName().contains("ItemStack") && p[1] == int.class && p[2] == int.class) {

                                m.invoke(context, ubixIcon, x, y);
                                drawn = true; break;
                            } else if (p.length == 2 && p[0].getName().contains("ItemStack") && p[1] == int.class) {

                                m.invoke(context, ubixIcon, x);
                                drawn = true; break;
                            }
                        }

                        if (name.equals("itemDecorations") || name.startsWith("handler$bbe000")) {
                            if (p.length >= 4 && p[0].getName().contains("Font") && p[1].getName().contains("ItemStack")) {

                                if (p.length == 4) m.invoke(context, fontObj, ubixIcon, x, y);
                                else if (p.length == 5) m.invoke(context, fontObj, ubixIcon, x, y, "");
                                drawn = true; break;
                            }
                        }
                    } catch (Throwable ignored) {}
                }

                // Second pass: other methods (text, centeredText, etc.)
                for (Method m : ctxClass.getMethods()) {

                    String name = m.getName();

                    // item(ItemStack, int, int) or item(ItemStack, int)
                    if (name.equals("item")) {
                        Class<?>[] p = m.getParameterTypes();
                        try {
                            if (p.length == 3 && p[0].getName().contains("ItemStack") && p[1] == int.class && p[2] == int.class) {

                                m.invoke(context, ubixIcon, x, y);
                                drawn = true; break;
                            } else if (p.length == 2 && p[0].getName().contains("ItemStack") && p[1] == int.class) {

                                m.invoke(context, ubixIcon, x);
                                drawn = true; break;
                            }
                        } catch (Throwable ignored) {}
                    }

                    // itemDecorations(Font, ItemStack, int, int)
                    if (name.equals("itemDecorations") || name.equals("handler$bbe000$fabric-rendering-v1$drawStackOverlay") ) {
                        Class<?>[] p = m.getParameterTypes();
                        try {
                            if (p.length >= 4 && p[0].getName().contains("Font") && p[1].getName().contains("ItemStack")) {

                                if (p.length == 4) m.invoke(context, fontObj, ubixIcon, x, y);
                                else if (p.length == 5) m.invoke(context, fontObj, ubixIcon, x, y, "");
                                drawn = true; break;
                            }
                        } catch (Throwable ignored) {}
                    }

                    // text(Font, Component, int, int, int) or text(Font, String, int, int, int)
                    if (name.equals("text") || name.equals("centeredText") || name.equals("textWithBackdrop")) {
                        Class<?>[] p = m.getParameterTypes();
                        try {
                            // Prefer exact 5-arg overload with Component (Font, Component, int, int, int)
                            if (p.length == 5 && p[0].getName().contains("Font") && p[1].getName().contains("Component")) {

                                m.invoke(context, fontObj, net.minecraft.network.chat.Component.literal(message), textX, textY, 0xFFFFFFFF);
                                drawn = true; break;
                            }

                            // Fallback: String-based overload (Font, String, int, int, int)
                            if (p.length == 5 && p[0].getName().contains("Font") && p[1] == String.class) {

                                m.invoke(context, fontObj, message, textX, textY, 0xFFFFFFFF);
                                drawn = true; break;
                            }

                            // Skip other overloads (e.g., FormattedCharSequence variants) to avoid wrong-arg exceptions
                        } catch (Throwable t) { RoxareXMods.LOGGER.warn("[RiftHUD] context.text invocation failed: " + m, t); }
                    }
                }
            } catch (Throwable ignored) {}
        }

        if (!drawn) {
            try {

                Object itemRenderer = null;
                try {
                    java.lang.reflect.Method gm = client.getClass().getMethod("getItemRenderer"); itemRenderer = gm.invoke(client);
                } catch (Throwable e) {
                    try { java.lang.reflect.Field f = client.getClass().getDeclaredField("itemRenderer"); f.setAccessible(true); itemRenderer = f.get(client); } catch (Throwable ignored) { itemRenderer = null; }
                }


                if (itemRenderer != null) {
                    // render item with various possible signatures
                    boolean renderedItem = false;
                    for (Method mm : itemRenderer.getClass().getMethods()) {

                        if (!mm.getName().toLowerCase().contains("render") ) continue;
                        Class<?>[] pp = mm.getParameterTypes();
                        if (pp.length >= 3 && pp[0].getName().contains("ItemStack")) {
                            Object[] args = new Object[pp.length];
                            args[0] = ubixIcon;
                            if (pp.length >= 3) { args[pp.length-2] = x; args[pp.length-1] = y; }
                            try { mm.invoke(itemRenderer, args); renderedItem = true; break; } catch (Throwable t) { }
                        }
                    }

                    // decorations
                    for (Method mm : itemRenderer.getClass().getMethods()) {
                        if (!mm.getName().equals("renderGuiItemDecorations") && !mm.getName().contains("Decorations")) continue;
                        try {
                            Object[] args = new Object[mm.getParameterCount()];
                            if (mm.getParameterCount() >= 4) {
                                args[0] = getFont(client); args[1] = ubixIcon; args[mm.getParameterCount()-2] = x; args[mm.getParameterCount()-1] = y;
                            }
                            mm.invoke(itemRenderer, args); break;
                        } catch (Throwable t) { RoxareXMods.LOGGER.warn("[RiftHUD] itemRenderer decorations failed: " + mm, t); }
                    }
                }
            } catch (Throwable t) { RoxareXMods.LOGGER.warn("[RiftHUD] fallback path failed", t); }

            // draw text via font
            try { drawText(client, getFont(client), message, textX, textY, 0xFFFFFFFF); } catch (Throwable t) { }
        }
    }

    private static void drawText(Minecraft mc, Object fontObj, String text, int x, int y, int color) {
        if (fontObj == null) return;
        try {
            Method candidate = null;
            for (Method m : fontObj.getClass().getMethods()) {
                if (!m.getName().equals("draw")) continue;
                Class<?>[] p = m.getParameterTypes();
                if (p.length == 4 && (p[0] == String.class || p[0].getName().contains("Component") || p[0].getName().contains("CharSequence"))) {
                    candidate = m; break;
                }
                if (p.length == 5) { // (MatrixStack, Component, float, float, int)
                    candidate = m; break;
                }
            }
            if (candidate != null) {
                Class<?>[] p = candidate.getParameterTypes();
                Object[] args = new Object[p.length];
                if (p.length == 4) {
                    if (p[0] == String.class) {
                        args[0] = text; args[1] = (float) x; args[2] = (float) y; args[3] = color;
                    } else {
                        args[0] = net.minecraft.network.chat.Component.literal(text); args[1] = (float) x; args[2] = (float) y; args[3] = color;
                    }
                } else if (p.length == 5) {
                    args[0] = null; args[1] = net.minecraft.network.chat.Component.literal(text); args[2] = (float) x; args[3] = (float) y; args[4] = color;
                }
                candidate.invoke(fontObj, args);
                return;
            }
        } catch (Throwable ignored) {}
    }

    private static Object getFont(Minecraft mc) {
        try { return mc.font; } catch (Throwable ignored) {}
        try { java.lang.reflect.Method m = mc.getClass().getMethod("getFont"); return m.invoke(mc); } catch (Throwable ignored) {}
        return null;
    }

    public static long getCurrentRealTimeMillis() {
        return System.currentTimeMillis();
    }

}
