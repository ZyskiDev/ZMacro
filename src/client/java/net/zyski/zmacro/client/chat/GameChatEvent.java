package net.zyski.zmacro.client.chat;

import net.minecraft.network.chat.Component;

public class GameChatEvent implements ChatEvent {
    private final Component message;
    private final boolean overlay;
    private boolean cancel = false;

    public GameChatEvent(Component message, boolean overlay) {
        this.message = message;
        this.overlay = overlay;
    }

    public Component message() {
        return message;
    }

    public boolean overlay() {
        return overlay;
    }

    public boolean cancel() {
        return cancel;
    }

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }
}

