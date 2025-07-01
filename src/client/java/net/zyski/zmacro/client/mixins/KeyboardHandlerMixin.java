package net.zyski.zmacro.client.mixins;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.zyski.zmacro.client.ZmacroClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {

    @Inject(
            method = "keyPress(JIIII)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onKeyPress(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (action == GLFW.GLFW_PRESS) {
            Minecraft client = Minecraft.getInstance();
            if (client.player != null) {
                if (ZmacroClient.getInstance().getSelected() != null && ZmacroClient.getInstance().getSelected().isActive()) {
                    ZmacroClient.getInstance().getSelected().onKeyPress(key, modifiers, scancode);
                }
            }
        }
    }

}
