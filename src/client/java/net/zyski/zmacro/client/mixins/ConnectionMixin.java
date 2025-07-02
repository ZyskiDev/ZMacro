package net.zyski.zmacro.client.mixins;


import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.zyski.zmacro.client.ZmacroClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class ConnectionMixin {
    @Inject(
            method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V",
            at = @At("HEAD")
    )
    private void onPacket(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci){
        if(ZmacroClient.getInstance().isSelectedActive()){
            ZmacroClient.getInstance().getSelected().onPacket(packet);
        }
    }

}
