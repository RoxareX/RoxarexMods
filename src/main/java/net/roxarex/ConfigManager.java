package net.roxarex;

public class ConfigManager {

    public static void loadConfig() {
        try {
            ModConfig.HANDLER.load();
            RoxareXMods.LOGGER.info("Config loaded successfully!");
        } catch (Exception e) {
            RoxareXMods.LOGGER.error("Failed to load config!", e);
        }
    }

    public static void saveConfig() {
        try {
            ModConfig.HANDLER.save();
            RoxareXMods.LOGGER.info("Config saved successfully!");
        } catch (Exception e) {
            RoxareXMods.LOGGER.error("Failed to save config!", e);
        }
    }

    public static ModConfig getConfig() {
        return ModConfig.get();
    }
}
