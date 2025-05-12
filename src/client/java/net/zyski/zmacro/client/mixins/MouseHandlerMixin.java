package net.zyski.zmacro.client.mixins;

import net.minecraft.client.MouseHandler;
import net.zyski.zmacro.client.ZmacroClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Inject( method = "grabMouse", at = @At("HEAD"), cancellable = true)
    public void grabMouse(CallbackInfo ci){
        if(ZmacroClient.getInstance().blockMouseGrabbing){
            ci.cancel();
        }
    }

}
