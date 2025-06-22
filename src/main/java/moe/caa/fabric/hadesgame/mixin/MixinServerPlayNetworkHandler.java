package moe.caa.fabric.hadesgame.mixin;

import moe.caa.fabric.hadesgame.event.OnSneakStateChange;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class MixinServerPlayNetworkHandler {

    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onPlayerInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;setSneaking(Z)V"))
    private void onSetSneaking(PlayerInputC2SPacket packet, CallbackInfo ci) {
        if (player.isSneaking() != packet.input().sneak()) {
            OnSneakStateChange.Companion.trigger(player, packet.input().sneak());
        }
    }
}
