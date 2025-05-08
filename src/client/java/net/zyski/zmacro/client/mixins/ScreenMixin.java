package net.zyski.zmacro.client.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.zyski.zmacro.client.ZmacroClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Shadow protected abstract <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T guiEventListener);

    @Shadow protected abstract void removeWidget(GuiEventListener guiEventListener);

    @Shadow public int width;
    @Shadow public int height;
    @Unique
    private Button stopMacroButton;

    @Unique
    private boolean buttonInitialized = false;

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        Screen self = (Screen)(Object)this;

        if (!ZmacroClient.getInstance().isSelectedActive()) {
            removeButtonIfPresent(self);
            return;
        }

        if (!buttonInitialized) {
            stopMacroButton = Button.builder(
                            Component.literal("⏹ Stop Macro"),
                            button -> {
                                ZmacroClient.getInstance().getSelected().toggle();
                                removeButtonIfPresent(self);
                            }
                    )
                    .bounds(
                            width / 2 - 50,
                            height - 40,
                            100,
                            20
                    )
                    .build();

            // Use the public addRenderableWidget method
            addRenderableWidget(stopMacroButton);
            buttonInitialized = true;
        }
    }

    @Unique
    private void removeButtonIfPresent(Screen screen) {
        if (stopMacroButton != null) {
            removeWidget(stopMacroButton);
            stopMacroButton = null;
            buttonInitialized = false;
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(GuiGraphics gui, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (stopMacroButton != null) {
            Screen self = (Screen)(Object)this;
            stopMacroButton.setX(width / 2 - 50);
            stopMacroButton.setY(height - 40);
        }
    }

    @Inject(method = "resize", at = @At("TAIL"))
    private void onResize(Minecraft client, int width, int height, CallbackInfo ci) {
        Screen self = (Screen)(Object)this;
        removeButtonIfPresent(self);
        this.onInit(ci); // Reinitialize the button
    }
}