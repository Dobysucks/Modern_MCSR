package dev.fsg262.client;

import dev.fsg262.client.world.WorldCreationController;
import dev.fsg262.client.world.WorldCreationSettings;
import dev.fsg262.filter.FilterProfile;
import dev.fsg262.filter.SeedTypeChoice;
import dev.fsg262.mixin.CreateWorldScreenAccess;
import dev.fsg262.mixin.FsgScreenAccess;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.components.tabs.MenuTabBar;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;

public final class CreateWorldIntegration {
    private static final WorldCreationController CONTROLLER = WorldCreationController.instance();

    private CreateWorldIntegration() {}

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (screen instanceof CreateWorldScreen createWorld) addMcsrTab(createWorld);
        });
    }

    private static void addMcsrTab(CreateWorldScreen screen) {
        var access = (CreateWorldScreenAccess) screen;
        var oldBar = access.fsg262$getTabNavigationBar();
        if (oldBar == null || oldBar.getTabs().stream()
                .anyMatch(tab -> tab.getTabTitle().getString().equals("MCSR"))) return;
        var tabs = new ArrayList<>(oldBar.getTabs());
        tabs.add(new McsrTab(screen));
        var newBar = MenuTabBar.builder(access.fsg262$getTabManager(), screen.width)
                .addTabs(tabs.toArray(Tab[]::new)).build();
        var screenAccess = (FsgScreenAccess) screen;
        screenAccess.fsg262$removeWidget(oldBar);
        screenAccess.fsg262$addWidget(newBar);
        access.fsg262$setTabNavigationBar(newBar);
        newBar.arrangeElements(screen.width);
    }

    private static final class McsrTab extends GridLayoutTab {
        private final CreateWorldScreen screen;
        private final Button statusLabel;
        private final Button progressLabel;
        private final Button acceptedLabel;

        private McsrTab(CreateWorldScreen screen) {
            super(Component.literal("MCSR"));
            this.screen = screen;
            this.statusLabel = text("Status: " + CONTROLLER.status());
            this.progressLabel = text(progressText());
            this.acceptedLabel = text(acceptedText());
            layout.columnSpacing(8).rowSpacing(4);
            addToggle("Enabled", CONTROLLER.settings().enabled(), current ->
                    update(current, !current.enabled(), current.seedType(), current.profileName(),
                            current.completable(), current.standardizedRng(), current.customRngSeed()),
                    settings -> settings.enabled(), 0, 0);
            addCycle("Seed Type", CONTROLLER.settings().seedType().name(), button -> {
                var current = CONTROLLER.settings();
                update(button, current, nextSeedType(current.seedType()), current.profileName(),
                        current.completable(), current.standardizedRng(), current.customRngSeed());
            }, 0, 1);
            addCycle("Profile", CONTROLLER.settings().profileName(), button -> {
                var current = CONTROLLER.settings();
                update(button, current, current.seedType(), nextProfile(current.profileName()),
                        current.completable(), current.standardizedRng(), current.customRngSeed());
            }, 1, 0);
            addToggle("Completable", CONTROLLER.settings().completable(), current ->
                    update(current, current.enabled(), current.seedType(), current.profileName(),
                            !current.completable(), current.standardizedRng(), current.customRngSeed()),
                    settings -> settings.completable(), 1, 1);
            addToggle("Standardized RNG", CONTROLLER.settings().standardizedRng(), current ->
                    update(current, current.enabled(), current.seedType(), current.profileName(),
                            current.completable(), !current.standardizedRng(), current.customRngSeed()),
                    settings -> settings.standardizedRng(), 2, 0);
            addToggle("Background Filtering", CONTROLLER.settings().backgroundFiltering(), current ->
                    update(current, current.enabled(), current.seedType(), current.profileName(),
                            current.completable(), current.standardizedRng(), current.customRngSeed(),
                            !current.backgroundFiltering()),
                    settings -> settings.backgroundFiltering(), 2, 1);
            addCycle("RNG Seed", rngLabel(), button -> {
                var current = CONTROLLER.settings();
                update(button, current, current.seedType(), current.profileName(),
                        current.completable(), current.standardizedRng(),
                        current.customRngSeed() == null ? 0L : null);
            }, 3, 0);
            layout.addChild(Button.builder(Component.literal("START FILTERING"),
                    button -> CONTROLLER.startSearch(screen,
                            ignored -> Minecraft.getInstance().execute(this::refresh),
                            status -> Minecraft.getInstance().execute(() -> {
                                statusLabel.setMessage(Component.literal("Status: " + status));
                                refresh();
                            })))
                    .bounds(0, 0, 140, 20).build(), 4, 0, 1, 2);
            layout.addChild(Button.builder(Component.literal("STOP FILTERING"),
                    button -> CONTROLLER.cancel()).bounds(0, 0, 140, 20).build(), 5, 0, 1, 2);
            layout.addChild(statusLabel, 6, 0, 1, 2);
            layout.addChild(progressLabel, 7, 0, 1, 2);
            layout.addChild(acceptedLabel, 8, 0, 1, 2);
        }

        private Button text(String value) {
            return Button.builder(Component.literal(value), button -> {})
                    .bounds(0, 0, 140, 20).build();
        }

        private void refresh() {
            statusLabel.setMessage(Component.literal("Status: " + CONTROLLER.status()));
            progressLabel.setMessage(Component.literal(progressText()));
            acceptedLabel.setMessage(Component.literal(acceptedText()));
        }

        private String progressText() {
            var progress = CONTROLLER.progress();
            if (progress == null) return "Candidates: 0 | Candidates/sec: 0.0 | Current: -";
            return String.format(java.util.Locale.ROOT,
                    "Candidates: %d | Candidates/sec: %.1f | Current: %d | Stage: %s",
                    progress.tested(), progress.candidatesPerSecond(),
                    progress.currentSeed(), progress.currentStage());
        }

        private String acceptedText() {
            return "Accepted Seed: " + (CONTROLLER.hasAcceptedSeed()
                    ? CONTROLLER.acceptedSeed() : "None");
        }

        private void addToggle(String label, boolean value,
                               java.util.function.Consumer<WorldCreationSettings> action,
                               java.util.function.Function<WorldCreationSettings, Boolean> selected,
                               int row, int column) {
            layout.addChild(Button.builder(Component.literal(label + ": " + (value ? "ON" : "OFF")), button -> {
                var current = CONTROLLER.settings();
                action.accept(current);
                button.setMessage(Component.literal(label + ": "
                        + (selected.apply(CONTROLLER.settings()) ? "ON" : "OFF")));
            }).bounds(0, 0, 150, 20).build(), row, column);
        }

        private void addCycle(String label, String value,
                              java.util.function.Consumer<Button> action, int row, int column) {
            layout.addChild(Button.builder(Component.literal(label + ": " + value),
                    button -> action.accept(button)).bounds(0, 0, 150, 20).build(), row, column);
        }

        private void update(WorldCreationSettings current, boolean enabled, SeedTypeChoice type,
                            String profile, boolean completable, boolean rng, Long rngSeed) {
            CONTROLLER.updateSettings(new WorldCreationSettings(enabled, type, profile,
                    completable, rng, rngSeed, current.backgroundFiltering()));
        }

        private void update(WorldCreationSettings current, boolean enabled, SeedTypeChoice type,
                            String profile, boolean completable, boolean rng, Long rngSeed,
                            boolean backgroundFiltering) {
            CONTROLLER.updateSettings(new WorldCreationSettings(enabled, type, profile,
                    completable, rng, rngSeed, backgroundFiltering));
        }

        private void update(Button button, WorldCreationSettings current, SeedTypeChoice type,
                            String profile, boolean completable, boolean rng, Long rngSeed) {
            update(current, current.enabled(), type, profile, completable, rng, rngSeed);
            String prefix = button.getMessage().getString().split(":")[0];
            button.setMessage(Component.literal(prefix + ": "
                    + (prefix.equals("Seed Type") ? type.name()
                    : prefix.equals("Profile") ? profile
                    : rngSeed == null ? "Overworld" : rngSeed)));
        }

        private SeedTypeChoice nextSeedType(SeedTypeChoice current) {
            var values = new SeedTypeChoice[] {SeedTypeChoice.VILLAGE, SeedTypeChoice.SHIPWRECK,
                    SeedTypeChoice.DESERT_TEMPLE, SeedTypeChoice.RUINED_PORTAL,
                    SeedTypeChoice.BURIED_TREASURE};
            for (int i = 0; i < values.length; i++) if (values[i] == current) return values[(i + 1) % values.length];
            return values[0];
        }

        private String nextProfile(String current) {
            var values = new String[] {FilterProfile.strictRankedStyle().name(),
                    FilterProfile.balanced().name(), FilterProfile.completable().name()};
            for (int i = 0; i < values.length; i++) if (values[i].equals(current)) return values[(i + 1) % values.length];
            return values[0];
        }

        private String rngLabel() {
            var seed = CONTROLLER.settings().customRngSeed();
            return seed == null ? "Overworld" : Long.toString(seed);
        }
    }
}
