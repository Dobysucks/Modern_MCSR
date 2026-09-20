package dev.fsg262;

import net.fabricmc.api.ClientModInitializer;
import dev.fsg262.client.CreateWorldIntegration;
import dev.fsg262.client.FsgScreen;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screens.TitleScreen;

public final class FsgClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CreateWorldIntegration.register();
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (screen instanceof TitleScreen) {
                var access = (dev.fsg262.mixin.FsgScreenAccess) screen;
                access.fsg262$addWidget(FsgScreen.openButton(
                        width / 2 - 100, height / 4 + 96,
                        () -> client.setScreenAndShow(new FsgScreen(screen))));
            }
        });
    }
}
