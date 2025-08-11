package net.zyski.zmacro.client.mixins;


import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.zyski.zmacro.client.ZmacroClient;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class ConnectionMixin {


    @Inject(
            method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void onGetPacket(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci){
        if(ZmacroClient.getInstance().isSelectedActive()){
            if(ZmacroClient.getInstance().getSelected().onPacketReceived(packet)){
                ci.cancel();
            }
        }
    }

    @Inject(
            method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V",
            at = @At("HEAD"),
            cancellable = true)
    private void onSendPacket(Packet<?> packet, @Nullable ChannelFutureListener channelfuturelistener, boolean flag, CallbackInfo ci){
        if(ZmacroClient.getInstance().isSelectedActive()){
            if(ZmacroClient.getInstance().getSelected().onPacketSent(packet)){
                ci.cancel();
            }
        }
    }

}
