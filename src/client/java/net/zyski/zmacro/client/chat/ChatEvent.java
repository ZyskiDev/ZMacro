package net.zyski.zmacro.client.chat;

import net.minecraft.network.chat.Component;

public interface ChatEvent {
    Component message();
    boolean cancel();
    void setCancel(boolean cancel);
}

