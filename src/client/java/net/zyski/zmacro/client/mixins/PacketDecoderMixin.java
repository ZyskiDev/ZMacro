package net.zyski.zmacro.client.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.zyski.zmacro.client.ZMacroClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Random;

@Mixin(PacketDecoder.class)
public class PacketDecoderMixin {

    @Shadow @Final private ProtocolInfo<?> protocolInfo;
    private static int profileId = -1;


    @Inject(
            method = "decode",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/protocol/Packet;type()Lnet/minecraft/network/protocol/PacketType;",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true
    )
    private void onPacketDecoded(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out, CallbackInfo ci, @Local Packet<?> packet) {
        if (packet == null) return;
        if (protocolInfo.flow() == PacketFlow.CLIENTBOUND) {
            if(ZMacroClient.getInstance().isSelectedActive()) {
                if (packet instanceof ClientboundContainerSetContentPacket p) {
                    if (p.containerId() == profileId) {
                        boolean found = false;
                        for (ItemStack item : p.items()) {
                            if (item.is(Items.OAK_DOOR) && item.getCustomName() != null && item.getCustomName().getString().contains("First Join Date")) {
                                ItemLore lore = item.getOrDefault(DataComponents.LORE, ItemLore.EMPTY);
                                for (Component line : lore.lines()) {
                                    if (line.getString().contains("Firefly")) {
                                        ZMacroClient.checkDimension("firefly");
                                        found = true;
                                        break;
                                    } else if (line.getString().contains("Honeybee")) {
                                        ZMacroClient.checkDimension("honeybee");
                                        found = true;
                                        break;
                                    }
                                }
                            }
                            if(found) break;
                        }
                        Minecraft.getInstance().getConnection().send(new ServerboundContainerClosePacket(profileId));
                        profileId = -1;
                    }
                } else if (packet instanceof ClientboundOpenScreenPacket p) {
                    if (p.getTitle().getString().contains("My Profile")) {
                        profileId = p.getContainerId();
                        ci.cancel();
                    }
                } else if (packet instanceof ClientboundSystemChatPacket p) {
                    if (p.content().getString().contains("can't use /profile here")) {
                        ZMacroClient.checkDimension("hub");
                        ci.cancel();
                    }else if (p.content().getString().contains("[+] " + Minecraft.getInstance().getUser().getName() +" has logged in.")){
                        ZMacroClient.updateDimensionPing(new Random().nextInt(10001) + 5000);
                    }
                }
            }

            if(ZMacroClient.getInstance().isSelectedActive()){
                if(ZMacroClient.getInstance().getSelected().onPacketReceived(packet)){
                    out.remove(packet);
                    ci.cancel();
                }
            }
        }
    }
}
