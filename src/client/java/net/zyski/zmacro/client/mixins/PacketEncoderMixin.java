package net.zyski.zmacro.client.mixins;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.zyski.zmacro.client.ZMacroClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;


@Mixin(PacketEncoder.class)
public class PacketEncoderMixin {

    @Shadow
    @Final
    private ProtocolInfo<?> protocolInfo;


    @Inject(
            method = "encode",
            at = @At("RETURN"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true)
    private void onPacketEncode(ChannelHandlerContext channelhandlercontext, Packet<?> packet, ByteBuf bytebuf, CallbackInfo ci) {
        if (packet == null) return;
        if (protocolInfo.flow() == PacketFlow.SERVERBOUND) {
            if(ZMacroClient.getInstance().isSelectedActive()){
                if(ZMacroClient.getInstance().getSelected().onPacketSent(packet)){
                    ci.cancel();
                }
            }
        }
    }

}
