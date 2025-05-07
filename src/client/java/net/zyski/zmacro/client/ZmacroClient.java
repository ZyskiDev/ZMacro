package net.zyski.zmacro.client;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.zyski.zmacro.client.Macro.Macro;
import net.zyski.zmacro.client.Macro.ZMacro;
import net.zyski.zmacro.client.chat.GameChatEvent;
import net.zyski.zmacro.client.chat.PlayerChatEvent;
import net.zyski.zmacro.client.screen.MacroSelectionScreen;
import net.zyski.zmacro.client.screen.SimpleMacroScreen;
import net.zyski.zmacro.client.util.MacroMetadata;
import net.zyski.zmacro.client.util.MemoryMappedClassLoader;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;


public class ZmacroClient implements ClientModInitializer {

    private  List<ZMacro> loadedMacros = new ArrayList<>();
    private  HashMap<ZMacro, MacroMetadata> metadataMap = new HashMap<>();
    private List<MemoryMappedClassLoader> activeClassLoaders = new ArrayList<>();
    ZMacro selected = null;
    File directory;

    private static ZmacroClient instance;

    public static KeyMapping OPEN_GUI = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("net.ZMacro.open_gui", InputConstants.Type.KEYSYM, InputConstants.KEY_EQUALS, "ZMacro")
    );

    @Override
    public void onInitializeClient() {
        directory = Minecraft.getInstance().gameDirectory.toPath().resolve("ZMacro").toFile();
        directory.mkdir();
        try {
            loadMacrosFromFolder(directory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        registerCommands();
        registerGraphicsThread();
        registerTickThread();
        registerKeyBindThread();
        registerChatListenerThread();
        instance = this;
    }

    private void registerChatListenerThread() {
        ClientReceiveMessageEvents.ALLOW_GAME.register(new ClientReceiveMessageEvents.AllowGame() {
            @Override
            public boolean allowReceiveGameMessage(Component message, boolean overlay) {
                GameChatEvent event = new GameChatEvent(message, overlay);
                if(selected != null && selected.isActive()){
                    selected.onChat(event);
                }
                return true;
            }
        });

        ClientReceiveMessageEvents.ALLOW_CHAT.register(new ClientReceiveMessageEvents.AllowChat() {
            @Override
            public boolean allowReceiveChatMessage(Component component, @Nullable PlayerChatMessage playerChatMessage, @Nullable GameProfile gameProfile, ChatType.Bound bound, Instant instant) {
                PlayerChatEvent event = new PlayerChatEvent(component, playerChatMessage, gameProfile, bound, instant);
                if(selected != null && selected.isActive()){
                    selected.onChat(event);
                }
                return true;
            }
        });
    }

    private void registerKeyBindThread() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_GUI.consumeClick()) {
                client.setScreen(new MacroSelectionScreen(metadataMap, client.screen));
            }

        });
    }

    private void registerTickThread() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(selected != null && selected.isActive())
                selected.loop();
        });
    }

    private void registerGraphicsThread() {
        HudRenderCallback.EVENT.register((guiGraphics, deltaTracker) -> {
            if(selected != null && selected.isActive())
                selected.onRender(guiGraphics);
        });
    }

    private void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> {

            dispatcher.register(ClientCommandManager.literal("reloadmacros").executes(context -> {
                if(selected != null && selected.isActive()){
                    message("Please stop macro before executing reload..");
                    return 1;
                }
                loadMacrosAsync(directory);
                return 1;
            }));

            dispatcher.register(ClientCommandManager.literal("stopmacro").executes(context -> {
                if(selected != null && selected.isActive()){
                    message("Stopping Macro...");
                    selected.toggle();
                    selected = null;
                }else{
                    message("No Macro Running.");
                }
                return 1;
            }));
        }));
    }

    public void setSelected(ZMacro selected) {
        this.selected = selected;
        selected.toggle();
    }


    public void loadMacrosAsync(File directory) {
        CompletableFuture.runAsync(() -> {
            try {
                if(Minecraft.getInstance().player != null)
                    Minecraft.getInstance().player.displayClientMessage(
                            Component.literal("Attempting to reload macros..."),
                            false
                    );
                loadMacrosFromFolder(directory);
            } catch (IOException e) {
                Minecraft.getInstance().execute(() -> {
                    throw new RuntimeException(e);
                });
            }
        }).thenRunAsync(() -> {
            if(Minecraft.getInstance().player != null)
                 Minecraft.getInstance().player.displayClientMessage(
            Component.literal("Macros loaded successfully! ("+loadedMacros.size()+")"),
                      false
                  );
        }, Minecraft.getInstance());
    }

    public void loadMacrosFromFolder(File macrosFolder) throws IOException {
        releaseAllMacros();

        File[] jarFiles = macrosFolder.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jarFiles == null) return;

        for (File jarFile : jarFiles) {
            byte[] jarBytes = Files.readAllBytes(jarFile.toPath());

            MemoryMappedClassLoader classLoader = new MemoryMappedClassLoader(
                    jarBytes,
                    jarFile.getName(),
                    getClass().getClassLoader()
            );
            activeClassLoaders.add(classLoader);

            try (JarInputStream jarStream = new JarInputStream(new ByteArrayInputStream(jarBytes))) {
                JarEntry entry;
                while ((entry = jarStream.getNextJarEntry()) != null) {
                    if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                        processJarEntry(classLoader, entry);
                    }
                }
            }
        }
    }

    private void processJarEntry(ClassLoader loader, JarEntry entry) {
        String className = entry.getName()
                .replace(".class", "")
                .replace("/", ".");

        try {
            Class<?> cls = loader.loadClass(className);
            if (cls.isAnnotationPresent(Macro.class) && ZMacro.class.isAssignableFrom(cls)) {
                ZMacro macro = (ZMacro) cls.getDeclaredConstructor().newInstance();
                Macro meta = cls.getAnnotation(Macro.class);

                loadedMacros.add(macro);
                metadataMap.put(macro, new MacroMetadata(
                        meta.name(), meta.version(), meta.author(), meta.description(), meta.icon()
                ));
            }
        } catch (Exception e) {
            System.err.println("Failed to process " + className);
        }
    }

    public void releaseAllMacros() {
        loadedMacros.clear();
        metadataMap.clear();

        activeClassLoaders.clear();

        System.gc();
    }

    private void message(String message){
        if(Minecraft.getInstance().player != null)
            Minecraft.getInstance().player.displayClientMessage(Component.literal(message), false);
    }

    public static ZmacroClient getInstance(){
        return instance;
    }

    public boolean isSelectedActive() {
        return selected != null && selected.isActive();
    }

    public ZMacro getSelected() {
        return selected;
    }
}
