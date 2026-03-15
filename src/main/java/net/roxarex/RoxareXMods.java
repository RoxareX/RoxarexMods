package net.roxarex;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import net.roxarex.skyblock.rift.RiftUbixCooldown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoxareXMods implements ModInitializer {
    public static final String MOD_ID = "roxarexmods";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Hello Fabric world!");
        ConfigManager.loadConfig();
        LOGGER.info("Config loaded successfully!");


        RiftUbixCooldown.init();
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
