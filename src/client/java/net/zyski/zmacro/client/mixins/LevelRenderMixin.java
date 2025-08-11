package net.zyski.zmacro.client.mixins;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.zyski.zmacro.client.ZMacroClient;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRenderMixin {


    @Inject(at = @At("RETURN"),
            method = "renderLevel")
    private void postRender(GraphicsResourceAllocator allocator, DeltaTracker tickCounter, boolean blockOutline, Camera camera, Matrix4f positionMatrix, Matrix4f projectionMatrix, GpuBufferSlice fog, Vector4f fogColor, boolean shouldRenderSky, CallbackInfo ci)
    {

        if(ZMacroClient.getInstance().getSelected() != null && ZMacroClient.getInstance().isSelectedActive())
            ZMacroClient.getInstance().getSelected().postLevelRender(allocator, tickCounter, blockOutline, camera, positionMatrix, projectionMatrix, fog, fogColor, shouldRenderSky);
    }

    @Inject(at = @At("HEAD"),
            method = "renderLevel")
    private void preRender(GraphicsResourceAllocator allocator, DeltaTracker tickCounter, boolean blockOutline, Camera camera, Matrix4f positionMatrix, Matrix4f projectionMatrix, GpuBufferSlice fog, Vector4f fogColor, boolean shouldRenderSky, CallbackInfo ci)
    {
        if(ZMacroClient.getInstance().getSelected() != null && ZMacroClient.getInstance().isSelectedActive())
            ZMacroClient.getInstance().getSelected().preLevelRender(allocator, tickCounter, blockOutline, camera, positionMatrix, projectionMatrix, fog, fogColor, shouldRenderSky);
    }
}
