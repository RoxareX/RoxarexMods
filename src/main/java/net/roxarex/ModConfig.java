package net.roxarex;

import java.nio.file.Path;
import java.time.LocalDateTime;

import net.azureaaron.dandelion.api.ConfigManager;
import net.azureaaron.dandelion.api.ConfigType;
import net.fabricmc.loader.api.FabricLoader;
import net.roxarex.utils.LocalDateTimeAdapter;

public class ModConfig {

	public static ModConfig LIVE;
	private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
	private static final Path CONFIG_FILE = CONFIG_DIR.resolve("roxarexmods.json");
	public static final ConfigManager<ModConfig> HANDLER = ConfigManager.create(
			ModConfig.class,
			CONFIG_FILE,
			builder -> builder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
	);

	// ===== GENERAL SETTINGS =====
	public boolean enabled = true;
	public boolean debugMode = false;
	public int updateFrequency = 1000;

	// ===== RIFT SETTINGS =====
	public boolean showTimers = true;
	public int alertVolume = 80;
	public float alertPitch = 1.0f;
	public int UbixCooldownUIX = 1;
	public int UbixCooldownUIY = 1;
	public LocalDateTime UbixNextAvailable =  LocalDateTime.now();

	// ===== CONFIG UI =====
	public ConfigType configBackend = ConfigType.MOUL_CONFIG;

	public static ModConfig get() {
		return HANDLER.instance();
	}
}
