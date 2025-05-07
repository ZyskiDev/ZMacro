package net.zyski.zmacro.client.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.zyski.zmacro.client.ZmacroClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {


    @Unique
    private Button customButton;

    protected ChatScreenMixin(Component component) {
        super(component);

    }


    @Inject(method = "init", at = @At("TAIL"))
    private void addCustomButton(CallbackInfo ci) {
        if (!ZmacroClient.getInstance().isSelectedActive()) {
            return;
        }

        this.customButton = Button.builder(Component.literal("Stop Macro"), button -> {
                    if (Minecraft.getInstance().player != null) {
                        ZmacroClient.getInstance().getSelected().toggle();
                        this.removeWidget(customButton);
                        customButton = null;
                    }
                })
                .bounds(this.width / 2 - 50, this.height - 40, 100, 20)
                .build();

        this.addRenderableWidget(this.customButton);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void adjustButtonPosition(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.customButton != null) {
            this.customButton.setY(this.height - 40);
        }
    }


}