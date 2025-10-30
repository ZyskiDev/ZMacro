package net.zyski.zmacro.client.Macro;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.Vec3;
import net.zyski.zmacro.client.ZMacroClient;
import net.zyski.zmacro.client.api.ZAPI;
import net.zyski.zmacro.client.chat.ChatEvent;
import net.zyski.zmacro.client.util.SleepUtil;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.time.Instant;
import java.util.List;
import java.util.function.BooleanSupplier;

public interface ZMacro {

    public ZAPI api = new ZAPI();

    void start();

    void loop();

    void stop();

    void toggle();

    boolean isActive();

    default void sleep(long timeout) {
        sleep(() -> false, timeout);
    }

    default void sleep(BooleanSupplier condition, long interval) {
        SleepUtil.setSleepTimeout(System.currentTimeMillis() + interval);
        SleepUtil.setBreakSleepCondition(condition);
        SleepUtil.setSleeping(true);
    }

    default void stopSleep() {
        SleepUtil.setSleeping(false);
        SleepUtil.setSleepTimeout(0);
        SleepUtil.setBreakSleepCondition(null);
    }

    default void onHUDRender(GuiGraphics graphics) {

    }

    default void onScreenRender(Screen screen, GuiGraphics gui, int mouseX, int mouseY, float delta){

    }

    default void onWorldRender(WorldRenderContext context) {

    }

    default void onScreenPreInit(Minecraft minecraft, Screen screen, int width, int height) {

    }

    default void onScreenPostInit(Minecraft minecraft, Screen screen, int width, int height) {

    }

    default void onKeyPress(int keyCode, int modifiers, int scanCode) {

    }

    default void onEntityLoad(Entity enttiy, ClientLevel clientLevel) {

    }

    default void onEntityUnload(Entity enttiy, ClientLevel clientLevel) {

    }

    default void onChat(ChatEvent chatEvent) {

    }

    default void onCommand(String command) {

    }

    default boolean onPacketReceived(Packet<?> packet){
        return false;
    }


    default boolean onPacketSent(Packet<?> packet){
        return false;
    }

    default void onToolTipCallBack(ItemStack itemStack, Item.TooltipContext tooltipContext, TooltipFlag tooltipFlag, List<Component> list){

    }

    default void message(String text) {
        Minecraft.getInstance().gui.getChat().addMessage(Component.literal(text));
        ChatLog chatlog = Minecraft.getInstance().getReportingContext().chatLog();
        chatlog.push(LoggedChatMessage.system(Component.literal(text), Instant.now()));
    }

    default void setMouseGrab(boolean shouldGrab) {
        ZMacroClient.getInstance().blockMouseGrabbing = !shouldGrab;
    }


    default void postLevelRender(GraphicsResourceAllocator allocator, DeltaTracker tickCounter, boolean blockOutline, Camera camera, Matrix4f positionMatrix, Matrix4f projectionMatrix, GpuBufferSlice fog, Vector4f fogColor, boolean shouldRenderSky) {


    }

    default void preLevelRender(GraphicsResourceAllocator allocator, DeltaTracker tickCounter, boolean blockOutline, Camera camera, Matrix4f positionMatrix, Matrix4f projectionMatrix, GpuBufferSlice fog, Vector4f fogColor, boolean shouldRenderSky) {


    }

    default boolean onPlayerMove(Vec3 travelVec){
        return false;
    }
}
