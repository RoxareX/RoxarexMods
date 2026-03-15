package net.roxarex;

import net.azureaaron.dandelion_bp.api.DandelionConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class RoxareXModsClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(ClientCommandManager.literal("roxarexmods")
					.executes(context -> {
						Minecraft client = Minecraft.getInstance();
						new Thread(() -> {
							try {
								Thread.sleep(10); // wait 10ms for chat to fully close
							} catch (InterruptedException ignored) {}
							client.execute(() -> client.setScreen(createConfigScreen(null)));
						}).start();
						return 1;
					})
			);
		});
	}

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

