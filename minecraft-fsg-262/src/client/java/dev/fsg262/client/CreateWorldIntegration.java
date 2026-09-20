package dev.fsg262.client;

import dev.fsg262.client.world.WorldCreationController;
import dev.fsg262.client.world.WorldCreationSettings;
import dev.fsg262.filter.SeedTypeChoice;
import dev.fsg262.mixin.FsgScreenAccess;
import dev.fsg262.search.CandidateEvaluation;
import dev.fsg262.search.SearchDecision;
import dev.fsg262.search.SeedSearchProgress;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;

public final class CreateWorldIntegration {
    private static final WorldCreationController CONTROLLER = WorldCreationController.instance();
    private static final String[] PROFILES = {"STRICT", "BALANCED", "COMPLETABLE"};
    private static final SeedTypeChoice[] SEED_TYPES = {
            SeedTypeChoice.VILLAGE, SeedTypeChoice.SHIPWRECK,
            SeedTypeChoice.DESERT_TEMPLE, SeedTypeChoice.RUINED_PORTAL,
            SeedTypeChoice.BURIED_TREASURE
    };

    private CreateWorldIntegration() {}

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (screen instanceof CreateWorldScreen createWorld) {
                addControls(client, createWorld);
                ScreenEvents.remove(screen).register(removed -> CONTROLLER.cancel());
            }
        });
    }

    private static void addControls(Minecraft client, CreateWorldScreen screen) {
        var access = (FsgScreenAccess) screen;
        int x = screen.width / 2 - 100;
        int y = 45;
        var seedType = Button.builder(Component.literal(seedTypeLabel()), button -> {
            var current = CONTROLLER.settings();
            CONTROLLER.updateSettings(new WorldCreationSettings(current.enabled(),
                    nextSeedType(current.seedType()), current.profileName(), current.completable(),
                    current.standardizedRng(), current.customRngSeed(), current.backgroundFiltering()));
            button.setMessage(Component.literal(seedTypeLabel()));
        }).bounds(x, y, 200, 20).build();
        access.fsg262$addWidget(seedType);

        var profile = Button.builder(Component.literal(profileLabel()), button -> {
            var current = CONTROLLER.settings();
            CONTROLLER.updateSettings(new WorldCreationSettings(current.enabled(),
                    current.seedType(), nextProfile(current.profileName()), current.completable(),
                    current.standardizedRng(), current.customRngSeed(), current.backgroundFiltering()));
            button.setMessage(Component.literal(profileLabel()));
        }).bounds(x, y + 22, 200, 20).build();
        access.fsg262$addWidget(profile);

        var completable = Button.builder(Component.literal(completableLabel()), button -> {
            var current = CONTROLLER.settings();
            CONTROLLER.updateSettings(new WorldCreationSettings(current.enabled(), current.seedType(),
                    current.profileName(), !current.completable(), current.standardizedRng(),
                    current.customRngSeed(), current.backgroundFiltering()));
            button.setMessage(Component.literal(completableLabel()));
        }).bounds(x, y + 44, 200, 20).build();
        access.fsg262$addWidget(completable);

        var rng = Button.builder(Component.literal(rngLabel()), button -> {
            var current = CONTROLLER.settings();
            CONTROLLER.updateSettings(new WorldCreationSettings(current.enabled(), current.seedType(),
                    current.profileName(), current.completable(), !current.standardizedRng(),
                    current.customRngSeed(), current.backgroundFiltering()));
            button.setMessage(Component.literal(rngLabel()));
        }).bounds(x, y + 66, 200, 20).build();
        access.fsg262$addWidget(rng);

        var enabled = Button.builder(Component.literal(enabledLabel()), button -> {
            var current = CONTROLLER.settings();
            CONTROLLER.updateSettings(new WorldCreationSettings(!current.enabled(), current.seedType(),
                    current.profileName(), current.completable(), current.standardizedRng(),
                    current.customRngSeed(), current.backgroundFiltering()));
            button.setMessage(Component.literal(enabledLabel()));
        }).bounds(x, y + 88, 200, 20).build();
        access.fsg262$addWidget(enabled);

        var search = Button.builder(Component.literal("FSG: FIND ACCEPTED SEED"), button -> {
            CreateWorldIntegration.startSearch(screen, client);
        }).bounds(x, y + 110, 200, 20).build();
        access.fsg262$addWidget(search);

        var cancel = Button.builder(Component.literal("FSG: CANCEL SEARCH"), button -> CONTROLLER.cancel())
                .bounds(x, y + 132, 200, 20).build();
        access.fsg262$addWidget(cancel);
    }

    private static void startSearch(CreateWorldScreen screen, Minecraft client) {
        CONTROLLER.startSearch(screen, (candidate, request) ->
                        CandidateEvaluation.notVerified(
                                "World-generation adapter is NOT VERIFIED for Minecraft 26.2"),
                progress -> client.execute(() -> logProgress(progress)),
                message -> client.execute(() -> System.out.println("[FSG] " + message)));
    }

    private static void logProgress(SeedSearchProgress progress) {
        // Progress is intentionally kept off the render state until a native
        // status widget is added; the search remains cancellable and safe.
    }

    private static SeedTypeChoice nextSeedType(SeedTypeChoice current) {
        for (int i = 0; i < SEED_TYPES.length; i++) {
            if (SEED_TYPES[i] == current) return SEED_TYPES[(i + 1) % SEED_TYPES.length];
        }
        return SEED_TYPES[0];
    }

    private static String nextProfile(String current) {
        for (int i = 0; i < PROFILES.length; i++) {
            if (PROFILES[i].equals(current)) return PROFILES[(i + 1) % PROFILES.length];
        }
        return PROFILES[0];
    }

    private static String seedTypeLabel() {
        return "Seed Type: " + CONTROLLER.settings().seedType().name();
    }

    private static String profileLabel() {
        return "Profile: " + CONTROLLER.settings().profileName();
    }

    private static String completableLabel() {
        return "Completable: " + (CONTROLLER.settings().completable() ? "ON" : "OFF");
    }

    private static String rngLabel() {
        return "Standardized RNG: " + (CONTROLLER.settings().standardizedRng() ? "ON" : "OFF");
    }

    private static String enabledLabel() {
        return "MCSR/FSG: " + (CONTROLLER.settings().enabled() ? "ON" : "OFF");
    }
}
