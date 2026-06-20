package net.roxarex;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.azureaaron.dandelion.api.DandelionConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.roxarex.chat.WidgetsInitialization;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;
public class RoxareXModsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        WidgetsInitialization.init();


        Minecraft client = Minecraft.getInstance();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    (LiteralArgumentBuilder<FabricClientCommandSource>) literal("roxarexmods")
                    .executes(context -> {
                        new Thread(() -> {
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException ignored) {}
                            client.execute(() -> client.setScreen(createConfigScreen(null)));
                        }).start();
                        return 1;
                    })
                    .then(
                            (LiteralArgumentBuilder<FabricClientCommandSource>) literal("roxarexmods")
                            .executes(context -> {
                                new Thread(() -> {
                                    try {
                                        Thread.sleep(10);
                                    } catch (InterruptedException ignored) {
                                    }
                                    client.execute(() -> client.setScreen(createConfigScreen(null)));
                                }).start();
                                return 1;
                            })
                            .then(literal("saveConfig")
                                    .executes(context -> {
                                        ModConfig.HANDLER.save();
                                        RoxareXMods.LOGGER.info("Config Saved but probably didn't do much other than reset most your settings :D");
                                        return 1;
                                    })
                            )
                            .then(literal("setUbixTime")
                                    .then(argument("hours", IntegerArgumentType.integer(0))
                                            .then(argument("minutes", IntegerArgumentType.integer(0, 59))
                                                    .then(argument("seconds", IntegerArgumentType.integer(0, 59))
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
                    )
            );
        });
    }

    public static Screen createConfigScreen(@Nullable Screen parentScreen) {
        return DandelionConfigScreen.create(
                ModConfig.HANDLER,
                (defaults, config, builder) -> {
                    ModConfig.LIVE = config; // capture Dandelion's actual instance
                    RoxareXMods.LOGGER.info("ModConfig created");
                    return builder
                            .title(Component.literal("RoxareXMods Configuration"))
                            .category(GeneralConfigCategory.create(defaults, config))
                            .category(RiftConfigCategory.create(defaults, config));
                }
        ).generateScreen(parentScreen, ModConfig.get().configBackend);
    }
}

