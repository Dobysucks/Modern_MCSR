package dev.fsg262;

import dev.fsg262.client.FsgScreen;
import dev.fsg262.mixin.FsgScreenAccess;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screens.TitleScreen;

public final class FsgClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof TitleScreen) {
                ((FsgScreenAccess) screen).fsg262$addWidget(FsgScreen.openButton(
                        scaledWidth / 2 - 100, scaledHeight / 4 + 72,
                        () -> client.setScreenAndShow(new FsgScreen(screen))));
            }
        });
    }
}
