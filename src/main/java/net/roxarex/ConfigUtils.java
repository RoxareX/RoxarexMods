package net.roxarex;

import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import net.azureaaron.dandelion_bp.api.ButtonOption;
import net.azureaaron.dandelion_bp.api.controllers.BooleanController;
import net.azureaaron.dandelion_bp.api.controllers.BooleanController.BooleanStyle;
import net.azureaaron.dandelion_bp.api.controllers.ColourController;
import net.azureaaron.dandelion_bp.api.controllers.EnumController;
import net.azureaaron.dandelion_bp.api.controllers.IntegerController;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;

public class ConfigUtils {
	public static final Function<ChatFormatting, Component> FORMATTING_FORMATTER = formatting -> 
		Component.literal(StringUtils.capitalize(formatting.getName().replaceAll("_", " ")));

	public static BooleanController createBooleanController() {
		return BooleanController.createBuilder()
				.coloured(true)
				.booleanStyle(BooleanStyle.YES_NO)
				.build();
	}

	public static ColourController createColourController(boolean hasAlpha) {
		return ColourController.createBuilder()
				.hasAlpha(hasAlpha)
				.build();
	}

	public static IntegerController createIntegerSliderController(int min, int max) {
		return (IntegerController) IntegerController.createBuilder()
				.min(min)
				.max(max)
				.slider(1)
				.build();
	}

	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> EnumController<T> createEnumController() {
		return (EnumController<T>) EnumController.createBuilder().build();
	}

	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> EnumController<T> createEnumController(Function<T, Component> formatter) {
		return (EnumController<T>) EnumController.createBuilder()
				.formatter(Function.class.cast(formatter))
				.build();
	}

	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> EnumController<T> createEnumDropdownController(Function<T, Component> formatter) {
		return (EnumController<T>) EnumController.createBuilder()
				.dropdown(true)
				.formatter(Function.class.cast(formatter))
				.build();
	}

	public static ButtonOption createShortcutToKeybindsScreen() {
		Minecraft client = Minecraft.getInstance();
		return ButtonOption.createBuilder()
				.name(Component.literal("Keybinds"))
				.action(screen -> client.setScreen(new KeyBindsScreen(screen, client.options)))
				.prompt(Component.literal("Open Keybinds Settings"))
				.build();
	}
}
