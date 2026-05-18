package net.roxarex;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.azureaaron.dandelion.api.DandelionConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

public class RoxareXModsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("roxarexmods")
                    .executes(context -> {
                        Minecraft client = Minecraft.getInstance();
                        new Thread(() -> {
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException ignored) {}
                            client.execute(() -> client.setScreen(createConfigScreen(null)));
                        }).start();
                        return 1;
                    })
                    .then(ClientCommandManager.literal("saveConfig")
                            .executes(context -> {
                                // your logic here
                                ModConfig.HANDLER.save();
                                RoxareXMods.LOGGER.info("Config Saved but probably didn't do much other than reset most your settings :D");
                                return 1;
                            })
                    )
                    .then(ClientCommandManager.literal("setUbixTime")
                            .then(ClientCommandManager.argument("hours", IntegerArgumentType.integer(0))
                                    .then(ClientCommandManager.argument("minutes", IntegerArgumentType.integer(0, 59))
                                            .then(ClientCommandManager.argument("seconds", IntegerArgumentType.integer(0, 59))
                                                    .executes(context -> {
                                                        int hours = IntegerArgumentType.getInteger(context, "hours");
                                                        int minutes = IntegerArgumentType.getInteger(context, "minutes");
                                                        int seconds = IntegerArgumentType.getInteger(context, "seconds");

                                                        ModConfig.LIVE.UbixNextAvailable = LocalDateTime.now()
                                                                .plusHours(hours)
                                                                .plusMinutes(minutes)
                                                                .plusSeconds(seconds)
                                                                .withNano(0);

                                                        RoxareXMods.LOGGER.info("Before save: " + ModConfig.HANDLER.instance().UbixNextAvailable);
                                                        ModConfig.HANDLER.save();
                                                        RoxareXMods.LOGGER.info("After save: " + ModConfig.HANDLER.instance().UbixNextAvailable);

                                                        return 1;
                                                    })
                                            )
                                    )
                            )
                    )
            );
        });
    }

    public static Screen createConfigScreen(@Nullable Screen parentScreen) {
        return DandelionConfigScreen.create(
                ModConfig.HANDLER,
                (defaults, config, builder) -> {
                    ModConfig.LIVE = config; // capture Dandelion's actual instance
                    return builder
                            .title(Component.literal("RoxareXMods Configuration"))
                            .category(GeneralConfigCategory.create(defaults, config))
                            .category(RiftConfigCategory.create(defaults, config));
                }
        ).generateScreen(parentScreen, ModConfig.get().configBackend);
    }
}

