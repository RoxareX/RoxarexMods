package net.roxarex;

import net.azureaaron.dandelion_bp.api.ConfigCategory;
import net.azureaaron.dandelion_bp.api.Option;
import net.azureaaron.dandelion_bp.api.controllers.FloatController;
import net.azureaaron.dandelion_bp.api.controllers.IntegerController;
import net.minecraft.network.chat.Component;

public class RiftConfigCategory {

    public static ConfigCategory create(ModConfig defaults, ModConfig config) {
        return ConfigCategory.createBuilder()
                .id(RoxareXMods.id("config/rift"))
                .name(Component.literal("Rift"))

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
                        .controller(IntegerController.createBuilder().range(0, 100).build())
                        .build())

                .option(Option.<Float>createBuilder()
                        .name(Component.literal("Alert Pitch"))
                        .description(Component.literal("Volume pitch for Rift alerts (1 is default pitch higher number is higher and lower number is lower)"))
                        .binding(defaults.alertPitch,
                                () -> config.alertPitch,
                                newValue -> config.alertPitch = newValue)
                        .controller(FloatController.createBuilder().range(0.1f, 2.0f).build())
                        .build())

                // UbixCooldown UI Related
                .option(Option.<Integer>createBuilder()
                        .name(Component.literal("Ubix Cooldown UI Position X"))
                        .description(Component.literal("Sets the UI position on the X axis in %"))
                        .binding(defaults.UbixCooldownUIX,
                                () -> config.UbixCooldownUIX,
                                newValue -> config.UbixCooldownUIX = newValue)
                        .controller(IntegerController.createBuilder().range(0, 100).build())
                        .build())

                .option(Option.<Integer>createBuilder()
                        .name(Component.literal("Ubix Cooldown UI Position Y"))
                        .description(Component.literal("Sets the UI position on the Y axis in %"))
                        .binding(defaults.UbixCooldownUIY,
                                () -> config.UbixCooldownUIY,
                                newValue -> config.UbixCooldownUIY = newValue)
                        .controller(IntegerController.createBuilder().range(0, 100).build())
                        .build())

                // #Scale# not used for now
//                .option(Option.<Float>createBuilder()
//                        .name(Component.literal("Ubix Cooldown UI Scale X"))
//                        .description(Component.literal("Sets the UI scale on the X axis"))
//                        .binding(defaults.UbixCooldownUIScale,
//                                () -> config.UbixCooldownUIScale,
//                                newValue -> config.UbixCooldownUIScale = newValue)
//                        .controller(FloatController.createBuilder().range(0f, 2f).build())
//                        .build())

                .build();
    }
}
