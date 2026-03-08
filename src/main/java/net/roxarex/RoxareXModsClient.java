package net.roxarex;

import net.azureaaron.dandelion_bp.api.DandelionConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class RoxareXModsClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {}

	public static Screen createConfigScreen(@Nullable Screen parentScreen) {
		return DandelionConfigScreen.create(
			ModConfig.HANDLER,
			(defaults, config, builder) -> builder
				.title(Component.literal("RoxareXMods Configuration"))
				.category(GeneralConfigCategory.create(defaults, config))
				.category(RiftConfigCategory.create(defaults, config))
		).generateScreen(parentScreen, ModConfig.get().configBackend);
	}
}

