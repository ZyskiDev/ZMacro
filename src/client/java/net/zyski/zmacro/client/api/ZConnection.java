package net.zyski.zmacro.client.api;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

import java.util.Random;

public class ZConnection {

    private static ServerAddress lastServerAddress;
    private static ServerData lastServerData;
    private static boolean lastFlag;
    private static TransferState lastTransferState;

    private static long nextAttempt = -1;
    private static long INTERVAL = 300000;

    public static void setLastConnection(ServerAddress serverAddress,
                                         ServerData serverData, boolean flag, TransferState transferState) {
        lastServerAddress = serverAddress;
        lastServerData = serverData;
        lastFlag = flag;
        lastTransferState = transferState;
    }

    public static void reconnect() {
        if ( lastServerAddress != null && lastServerData != null) {
            if(System.currentTimeMillis() >= nextAttempt) {
                ConnectScreen.startConnecting(
                        new JoinMultiplayerScreen(new TitleScreen()),
                        Minecraft.getInstance(),
                        lastServerAddress,
                        lastServerData,
                        false,
                        lastTransferState
                );
                nextAttempt = System.currentTimeMillis() + INTERVAL;
            }
        }
    }

    public static ServerData getLastServerData() { return lastServerData; }

    public static ServerAddress getLastServerAddress() { return lastServerAddress; }

    public static boolean shouldReconnect() {
        if (!hasStoredConnection()) return false;

        if (Minecraft.getInstance().screen instanceof ConnectScreen) return false;

        ClientPacketListener listener = Minecraft.getInstance().getConnection();
        if (listener != null && listener.getConnection().isConnected()) return false;

        return Minecraft.getInstance().screen instanceof TitleScreen || Minecraft.getInstance().screen instanceof JoinMultiplayerScreen || Minecraft.getInstance().screen instanceof DisconnectedScreen;
    }

    public static boolean hasStoredConnection() {
        return lastServerAddress != null && lastServerData != null;
    }

    public static void disconnect(){
        nextAttempt = System.currentTimeMillis() + ((new Random().nextInt(120000) + 60000));
    }
}
