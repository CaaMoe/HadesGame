package moe.caa.fabric.hadesgame.mixin;

import moe.caa.fabric.hadesgame.event.OnHello;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ServerLoginNetworkHandler.class)
public abstract class MixinServerLoginNetworkHandler {
    @Inject(method = "onHello", at = @At("HEAD"), cancellable = true)
    private void onOnHello(LoginHelloC2SPacket packet, CallbackInfo ci) {
        if (OnHello.Companion.shouldCancel((ServerLoginNetworkHandler) (Object) this)) {
            ci.cancel();
        }
    }
}
