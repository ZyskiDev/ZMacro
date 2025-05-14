package net.zyski.zmacro.client.chat;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public class PlayerChatEvent implements ChatEvent {
    private final Component message;
    private final @Nullable PlayerChatMessage playerChatMessage;
    private final @Nullable GameProfile profile;
    private final ChatType.Bound bound;
    private final Instant timestamp;
    private boolean cancel = false;

    public PlayerChatEvent(Component message, @Nullable PlayerChatMessage playerChatMessage, @Nullable GameProfile profile, ChatType.Bound bound, Instant timestamp) {
        this.message = message;
        this.playerChatMessage = playerChatMessage;
        this.profile = profile;
        this.bound = bound;
        this.timestamp = timestamp;
    }

    public Component message() {
        return message;
    }

    public @Nullable PlayerChatMessage playerChatMessage() {
        return playerChatMessage;
    }

    public @Nullable GameProfile profile() {
        return profile;
    }

    public ChatType.Bound bound() {
        return bound;
    }

    public Instant timestamp() {
        return timestamp;
    }

    public boolean cancel() {
        return cancel;
    }

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }
}

