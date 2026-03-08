package net.roxarex;

import net.azureaaron.dandelion_bp.api.ConfigCategory;
import net.azureaaron.dandelion_bp.api.Option;
import net.minecraft.network.chat.Component;

public class RiftConfigCategory {

	public static ConfigCategory create(ModConfig defaults, ModConfig config) {
		return ConfigCategory.createBuilder()
				.id(RoxareXMods.id("config/rift"))
				.name(Component.literal("Rift"))

				.option(Option.<Boolean>createBuilder()
						.name(Component.literal("Rift Alerts"))
						.description(Component.literal("Enable alerts for Rift events"))
						.binding(defaults.riftAlerts,
								() -> config.riftAlerts,
								newValue -> config.riftAlerts = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())

				.option(Option.<Boolean>createBuilder()
						.name(Component.literal("Show Timers"))
						.description(Component.literal("Display timers for Rift activities"))
						.binding(defaults.showTimers,
								() -> config.showTimers,
								newValue -> config.showTimers = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())

				.option(Option.<Integer>createBuilder()
						.name(Component.literal("Alert Volume"))
						.description(Component.literal("Volume level for Rift alerts (0-100)"))
						.binding(defaults.alertVolume,
								() -> config.alertVolume,
								newValue -> config.alertVolume = newValue)
						.controller(ConfigUtils.createIntegerSliderController(0, 100))
						.build())

				.build();
	}
}
