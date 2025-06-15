package net.zyski.zmacro.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.zyski.zmacro.client.Macro.Macro;
import net.zyski.zmacro.client.Macro.ZMacro;
import net.zyski.zmacro.client.chat.GameChatEvent;
import net.zyski.zmacro.client.chat.PlayerChatEvent;
import net.zyski.zmacro.client.screen.MacroSelectionScreen;
import net.zyski.zmacro.client.util.MacroWrapper;
import net.zyski.zmacro.client.util.MemoryMappedClassLoader;
import net.zyski.zmacro.client.util.Resources;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import static com.mojang.text2speech.Narrator.LOGGER;
import static org.lwjgl.opengl.GL11C.GL_BLEND;
import static org.lwjgl.opengl.GL11C.glEnable;


public class ZmacroClient implements ClientModInitializer {

    private static ZmacroClient instance;
    public boolean blockMouseGrabbing = false;
    public KeyMapping OPEN_GUI = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("net.ZMacro.open_gui", InputConstants.Type.KEYSYM, InputConstants.KEY_EQUALS, "ZMacro")
    );
    ZMacro selected = null;
    File directory;
    private List<MacroWrapper> loadedMacros = new ArrayList<>();
    private List<MemoryMappedClassLoader> activeClassLoaders = new ArrayList<>();

    public static ZmacroClient getInstance() {
        return instance;
    }


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
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            GameChatEvent event = new GameChatEvent(message, overlay);
            if (selected != null && selected.isActive()) {
                selected.onChat(event);
            }
            return true;
        });

        ClientReceiveMessageEvents.ALLOW_CHAT.register((component, playerChatMessage, gameProfile, bound, instant) -> {
            PlayerChatEvent event = new PlayerChatEvent(component, playerChatMessage, gameProfile, bound, instant);
            if (selected != null && selected.isActive()) {
                selected.onChat(event);
            }
            return true;
        });
    }

    private void registerKeyBindThread() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_GUI.consumeClick()) {
                if (selected == null || !selected.isActive()) {
                    client.setScreen(new MacroSelectionScreen(loadedMacros, client.screen));
                } else if (selected != null && selected.isActive()) {
                    selected.toggle();
                }
            }
        });
    }

    private void registerTickThread() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (selected != null && selected.isActive())
                selected.loop();
        });
    }

    private void registerGraphicsThread() {
        HudLayerRegistrationCallback.EVENT.register(drawer ->
                drawer.addLayer(new IdentifiedLayer() {
                                    @Override
                                    public ResourceLocation id() {
                                        return ResourceLocation.fromNamespaceAndPath("zmacro", "macro_layer");
                                    }

                                    @Override
                                    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
                                        if (selected != null && selected.isActive()) {
                                            selected.onRender(guiGraphics);

                                            if (Minecraft.getInstance().screen == null) {
                                                Minecraft client = Minecraft.getInstance();
                                                glEnable(GL_BLEND);


                                                int spriteSize = 32;
                                                int x = (guiGraphics.guiWidth() / 2) - (spriteSize / 2);
                                                int y = guiGraphics.guiHeight() - 90;
                                                guiGraphics.blit(RenderType::guiTextured,
                                                        Resources.STOP_BUTTON,
                                                        x,
                                                        y,
                                                        0, 0, spriteSize, spriteSize, spriteSize, spriteSize
                                                );

                                                String keyText = "[" + OPEN_GUI.getTranslatedKeyMessage().getString() + "]";
                                                guiGraphics.drawString(
                                                        client.font,
                                                        keyText,
                                                        x + (spriteSize / 2) - (client.font.width(keyText) / 2),
                                                        y + (spriteSize / 2) - 4,
                                                        0xFFFFFF,
                                                        true
                                                );

                                                String stopText = "STOP MACRO";
                                                guiGraphics.drawString(
                                                        client.font,
                                                        stopText,
                                                        x + (spriteSize / 2) - (client.font.width(stopText) / 2),
                                                        y + 5 + (spriteSize) - 4,
                                                        0xFFFFFF,
                                                        true
                                                );

                                            }
                                        }
                                    }
                                }
                ));
    }

    private void registerCommands() {

        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> {
            dispatcher.register(
                    ClientCommandManager.literal("zmacro")
                            .then(ClientCommandManager.argument("args", StringArgumentType.greedyString())
                                    .executes(ctx -> {
                                        String allArgs = StringArgumentType.getString(ctx, "args");
                                        if(selected != null && selected.isActive())
                                            selected.onCommand(allArgs);
                                        return 1;
                                    }))
            );

            dispatcher.register(ClientCommandManager.literal("reloadmacros").executes(context -> {
                if (selected != null && selected.isActive()) {
                    message("Please stop macro before executing reload..");
                    return 1;
                }
                loadMacrosAsync(directory);
                return 1;
            }));

            dispatcher.register(ClientCommandManager.literal("stopmacro").executes(context -> {
                if (selected != null && selected.isActive()) {
                    message("Stopping Macro...");
                    selected.toggle();
                    selected = null;
                } else {
                    message("No Macro Running.");
                }
                return 1;
            }));
        }));
    }

    public void loadMacrosAsync(File directory) {
        Minecraft.getInstance().execute(() -> {
            if (Minecraft.getInstance().player != null) {
                message("Attempting to reload macros...");
            }
        });
        CompletableFuture.supplyAsync(() -> {
                    try {
                        loadMacrosFromFolder(directory);
                        return loadedMacros.size();
                    } catch (IOException e) {
                        throw new CompletionException(e);
                    }
                }, Executors.newCachedThreadPool())
                .whenCompleteAsync((count, throwable) -> {
                    if (throwable != null) {
                        message("Failed to load macros: " + throwable.getCause().getMessage());
                        LOGGER.error("Macro loading failed", throwable);
                    } else {
                        message("Macros loaded successfully! (" + count + ")");
                    }
                }, Minecraft.getInstance());
    }

    public void loadMacrosFromFolder(File macrosFolder) throws IOException {
        releaseAllMacros();

        File[] jarFiles = macrosFolder.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jarFiles == null) return;

        for (File jarFile : jarFiles) {
            byte[] jarBytes = Files.readAllBytes(jarFile.toPath());

            try (MemoryMappedClassLoader classLoader = new MemoryMappedClassLoader(
                    jarBytes,
                    jarFile.getName(),
                    getClass().getClassLoader()
            )) {
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
                loadedMacros.add(new MacroWrapper(
                        macro,
                        meta.name(),
                        meta.version(),
                        meta.author(),
                        meta.description(),
                        meta.icon()
                ));

            }
        } catch (Exception e) {
            message("Failed to process " + className);
        }
    }

    public void releaseAllMacros() {
        for (ClassLoader loader : activeClassLoaders) {
            if (loader instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) loader).close();
                } catch (Exception e) {
                    LOGGER.error("Failed to close classloader", e);
                }
            }
        }

        loadedMacros.clear();
        activeClassLoaders.clear();
    }

    private void message(String message) {
        if (Minecraft.getInstance().player != null)
            Minecraft.getInstance().player.displayClientMessage(Component.literal(message), false);
    }

    public boolean isSelectedActive() {
        return selected != null && selected.isActive();
    }

    public ZMacro getSelected() {
        return selected;
    }

    public void setSelected(ZMacro selected) {
        this.selected = selected;
        selected.toggle();
    }
}
