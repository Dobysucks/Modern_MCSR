package dev.fsg262.client;

import dev.fsg262.cache.SeedCache;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Small, client-only control surface. Search work remains in the background
 * manager; this screen deliberately never performs world generation on the
 * render thread.
 */
public final class FsgScreen extends Screen {
    private final Screen parent;
    private Button seedType;
    private Button profile;
    private Button rng;
    private Button completable;
    private String selectedSeedType = "ANY";
    private String selectedProfile = "COMPLETABLE";
    private boolean standardizedRng = true;
    private boolean requireCompletable = true;
    private String status = "Idle (world-generation adapter: NOT VERIFIED)";

    public FsgScreen(Screen parent) {
        super(Component.literal("FSG Seed Filter"));
        this.parent = parent;
    }

    public static Button openButton(int x, int y, Runnable action) {
        return Button.builder(Component.literal("FSG Seed Filter"), button -> action.run())
                .bounds(x, y, 200, 20)
                .build();
    }

    @Override
    protected void init() {
        int center = width / 2;
        seedType = addRenderableWidget(Button.builder(label("Seed Type: ", selectedSeedType),
                        button -> cycleSeedType())
                .bounds(center - 100, 45, 200, 20).build());
        profile = addRenderableWidget(Button.builder(label("Profile: ", selectedProfile),
                        button -> cycleProfile())
                .bounds(center - 100, 70, 200, 20).build());
        rng = addRenderableWidget(Button.builder(label("Standardized RNG: ", standardizedRng ? "ON" : "OFF"),
                        button -> {
                            standardizedRng = !standardizedRng;
                            rng.setMessage(label("Standardized RNG: ", standardizedRng ? "ON" : "OFF"));
                        })
                .bounds(center - 100, 95, 200, 20).build());
        completable = addRenderableWidget(Button.builder(
                        label("Require Completable: ", requireCompletable ? "ON" : "OFF"),
                        button -> {
                            requireCompletable = !requireCompletable;
                            completable.setMessage(label("Require Completable: ",
                                    requireCompletable ? "ON" : "OFF"));
                        })
                .bounds(center - 100, 120, 200, 20).build());
        addRenderableWidget(Button.builder(Component.literal("START SEARCH"), button -> {
                    status = "Search requested; awaiting a verified 26.2 world-generation adapter";
                }).bounds(center - 100, 155, 200, 20).build());
        addRenderableWidget(Button.builder(Component.literal("CLEAR CACHE"), button -> {
                    status = "Cache clear requested (version " + SeedCache.FILTER_VERSION + ")";
                }).bounds(center - 100, 180, 200, 20).build());
        addRenderableWidget(Button.builder(Component.literal("DONE"), button -> minecraft.setScreenAndShow(parent))
                .bounds(center - 100, 215, 200, 20).build());
    }

    private void cycleSeedType() {
        String[] values = {"ANY", "VILLAGE", "SHIPWRECK", "DESERT TEMPLE",
                "RUINED PORTAL", "BURIED TREASURE"};
        selectedSeedType = next(values, selectedSeedType);
        seedType.setMessage(label("Seed Type: ", selectedSeedType));
    }

    private void cycleProfile() {
        String[] values = {"COMPLETABLE", "STRICT", "BALANCED", "CUSTOM"};
        selectedProfile = next(values, selectedProfile);
        profile.setMessage(label("Profile: ", selectedProfile));
    }

    private String next(String[] values, String current) {
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(current)) return values[(i + 1) % values.length];
        }
        return values[0];
    }

    private Component label(String prefix, String value) {
        return Component.literal(prefix + value);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        extractMenuBackground(graphics);
        graphics.textRenderer().accept(net.minecraft.client.gui.TextAlignment.CENTER,
                width / 2, 20, title);
        graphics.textRenderer().accept(net.minecraft.client.gui.TextAlignment.CENTER,
                width / 2, height - 35, Component.literal(status));
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
