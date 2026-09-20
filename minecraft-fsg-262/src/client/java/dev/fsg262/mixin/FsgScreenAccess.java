package dev.fsg262.mixin;

import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(net.minecraft.client.gui.screens.Screen.class)
public interface FsgScreenAccess {
    @Invoker("addRenderableWidget")
    <T extends net.minecraft.client.gui.components.events.GuiEventListener
            & net.minecraft.client.gui.components.Renderable
            & net.minecraft.client.gui.narration.NarratableEntry> T fsg262$addWidget(T widget);
}
