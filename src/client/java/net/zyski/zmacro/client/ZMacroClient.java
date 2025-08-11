package net.zyski.zmacro.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
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


public class ZMacroClient implements ClientModInitializer {

    private static ZMacroClient instance;
    public boolean blockMouseGrabbing = false;
    public KeyMapping OPEN_GUI = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("Open GUI", InputConstants.Type.KEYSYM, InputConstants.KEY_EQUALS, "ZMacro")
    );
    ZMacro selected = null;
    MemoryMappedClassLoader selectedLoader = null;
    File directory;

    public static ZMacroClient getInstance() {
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
        registerToolTipThread();
        instance = this;
    }


    private void registerToolTipThread(){
        ItemTooltipCallback.EVENT.register((itemStack, tooltipContext, tooltipFlag, list) -> {
            if (selected != null && selected.isActive()) {
                selected.onToolTipCallBack(itemStack, tooltipContext, tooltipFlag, list);
            }
        });
    }


    private void registerScreenEventsThread() {
        ScreenEvents.BEFORE_INIT.register((minecraft, screen, width, height) -> {
            if (selected != null && selected.isActive()) {
                selected.onScreenPreInit(minecraft, screen, width, height);
            }
        });

        ScreenEvents.AFTER_INIT.register((minecraft, screen, width, height) -> {
            if (selected != null && selected.isActive()) {
                selected.onScreenPostInit(minecraft, screen, width, height);
            }
        });
    }

    private void registerEntityLoadThread() {
        ClientEntityEvents.ENTITY_LOAD.register((entity, clientLevel) -> {
            if (selected != null && selected.isActive()) {
                selected.onEntityLoad(entity, clientLevel);
            }
        });

        ClientEntityEvents.ENTITY_UNLOAD.register((entity, clientLevel) -> {
            if (selected != null && selected.isActive()) {
                selected.onEntityUnload(entity, clientLevel);
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

        WorldRenderEvents.LAST.register(worldRenderContext -> {
            if (selected != null && selected.isActive()) {
                selected.onWorldRender(worldRenderContext);
            }
        });


        HudElementRegistry.addLast(ResourceLocation.fromNamespaceAndPath("zmacro", "macro_layer"), (guiGraphics, tickDelta) -> {
            Minecraft client = Minecraft.getInstance();

            if (selected != null && selected.isActive()) {
                selected.onHUDRender(guiGraphics);

                if (client.screen == null) {
                    glEnable(GL_BLEND);

                    int spriteSize = 32;
                    int x = (guiGraphics.guiWidth() / 2) - (spriteSize / 2);
                    int y = guiGraphics.guiHeight() - 90;

                    guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                            Resources.STOP_BUTTON,
                            x, y,
                            0, 0,
                            spriteSize, spriteSize,
                            spriteSize, spriteSize
                    );

                    String keyText = "[" + OPEN_GUI.getTranslatedKeyMessage().getString() + "]";
                    guiGraphics.drawString(
                            client.font,
                            keyText,
                            x + (spriteSize / 2) - (client.font.width(keyText) / 2),
                            y + (spriteSize / 2) - 4,
                            0xFFFFFFFF,
                            true
                    );

                    String stopText = "STOP MACRO";
                    guiGraphics.drawString(
                            client.font,
                            stopText,
                            x + (spriteSize / 2) - (client.font.width(stopText) / 2),
                            y + 5 + spriteSize - 4,
                            0xFFFFFFFF,
                            true
                    );
                }
            }
        });
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
            } catch (Exception ignored) {

            }
            byte[] jarBytes = Files.readAllBytes(jarFile.toPath());
            try (MemoryMappedClassLoader classLoader = new MemoryMappedClassLoader(
                    jarBytes,
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
                    } catch (Exception ignored) {

                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (selected != null)
            selected.toggle();
    }

    public File getDirectory() {
        return directory;
    }
}
