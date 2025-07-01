package net.zyski.zmacro.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.zyski.zmacro.client.Macro.Macro;
import net.zyski.zmacro.client.Macro.ZMacro;
import net.zyski.zmacro.client.chat.GameChatEvent;
import net.zyski.zmacro.client.chat.PlayerChatEvent;
import net.zyski.zmacro.client.screen.MacroSelectionScreen;
import net.zyski.zmacro.client.util.MacroWrapper;
import net.zyski.zmacro.client.util.MemoryMappedClassLoader;
import net.zyski.zmacro.client.util.Resources;
import net.zyski.zmacro.client.util.SleepUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

import static org.lwjgl.opengl.GL11C.GL_BLEND;
import static org.lwjgl.opengl.GL11C.glEnable;


public class ZmacroClient implements ClientModInitializer {

    private static ZmacroClient instance;
    public boolean blockMouseGrabbing = false;
    public KeyMapping OPEN_GUI = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("net.ZMacro.open_gui", InputConstants.Type.KEYSYM, InputConstants.KEY_EQUALS, "ZMacro")
    );
    ZMacro selected = null;
    MemoryMappedClassLoader selectedLoader = null;
    File directory;

    public static ZmacroClient getInstance() {
        return instance;
    }


    @Override
    public void onInitializeClient() {
        directory = Minecraft.getInstance().gameDirectory.toPath().resolve("ZMacro").toFile();
        directory.mkdir();
        registerCommands();
        registerGraphicsThread();
        registerTickThread();
        registerKeyBindThread();
        registerChatListenerThread();
        registerEntityLoadThread();
        registerScreenEventsThread();
        instance = this;
    }

    private void registerScreenEventsThread() {
        ScreenEvents.BEFORE_INIT.register(new ScreenEvents.BeforeInit() {
            @Override
            public void beforeInit(Minecraft minecraft, Screen screen, int width, int height) {
                if (selected != null && selected.isActive()) {
                    selected.onScreenPreInit(minecraft, screen, width, height);
                }
            }
        });

        ScreenEvents.AFTER_INIT.register(new ScreenEvents.AfterInit() {
            @Override
            public void afterInit(Minecraft minecraft, Screen screen, int width, int height) {
                if (selected != null && selected.isActive()) {
                    selected.onScreenPostInit(minecraft, screen, width, height);
                }
            }
        });
    }

    private void registerEntityLoadThread() {
        ClientEntityEvents.ENTITY_LOAD.register(new ClientEntityEvents.Load() {
            @Override
            public void onLoad(Entity entity, ClientLevel clientLevel) {
                if (selected != null && selected.isActive()) {
                    selected.onEntityLoad(entity, clientLevel);
                }
            }
        });

        ClientEntityEvents.ENTITY_UNLOAD.register(new ClientEntityEvents.Unload() {
            @Override
            public void onUnload(Entity entity, ClientLevel clientLevel) {
                if (selected != null && selected.isActive()) {
                    selected.onEntityUnload(entity, clientLevel);
                }
            }
        });
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
                    try {
                        client.setScreen(new MacroSelectionScreen(getMacroIndexes(directory), client.screen));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else if (selected != null && selected.isActive()) {
                    selected.toggle();
                }
            }
        });
    }

    private void registerTickThread() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (selected != null && selected.isActive()) {
                if (SleepUtil.isSleeping() && SleepUtil.getBreakSleepCondition() != null && SleepUtil.getSleepTimeout() > 0) {
                    if ((SleepUtil.getBreakSleepCondition().getAsBoolean()) || (System.currentTimeMillis() > SleepUtil.getSleepTimeout())) {
                        selected.stopSleep();
                    }
                } else {
                    selected.loop();
                }

            }
        });
    }

    private void registerGraphicsThread() {

        WorldRenderEvents.LAST.register(new WorldRenderEvents.Last() {
            @Override
            public void onLast(WorldRenderContext context) {
                if (selected != null && selected.isActive()) {
                    selected.onWorldRender(context);
                }
            }
        });

        HudLayerRegistrationCallback.EVENT.register(drawer ->
                drawer.addLayer(new IdentifiedLayer() {
                                    @Override
                                    public ResourceLocation id() {
                                        return ResourceLocation.fromNamespaceAndPath("zmacro", "macro_layer");
                                    }

                                    @Override
                                    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
                                        if (selected != null && selected.isActive()) {
                                            selected.onHUDRender(guiGraphics);

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
                                        if (selected != null && selected.isActive())
                                            selected.onCommand(allArgs);
                                        return 1;
                                    }))
            );

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

    public LinkedList<MacroWrapper> getMacroIndexes(File directory) throws IOException {
        LinkedList<MacroWrapper> wrappers = new LinkedList<>();
        File[] jarFiles = directory.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jarFiles == null) return wrappers;
        for (File jarFile : jarFiles) {
            try (JarFile jar = new JarFile(jarFile)) {
                JarEntry metadata = jar.getJarEntry("META-INF/macro");

                if (metadata != null) {
                    Properties props = new Properties();
                    try (InputStream is = jar.getInputStream(metadata)) {
                        props.load(is);
                        wrappers.add(new MacroWrapper(
                                jarFile.toPath(),
                                props.getProperty("name"),
                                props.getProperty("version"),
                                props.getProperty("author"),
                                props.getProperty("description"),
                                props.getProperty("icon")
                        ));
                        continue;
                    }
                }
            } catch (Exception e) {

            }
            byte[] jarBytes = Files.readAllBytes(jarFile.toPath());
            try (MemoryMappedClassLoader classLoader = new MemoryMappedClassLoader(
                    jarBytes,
                    jarFile.getName(),
                    getClass().getClassLoader()
            )) {
                try (JarInputStream jarStream = new JarInputStream(new ByteArrayInputStream(jarBytes))) {
                    JarEntry entry;
                    while ((entry = jarStream.getNextJarEntry()) != null) {
                        if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                            String className = entry.getName()
                                    .replace(".class", "")
                                    .replace("/", ".");

                            try {
                                Class<?> cls = classLoader.loadClass(className);
                                if (cls.isAnnotationPresent(Macro.class) && ZMacro.class.isAssignableFrom(cls)) {
                                    Macro meta = cls.getAnnotation(Macro.class);
                                    wrappers.add(new MacroWrapper(
                                            jarFile.toPath(),
                                            meta.name(),
                                            meta.version(),
                                            meta.author(),
                                            meta.description(),
                                            meta.icon()
                                    ));
                                }
                            } catch (Exception e) {
                                System.err.println("Failed to process " + className + ": " + e.getMessage());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to process JAR: " + jarFile.getName() + ": " + e.getMessage());
            }
        }
        return wrappers;
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


    public void setSelected(Path path) {
        if (selectedLoader != null)
            selectedLoader.close();

        try {
            byte[] jarBytes = Files.readAllBytes(path);
            MemoryMappedClassLoader classLoader = new MemoryMappedClassLoader(
                    jarBytes,
                    path.getFileName().toString(),
                    getClass().getClassLoader());
            JarInputStream jarStream = new JarInputStream(new ByteArrayInputStream(jarBytes));

            JarEntry entry;
            while ((entry = jarStream.getNextJarEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                    String className = entry.getName()
                            .replace(".class", "")
                            .replace("/", ".");

                    try {
                        Class<?> cls = classLoader.loadClass(className);
                        if (ZMacro.class.isAssignableFrom(cls)) {
                            selected = (ZMacro) cls.getDeclaredConstructor().newInstance();
                            selectedLoader = classLoader;
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (selected != null)
            selected.toggle();
    }
}
