package net.zyski.zmacro.client.mixins;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.zyski.zmacro.client.ZmacroClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Shadow protected abstract <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T guiEventListener);

    @Shadow protected abstract void removeWidget(GuiEventListener guiEventListener);

    @Shadow public int width;
    @Shadow public int height;

     final ResourceLocation STOP_SPRITE = ResourceLocation.fromNamespaceAndPath("zmacro","textures/stop.png");


    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {

    }


    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(GuiGraphics gui, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if(ZmacroClient.getInstance().getSelected() != null && ZmacroClient.getInstance().getSelected().isActive()) {
            Minecraft client = Minecraft.getInstance();
            RenderSystem.enableBlend();
            int spriteSize = 32;
            int x = (gui.guiWidth() / 2) - (spriteSize / 2);
            int y = gui.guiHeight() - 90;
            gui.blit(RenderType::guiTextured,
                    ResourceLocation.fromNamespaceAndPath("zmacro", "textures/stop.png"),
                    x, // Centered horizontally
                    y,
                    0, 0, spriteSize, spriteSize, spriteSize, spriteSize
            );

            String keyText = "[ " + ZmacroClient.getInstance().OPEN_GUI.getTranslatedKeyMessage().getString() + " ]";
            gui.drawString(
                    client.font,
                    keyText,
                    x + (spriteSize / 2) - (client.font.width(keyText) / 2),
                    y + (spriteSize / 2) - 4,
                    0xFFFFFF,
                    true
            );
            String stopText = "STOP MACRO";
            gui.drawString(
                    client.font,
                    stopText,
                    x + (spriteSize / 2) - (client.font.width(stopText) / 2),
                    y + 5 + (spriteSize) - 4,
                    0xFFFFFF,
                    true
            );
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int i, int j, int k, CallbackInfoReturnable<Boolean> cir) {
        if ( ZmacroClient.getInstance().OPEN_GUI.matches(i, j)) {
            if (ZmacroClient.getInstance().isSelectedActive()) {
                ZmacroClient.getInstance().getSelected().toggle();
                cir.setReturnValue(true);
            }
        }
    }
}