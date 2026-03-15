package net.roxarex;

import java.nio.file.Path;
import java.util.function.UnaryOperator;

import net.azureaaron.dandelion_bp.api.ConfigManager;
import net.azureaaron.dandelion_bp.api.ConfigType;
import net.fabricmc.loader.api.FabricLoader;

public class ModConfig {

	private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
	private static final Path CONFIG_FILE = CONFIG_DIR.resolve("roxarexmods.json");
	public static final ConfigManager<ModConfig> HANDLER = ConfigManager.create(ModConfig.class, CONFIG_FILE, UnaryOperator.identity());

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
	public float UbixCooldownUIScale = 1.0f;
	public int highlightColor = 0xFF00FF;

	// ===== CONFIG UI =====
	public ConfigType configBackend = ConfigType.MOUL_CONFIG;

	public static ModConfig get() {
		return HANDLER.instance();
	}
}
