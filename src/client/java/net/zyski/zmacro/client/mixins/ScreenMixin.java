package net.zyski.zmacro.client.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.zyski.zmacro.client.ZmacroClient;
import net.zyski.zmacro.client.util.Resources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.lwjgl.opengl.GL11C.GL_BLEND;
import static org.lwjgl.opengl.GL11C.glEnable;


@Mixin(Screen.class)
public abstract class ScreenMixin {


    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(GuiGraphics gui, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (ZmacroClient.getInstance().getSelected() != null && ZmacroClient.getInstance().getSelected().isActive()) {
            ZmacroClient.getInstance().getSelected().onScreenRender((Screen)(Object) this, gui, mouseX, mouseY, delta);

            Minecraft client = Minecraft.getInstance();
            glEnable(GL_BLEND);
            int spriteSize = 32;
            int x = (gui.guiWidth() / 2) - (spriteSize / 2);
            int y = gui.guiHeight() - 90;
            gui.blit(RenderType::guiTextured,
                    Resources.STOP_BUTTON,
                    x,
                    y,
                    0, 0, spriteSize, spriteSize, spriteSize, spriteSize
            );

            String keyText = "[" + ZmacroClient.getInstance().OPEN_GUI.getTranslatedKeyMessage().getString() + "]";
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
        if (ZmacroClient.getInstance().OPEN_GUI.matches(i, j)) {
            if (ZmacroClient.getInstance().isSelectedActive()) {
                ZmacroClient.getInstance().getSelected().toggle();
                cir.setReturnValue(true);
            }
        }
    }
}