package net.roxarex;

import dev.isxander.yacl3.api.controller.ControllerBuilder;
import net.azureaaron.dandelion_bp.api.ConfigCategory;
import net.azureaaron.dandelion_bp.api.Option;
import net.azureaaron.dandelion_bp.api.controllers.FloatController;
import net.azureaaron.dandelion_bp.api.controllers.IntegerController;
import net.minecraft.network.chat.Component;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.OptionDescription;

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
								newValue -> config.updateFrequency = Math.max(100, Math.min(5000, newValue)))
						.controller(IntegerController.createBuilder().range(100, 5000).slider(5).build())
						.build())

				.build();
	}
}
