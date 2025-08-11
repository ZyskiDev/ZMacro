package net.zyski.zmacro.client.mixins;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.zyski.zmacro.client.ZMacroClient;
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
                if (ZMacroClient.getInstance().getSelected() != null && ZMacroClient.getInstance().getSelected().isActive()) {
                    ZMacroClient.getInstance().getSelected().onKeyPress(key, modifiers, scancode);
                }
            }
        }
    }

}
