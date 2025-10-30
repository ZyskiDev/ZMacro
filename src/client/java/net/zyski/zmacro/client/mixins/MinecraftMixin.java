package net.zyski.zmacro.client.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.zyski.zmacro.client.api.ZConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "disconnect", at = @At("HEAD"))
    public void disconnect(Screen screen, boolean flag, CallbackInfo ci) {
        ZConnection.disconnect();
    }

    }
