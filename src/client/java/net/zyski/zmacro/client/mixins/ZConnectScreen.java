package net.zyski.zmacro.client.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.zyski.zmacro.client.api.ZConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConnectScreen.class)
public class ZConnectScreen {

    @Inject(method = "startConnecting", at = @At("HEAD"))
    private static void captureConnectionParams(Screen screen, Minecraft minecraft, ServerAddress serverAddress,
                                                ServerData serverData, boolean flag, TransferState transferState,
                                                CallbackInfo ci) {
       ZConnection.setLastConnection(serverAddress, serverData, flag, transferState);
    }

}
