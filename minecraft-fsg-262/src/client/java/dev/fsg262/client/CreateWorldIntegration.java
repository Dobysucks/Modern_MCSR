package dev.fsg262.client;

import dev.fsg262.client.world.WorldCreationController;
import dev.fsg262.search.SeedSearchProgress;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;
import dev.fsg262.mixin.FsgScreenAccess;

public final class CreateWorldIntegration {
    private static final WorldCreationController CONTROLLER = WorldCreationController.instance();

    private CreateWorldIntegration() {}

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (screen instanceof CreateWorldScreen createWorld) {
                addIndicator(client, createWorld);
            }
        });
    }

    private static void addIndicator(Minecraft client, CreateWorldScreen screen) {
        var access = (FsgScreenAccess) screen;
        var indicator = Button.builder(Component.literal(statusLabel()), button ->
                client.setScreenAndShow(new FsgScreen(screen)))
                .bounds(screen.width / 2 - 100, 45, 200, 20).build();
        access.fsg262$addWidget(indicator);
    }

    private static String statusLabel() {
        var settings = CONTROLLER.settings();
        return "MCSR: " + (settings.enabled() ? CONTROLLER.status() : "Disabled");
    }

    static void startSearch(CreateWorldScreen screen, Minecraft client, String[] status) {
        CONTROLLER.startSearch(screen,
                progress -> client.execute(() -> status[0] = progressLabel(progress)),
                message -> client.execute(() -> status[0] = message));
    }

    private static String progressLabel(SeedSearchProgress progress) {
        return "Tested " + progress.tested() + " (" + progress.currentStage() + ")";
    }
}
