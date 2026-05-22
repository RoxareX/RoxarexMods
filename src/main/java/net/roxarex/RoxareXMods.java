package net.roxarex;

import net.azureaaron.dandelion.api.DandelionConfigScreen;
import net.fabricmc.api.ModInitializer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import net.roxarex.skyblock.rift.RiftUbixCooldown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoxareXMods implements ModInitializer {
    public static final String MOD_ID = "roxarexmods";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Started Loading RoxareXMods");

        // Load Config before trying to change it
        ModConfig.HANDLER.load();

        // Eagerly capture Dandelion's live instance at startup
        DandelionConfigScreen.create(
                ModConfig.HANDLER,
                (defaults, config, builder) -> {
                    ModConfig.LIVE = config;
                    return builder
                            .title(Component.literal("RoxareXMods Configuration"))
                            .category(GeneralConfigCategory.create(defaults, config))
                            .category(RiftConfigCategory.create(defaults, config));
                }
        ); // no .generateScreen() — we just want the lambda to run and capture config

        LOGGER.info("RoxarexMods Config loaded successfully!");

        // TODO: make a calendar that includes every skyblock event



        RiftUbixCooldown.init();
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
