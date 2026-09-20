package dev.fsg262;

import dev.fsg262.commands.SpeedrunCommands;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public final class Fsg262 implements ModInitializer {
    public static final String MOD_ID = "fsg262";

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                SpeedrunCommands.register(dispatcher));
    }
}