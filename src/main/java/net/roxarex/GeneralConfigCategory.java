package net.roxarex;

import net.azureaaron.dandelion_bp.api.ConfigCategory;
import net.azureaaron.dandelion_bp.api.Option;
import net.minecraft.network.chat.Component;

public class GeneralConfigCategory {

	public static ConfigCategory create(ModConfig defaults, ModConfig config) {
		return ConfigCategory.createBuilder()
				.id(RoxareXMods.id("config/general"))
				.name(Component.literal("General"))

				.option(Option.<Boolean>createBuilder()
						.name(Component.literal("Enabled"))
						.description(Component.literal("Enable the mod"))
						.binding(defaults.enabled,
								() -> config.enabled,
								newValue -> config.enabled = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())

				.option(Option.<Boolean>createBuilder()
						.name(Component.literal("Debug Mode"))
						.description(Component.literal("Enable debug messages in chat"))
						.binding(defaults.debugMode,
								() -> config.debugMode,
								newValue -> config.debugMode = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())

				.option(Option.<Integer>createBuilder()
						.name(Component.literal("Update Frequency"))
						.description(Component.literal("How often to update (milliseconds)"))
						.binding(defaults.updateFrequency,
								() -> config.updateFrequency,
								newValue -> config.updateFrequency = newValue)
						.controller(ConfigUtils.createIntegerSliderController(100, 5000))
						.build())

				.build();
	}
}
