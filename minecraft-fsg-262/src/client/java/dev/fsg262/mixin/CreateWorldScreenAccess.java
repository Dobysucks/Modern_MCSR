package dev.fsg262.mixin;

import net.minecraft.client.gui.components.tabs.MenuTabBar;
import net.minecraft.client.gui.components.tabs.TabManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(net.minecraft.client.gui.screens.worldselection.CreateWorldScreen.class)
public interface CreateWorldScreenAccess {
    @Accessor("tabManager")
    TabManager fsg262$getTabManager();

    @Accessor("tabNavigationBar")
    MenuTabBar fsg262$getTabNavigationBar();

    @Accessor("tabNavigationBar")
    void fsg262$setTabNavigationBar(MenuTabBar bar);
}
