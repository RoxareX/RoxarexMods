package net.roxarex.skyblock.rift;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import net.minecraft.resources.Identifier;
import net.roxarex.ConfigManager;
import net.roxarex.ModConfig;
import net.roxarex.injected.SkyblockerStack;
import org.joml.Matrix3x2fStack;

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
        System.out.println("UbixNextAvailable: " + ModConfig.LIVE.UbixNextAvailable);

        // Register the UseItemCallback to listen for item usage
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!world.isClientSide()) return InteractionResult.PASS;
            if (player.getItemInHand(hand).getItem() == Items.PLAYER_HEAD) {
                try {
                    ItemStack stack = player.getItemInHand(hand);
                    if ("UBIKS_CUBE".equals(((SkyblockerStack) (Object) stack).getNeuName())) {
                        ubixUsed = true;
                        System.out.println("UBIKS_CUBE USED!!!");
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
                    System.out.println("UBIX Cooldown Started!");
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
                    System.out.println("UBIX SPLIT! message loaded and saved!");
                }
            }
        });

        // Register HUD element to render the cooldown (uses same system as DungeonScoreHUD)
        HudElementRegistry.attachElementAfter(VanillaHudElements.OVERLAY_MESSAGE, RIFT_UBIX_COOLDOWN, (context, tickCounter) -> render(context));
    }

    private static void render(GuiGraphics context) {
        if (!ModConfig.get().showTimers) return;
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        if (!(Utils.isOnSkyblock() && Utils.isOnHypixel())) return;

        var remainingTimeInDate = Duration.between(LocalDateTime.now(),ModConfig.LIVE.UbixNextAvailable).getSeconds();
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
				client.player.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, (float) ModConfig.get().alertVolume, ModConfig.get().alertPitch);
				client.player.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 80f, 0.1f);
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
        // Draw item using NEU repository (Ubiks Cube NEU id = "UBIKS_CUBE")
        ItemStack ubixIcon = ItemRepository.getItemStack("UBIKS_CUBE");
        if (ubixIcon == null) ubixIcon = new ItemStack(Items.PLAYER_HEAD); // fallback
        // Use GuiGraphics rendering helpers (works across mappings)
        try {
            context.renderItem(ubixIcon, x, y);
            // Decorations (overlay/counts) - some mappings accept a format string, others don't; prefer the 4-arg variant when available
            try {
                context.renderItemDecorations(client.font, ubixIcon, x, y);
            } catch (Throwable t) {
                // Some versions have a 5-arg signature; try that as fallback
                try {
                    context.renderItemDecorations(client.font, ubixIcon, x, y, "Something Went Wrong");
                } catch (Throwable ignored) {
                    // ignore and fall back to text below
                }
            }
        } catch (Throwable ignored) {
            // Rendering failed; fall back to a simple text marker
            context.drawString(client.font, net.minecraft.network.chat.Component.literal("[Ubix]"), x, y, 0xFFFFFF00);
        }

        // Scale text slightly if desired
        Matrix3x2fStack matrix = context.pose();
        matrix.pushMatrix();
//        keep scale 1.0 for crispness, adjust if needed:
//        matrix.scale(ModConfig.get().UbixCooldownUIScale, ModConfig.get().UbixCooldownUIScale);
        context.drawString(client.font, net.minecraft.network.chat.Component.literal(message), textX, textY, 0xFFFFFFFF);
        matrix.popMatrix();
    }

    public static long getCurrentRealTimeMillis() {
        return System.currentTimeMillis();
    }

}
