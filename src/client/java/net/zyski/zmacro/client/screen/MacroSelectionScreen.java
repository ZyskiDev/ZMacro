package net.zyski.zmacro.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FormattedCharSequence;
import net.zyski.zmacro.client.ZmacroClient;
import net.zyski.zmacro.client.util.MacroWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class MacroSelectionScreen extends Screen {
    private static final ResourceLocation DEFAULT_ICON = ResourceLocation.withDefaultNamespace("textures/item/paper.png");
    private final Screen parent;
    private MacroListWidget macroList;
    private LinkedList<MacroWrapper> wrappers;
    public MacroSelectionScreen(LinkedList<MacroWrapper> macros, Screen parent) {
        super(Component.literal("Macro Selector"));
        this.parent = parent;
        this.wrappers = macros;
    }

    @Override
    protected void init() {
        this.macroList = new MacroListWidget(
                Minecraft.getInstance(),
                this.width,
                this.height,
                this
        );
        wrappers.forEach((wrapper) ->
                macroList.addMacroEntry(wrapper)
        );

        this.addRenderableWidget(macroList);

    }


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        this.macroList.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(
                this.font,
                this.title,
                this.width / 2,
                12,
                0xFFFFFF
        );
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    public static class MacroListWidget extends ObjectSelectionList<MacroListWidget.Entry> {
        private static final int ENTRY_WIDTH = 400;
        private static final int ICON_SIZE = 16;
        private static final int PADDING = 4;

        private final MacroSelectionScreen parentScreen;

        public MacroListWidget(Minecraft client, int width, int height, MacroSelectionScreen parentScreen) {
            super(client, width, height, 33, 36, (int) (9.0F * 1.5F));
            this.parentScreen = parentScreen;
        }

        public void addMacroEntry(MacroWrapper wrapper) {
            this.addEntry(new Entry(wrapper));
        }

        protected int getScrollbarPosition() {
            return this.width / 2 + 160;
        }


        @Override
        public void onClick(double d, double e) {
            super.onClick(d, e);
        }

        @Override
        public int getRowWidth() {
            return ENTRY_WIDTH * 2;
        }

        public class Entry extends ObjectSelectionList.Entry<Entry> {
            private final MacroWrapper macro;
            private final Minecraft minecraft = Minecraft.getInstance();
            private ResourceLocation fetchedIcon;

            public Entry(MacroWrapper wrapper) {
                this.macro = wrapper;
                ResourceLocation temp = ResourceLocation.withDefaultNamespace(macro.getIcon());
                ResourceManager manager = Minecraft.getInstance().getResourceManager();
                if (manager.getResource(temp).isPresent()) {
                    fetchedIcon = temp;
                }
            }


            @Override
            public void render(GuiGraphics gui, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean hovered, float delta) {
                int centerX = (parentScreen.width - ENTRY_WIDTH) / 2;

                if (hovered || this.equals(getSelected())) {
                    gui.fill(centerX - 2, top - 2, centerX + ENTRY_WIDTH + 2, top + height + 2, 0x33FFFFFF);
                }


                gui.blit(RenderType::guiTextured, (fetchedIcon != null ? fetchedIcon : DEFAULT_ICON), centerX + PADDING, top + PADDING, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);


                int textLeft = centerX + ICON_SIZE + PADDING * 2;
                int textWidth = ENTRY_WIDTH - ICON_SIZE - PADDING * 3;
                Font font = minecraft.font;


                MutableComponent name = Component.literal(macro.getName() + "    " + macro.getVersion()).setStyle(Style.EMPTY.withBold(true));
                gui.drawString(font, name, textLeft, top + PADDING, 0xFFFFFF, false);


                List<FormattedCharSequence> descriptionLines = font.split(
                        Component.literal(macro.getDescription()).withStyle(Style.EMPTY.withColor(0xAAAAAA)),
                        textWidth
                );

                if (!descriptionLines.isEmpty()) {
                    gui.drawString(font, descriptionLines.getFirst(), textLeft, top + PADDING + 12, 0xAAAAAA, false);
                }
            }


            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (button == 0) {
                    ZmacroClient.getInstance().setSelected(this.macro.getPath());
                    Minecraft.getInstance().setScreen(null);
                    return true;
                }
                return false;
            }

            @Override
            public @NotNull Component getNarration() {
                return Component.literal(macro.getName() + ": " + macro.getDescription());
            }
        }
    }
}
