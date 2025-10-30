package net.zyski.zmacro.client.mixins;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.zyski.zmacro.client.ZMacroClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void onTravel(Vec3 travelVector, CallbackInfo ci) {
        if ((Object) this instanceof LocalPlayer) {
            if(ZMacroClient.getInstance().blockPlayerMovement || (ZMacroClient.getInstance().getSelected() != null && ZMacroClient.getInstance().isSelectedActive() && ZMacroClient.getInstance().getSelected().onPlayerMove(travelVector)))
                ci.cancel();
        }
    }

}
