package dev.fsg262.client;

import dev.fsg262.client.world.WorldCreationController;
import dev.fsg262.client.world.WorldCreationSettings;
import dev.fsg262.mixin.CreateWorldScreenAccess;
import dev.fsg262.mixin.FsgScreenAccess;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.components.tabs.MenuTabBar;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;

import java.io.IOException;
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
        tabs.add(new McsrTab());
        var newBar = MenuTabBar.builder(access.fsg262$getTabManager(), screen.width)
                .addTabs(tabs.toArray(Tab[]::new)).build();
        var screenAccess = (FsgScreenAccess) screen;
        screenAccess.fsg262$removeWidget(oldBar);
        screenAccess.fsg262$addWidget(newBar);
        access.fsg262$setTabNavigationBar(newBar);
        newBar.arrangeElements(screen.width);
    }

    private static final class McsrTab extends GridLayoutTab {
        private final Button enabledButton;
        private final Button profileButton;
        private final Button completableButton;
        private final Button rngButton;
        private final Button brutesButton;
        private final Button overworldCategoryButton;
        private final Button netherCategoryButton;
        private final StringWidget overworldValueLabel;
        private final StringWidget netherValueLabel;
        private final StringWidget countLabel;
        private final StringWidget statusLabel;

        private McsrTab() {
            super(Component.literal("MCSR"));
            layout.columnSpacing(8).rowSpacing(4);
            try {
                CONTROLLER.ensureCategorySelections();
            } catch (IOException | IllegalStateException exception) {
                CONTROLLER.setStatus("Seed selection failed: " + exception.getMessage());
            }
            var settings = CONTROLLER.settings();
            enabledButton = toggle("MCSR", settings.enabled(), value ->
                    CONTROLLER.updateSettings(CONTROLLER.settings().withEnabled(value)),
                    WorldCreationSettings::enabled);
            profileButton = cycle("Profile", settings.profileName(),
                    this::cycleProfile);
            completableButton = toggle("Completable", settings.completable(), value ->
                    CONTROLLER.updateSettings(CONTROLLER.settings().withCompletable(value)),
                    WorldCreationSettings::completable);
            rngButton = toggle("Standardized RNG", settings.standardizedRng(), value ->
                    CONTROLLER.updateSettings(CONTROLLER.settings().withStandardizedRng(value)),
                    WorldCreationSettings::standardizedRng);
            brutesButton = toggle("Disable Piglin Brutes", settings.disablePiglinBrutes(), value ->
                    CONTROLLER.updateSettings(CONTROLLER.settings().withDisablePiglinBrutes(value)),
                    WorldCreationSettings::disablePiglinBrutes);
            countLabel = wideText(CONTROLLER.countsText());
            overworldCategoryButton = categoryButton(true);
            netherCategoryButton = categoryButton(false);
            overworldValueLabel = wideText(seedText("Overworld", settings.overworldSeed()));
            netherValueLabel = wideText(seedText("Nether", settings.netherSeed()));
            statusLabel = wideText(displayText("Status: " + CONTROLLER.status(), 500));

            layout.addChild(enabledButton, 0, 0);
            layout.addChild(profileButton, 0, 1);
            layout.addChild(completableButton, 1, 0);
            layout.addChild(rngButton, 1, 1);
            layout.addChild(brutesButton, 2, 0, 1, 2);
            layout.addChild(overworldCategoryButton, 3, 0);
            layout.addChild(netherCategoryButton, 3, 1);
            layout.addChild(overworldValueLabel, 4, 0, 1, 2);
            layout.addChild(netherValueLabel, 5, 0, 1, 2);
            layout.addChild(countLabel, 6, 0, 1, 2);
            layout.addChild(statusLabel, 7, 0, 1, 2);
        }

        private StringWidget wideText(String value) {
            return new StringWidget(0, 0, 500, 20, Component.literal(value),
                    Minecraft.getInstance().font);
        }

        private String displayText(String value, int maxWidth) {
            var font = Minecraft.getInstance().font;
            if (font.width(value) <= maxWidth) return value;
            var shortened = value;
            while (!shortened.isEmpty()
                    && font.width(shortened + "...") > maxWidth) {
                shortened = shortened.substring(0, shortened.length() - 1);
            }
            return shortened + "...";
        }

        private Button cycle(String label, String value,
                             java.util.function.Consumer<Button> action) {
            return Button.builder(Component.literal(label + ": " + value),
                            button -> action.accept(button))
                    .bounds(0, 0, 150, 20).build();
        }

        private Button categoryButton(boolean overworld) {
            var category = overworld ? CONTROLLER.settings().overworldCategory()
                    : CONTROLLER.settings().netherCategory();
            var dimension = overworld ? "Overworld" : "Nether";
            return Button.builder(Component.literal(dimension + ": " + category),
                            button -> cycleCategory(overworld))
                    .bounds(0, 0, 260, 20).build();
        }

        private Button toggle(String label, boolean value,
                              java.util.function.Consumer<Boolean> setter,
                              java.util.function.Function<WorldCreationSettings, Boolean> getter) {
            return Button.builder(Component.literal(toggleText(label, value)), button -> {
                setter.accept(!getter.apply(CONTROLLER.settings()));
                button.setMessage(Component.literal(toggleText(label, getter.apply(CONTROLLER.settings()))));
                refresh();
            }).bounds(0, 0, 170, 20).build();
        }

        private String toggleText(String label, boolean value) {
            return label + ": " + (value ? "ON" : "OFF");
        }

        private void cycleProfile(Button button) {
            var profiles = WorldCreationSettings.PROFILES;
            var current = CONTROLLER.settings();
            var index = profiles.indexOf(current.profileName());
            var next = profiles.get((index + 1) % profiles.size());
            CONTROLLER.updateSettings(current.withProfile(next));
            button.setMessage(Component.literal("Profile: " + next));
            try {
                CONTROLLER.ensureCategorySelections();
            } catch (IOException | IllegalStateException exception) {
                CONTROLLER.setStatus("Seed selection failed: " + exception.getMessage());
            }
            refresh();
        }

        private void cycleCategory(boolean overworld) {
            var settings = CONTROLLER.settings();
            var categories = overworld ? WorldCreationSettings.OVERWORLD_CATEGORIES
                    : WorldCreationSettings.NETHER_CATEGORIES;
            var current = overworld ? settings.overworldCategory() : settings.netherCategory();
            var next = categories.get((categories.indexOf(current) + 1) % categories.size());
            try {
                if (overworld) CONTROLLER.selectOverworldCategory(next);
                else CONTROLLER.selectNetherCategory(next);
            } catch (IOException | IllegalStateException exception) {
                CONTROLLER.setStatus("Seed selection failed: " + exception.getMessage());
            }
            refresh();
        }

        private String seedText(String dimension, Long seed) {
            return dimension + " seed (informational): " + CONTROLLER.seedLabel(seed);
        }

        private void refresh() {
            try {
                CONTROLLER.ensureCategorySelections();
            } catch (IOException | IllegalStateException exception) {
                CONTROLLER.setStatus("Seed selection failed: " + exception.getMessage());
            }
            var settings = CONTROLLER.settings();
            enabledButton.setMessage(Component.literal(toggleText("MCSR", settings.enabled())));
            profileButton.setMessage(Component.literal("Profile: " + settings.profileName()));
            completableButton.setMessage(Component.literal(
                    toggleText("Completable", settings.completable())));
            rngButton.setMessage(Component.literal(
                    toggleText("Standardized RNG", settings.standardizedRng())));
            brutesButton.setMessage(Component.literal(
                    toggleText("Disable Piglin Brutes", settings.disablePiglinBrutes())));
            overworldCategoryButton.setMessage(Component.literal(
                    "Overworld: " + settings.overworldCategory()));
            netherCategoryButton.setMessage(Component.literal(
                    "Nether: " + settings.netherCategory()));
            overworldValueLabel.setMessage(Component.literal(
                    displayText(seedText("Overworld", settings.overworldSeed()), 500)));
            netherValueLabel.setMessage(Component.literal(
                    displayText(seedText("Nether", settings.netherSeed()), 500)));
            countLabel.setMessage(Component.literal(
                    displayText(CONTROLLER.countsText(), 500)));
            statusLabel.setMessage(Component.literal(
                    displayText("Status: " + CONTROLLER.status(), 500)));
        }
    }
}
