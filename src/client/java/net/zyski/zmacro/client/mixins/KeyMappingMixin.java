package net.zyski.zmacro.client.mixins;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.zyski.zmacro.client.ZmacroClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyMapping.class)
public abstract class KeyMappingMixin {

    @Inject(method = "set", at = @At("HEAD"), cancellable = true)
    private static void onSet(InputConstants.Key key, boolean isDown, CallbackInfo ci) {
        // Block left or right mouse button
        if (key.getType() == InputConstants.Type.MOUSE) {
            int button = key.getValue();
            if (button == 0 || button == 1) { // 0 = left click, 1 = right click
                if (ZmacroClient.getInstance().blockMouseGrabbing) {
                    ci.cancel(); // Prevents the call from reaching keymapping.setDown(flag)
                }
            }
        }
    }


}

