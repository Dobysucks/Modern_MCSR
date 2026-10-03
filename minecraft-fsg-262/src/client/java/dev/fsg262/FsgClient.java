package dev.fsg262;

import net.fabricmc.api.ClientModInitializer;
import dev.fsg262.client.world.WorldCreationController;
import dev.fsg262.client.CreateWorldIntegration;
import dev.fsg262.world.McsrWorldDataAccess;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public final class FsgClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CreateWorldIntegration.register();
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (!server.isSingleplayer()) return;
            if (server.getWorldData() instanceof McsrWorldDataAccess mcsrWorld
                    && mcsrWorld.fsg262$isMcsrWorld()) {
                WorldCreationController.instance().worldCreationFinished();
            }
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (!server.isSingleplayer()) return;
            if (server.getWorldData() instanceof McsrWorldDataAccess mcsrWorld
                    && mcsrWorld.fsg262$isMcsrWorld()) {
                WorldCreationController.instance().worldCreationFailed();
            }
        });
    }
}
