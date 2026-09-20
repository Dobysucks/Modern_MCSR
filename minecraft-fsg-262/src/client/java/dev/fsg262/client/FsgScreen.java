package dev.fsg262.client;

import dev.fsg262.client.world.WorldCreationController;
import dev.fsg262.client.world.WorldCreationSettings;
import dev.fsg262.filter.FilterProfile;
import dev.fsg262.filter.SeedTypeChoice;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;

public final class FsgScreen extends Screen {
    private static final WorldCreationController CONTROLLER = WorldCreationController.instance();
    private final Screen parent;
    private String status = "Idle";

    public FsgScreen(Screen parent) {
        super(Component.literal("MCSR"));
        this.parent = parent;
    }

    public static Button openButton(int x, int y, Runnable action) {
        return Button.builder(Component.literal("MCSR"), button -> action.run())
                .bounds(x, y, 200, 20).build();
    }

    @Override
    protected void init() {
        addRenderableWidget(toggle("Status: ", CONTROLLER.settings().enabled(), current ->
                update(current, !current.enabled(), current.seedType(), current.profileName(),
                        current.completable(), current.standardizedRng(), current.customRngSeed())));
        addRenderableWidget(cycle("Seed Type: ", CONTROLLER.settings().seedType().name(), button -> {
            var current = CONTROLLER.settings();
            update(button, current, nextSeedType(current.seedType()), current.profileName(),
                    current.completable(), current.standardizedRng(), current.customRngSeed());
        }));
        addRenderableWidget(cycle("Profile: ", CONTROLLER.settings().profileName(), button -> {
            var current = CONTROLLER.settings();
            update(button, current, current.seedType(), nextProfile(current.profileName()),
                    current.completable(), current.standardizedRng(), current.customRngSeed());
        }));
        addRenderableWidget(toggle("Completable: ", CONTROLLER.settings().completable(), current ->
                update(current, current.enabled(), current.seedType(), current.profileName(),
                        !current.completable(), current.standardizedRng(), current.customRngSeed())));
        addRenderableWidget(toggle("Standardized RNG: ", CONTROLLER.settings().standardizedRng(), current ->
                update(current, current.enabled(), current.seedType(), current.profileName(),
                        current.completable(), !current.standardizedRng(), current.customRngSeed())));
        addRenderableWidget(cycle("RNG Seed: ", rngLabel(), button -> {
            var current = CONTROLLER.settings();
            update(button, current, current.seedType(), current.profileName(),
                    current.completable(), current.standardizedRng(),
                    current.customRngSeed() == null ? 0L : null);
        }));
        addRenderableWidget(Button.builder(Component.literal("START FILTERING"), button -> startSearch())
                .bounds(width / 2 - 100, 195, 200, 20).build());
        addRenderableWidget(Button.builder(Component.literal("STOP FILTERING"), button -> {
            CONTROLLER.cancel();
            status = "Stopped";
        }).bounds(width / 2 - 100, 220, 200, 20).build());
        addRenderableWidget(Button.builder(Component.literal("BACK"), button -> minecraft.setScreenAndShow(parent))
                .bounds(width / 2 - 100, height - 35, 200, 20).build());
    }

    private Button toggle(String prefix, boolean value, java.util.function.Consumer<WorldCreationSettings> action) {
        return Button.builder(Component.literal(prefix + (value ? "ON" : "OFF")), button -> {
            action.accept(CONTROLLER.settings());
            button.setMessage(Component.literal(prefix + (!value ? "ON" : "OFF")));
        }).bounds(width / 2 - 100, 45 + 25 * children().size(), 200, 20).build();
    }

    private Button cycle(String prefix, String value, java.util.function.Consumer<Button> action) {
        return Button.builder(Component.literal(prefix + value), button -> action.accept(button))
                .bounds(width / 2 - 100, 45 + 25 * children().size(), 200, 20).build();
    }

    private void update(WorldCreationSettings current, boolean enabled, SeedTypeChoice type,
                        String profile, boolean completable, boolean rng, Long rngSeed) {
        CONTROLLER.updateSettings(new WorldCreationSettings(enabled, type, profile,
                completable, rng, rngSeed, current.backgroundFiltering()));
    }

    private void update(Button button, WorldCreationSettings current, SeedTypeChoice type,
                        String profile, boolean completable, boolean rng, Long rngSeed) {
        update(current, current.enabled(), type, profile, completable, rng, rngSeed);
        button.setMessage(Component.literal(button.getMessage().getString().startsWith("Seed Type")
                ? "Seed Type: " + type.name() : button.getMessage().getString().startsWith("Profile")
                ? "Profile: " + profile : "RNG Seed: " + (rngSeed == null ? "Overworld" : rngSeed)));
    }

    private void startSearch() {
        if (parent instanceof CreateWorldScreen createWorld) {
            status = "Searching";
            CreateWorldIntegration.startSearch(createWorld, minecraft, new String[] {status});
        } else {
            status = "Open Create New World to bind the active 26.2 worldgen context";
        }
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

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        extractMenuBackground(graphics);
        graphics.textRenderer().accept(net.minecraft.client.gui.TextAlignment.CENTER,
                width / 2, 20, title);
        graphics.textRenderer().accept(net.minecraft.client.gui.TextAlignment.CENTER,
                width / 2, height - 55, Component.literal("Status: " + status));
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
