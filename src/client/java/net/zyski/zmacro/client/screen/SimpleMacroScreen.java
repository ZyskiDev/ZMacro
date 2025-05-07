package net.zyski.zmacro.client.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.zyski.zmacro.client.Macro.ZMacro;
import net.zyski.zmacro.client.ZmacroClient;
import net.zyski.zmacro.client.util.MacroMetadata;

import java.util.Map;

@Environment(EnvType.CLIENT)
public class SimpleMacroScreen extends Screen {
    private final Screen parent;
    private final Map<ZMacro, MacroMetadata> macros;

    public SimpleMacroScreen(Map<ZMacro, MacroMetadata> macros, Screen parent) {
        super(Component.literal("ZMacro Selector"));
        this.parent = parent;
        this.macros = macros;
    }

    @Override
    protected void init() {
        this.clearWidgets(); // Clear any existing widgets

        int buttonWidth = 200;
        int buttonHeight = 20;
        int startY = this.height / 4;
        int spacing = 24;
        int i = 0;

        // Add macro buttons
        for (Map.Entry<ZMacro, MacroMetadata> entry : macros.entrySet()) {
            final ZMacro macro = entry.getKey();
            this.addRenderableWidget(Button.builder(
                    Component.literal(entry.getValue().getName()),
                    button -> {
                       // macro.run();
                       ZmacroClient.getInstance().setSelected(macro);
                        this.onClose();
                    }
            ).bounds(
                    this.width / 2 - buttonWidth / 2,
                    startY + (i * spacing),
                    buttonWidth,
                    buttonHeight
            ).build());
            i++;
        }

        // Close button
        this.addRenderableWidget(Button.builder(
                Component.literal("Close"),
                button -> this.onClose()
        ).bounds(
                this.width / 2 - buttonWidth / 2,
                this.height - 30,
                buttonWidth,
                buttonHeight
        ).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(
                this.font,
                this.title,
                this.width / 2,
                20,
                0xFFFFFF
        );
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }
}