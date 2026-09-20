package dev.fsg262;

import net.fabricmc.api.ClientModInitializer;
import dev.fsg262.client.CreateWorldIntegration;

public final class FsgClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CreateWorldIntegration.register();
    }
}
