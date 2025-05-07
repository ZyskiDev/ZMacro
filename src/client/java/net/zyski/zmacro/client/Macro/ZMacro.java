package net.zyski.zmacro.client.Macro;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import net.minecraft.network.chat.Component;
import net.zyski.zmacro.client.chat.ChatEvent;

import java.time.Instant;

public interface ZMacro {

    void start();
    void loop();
    void stop();
    void toggle();
    boolean isActive();

    default void onRender(GuiGraphics graphics){

    }

    default void onChat(ChatEvent chatEvent){

    }

    default void message(String text) {
        Minecraft.getInstance().gui.getChat().addMessage(Component.literal(text));
        ChatLog chatlog = Minecraft.getInstance().getReportingContext().chatLog();
        chatlog.push(LoggedChatMessage.system(Component.literal(text), Instant.now()));
    }

}
