package moe.caa.fabric.hadesgame.mixin;

import moe.caa.fabric.hadesgame.event.OnHello;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class MixinServerLoginPacketListenerImpl {

    @Inject(method = "handleHello", at = @At("HEAD"), cancellable = true)
    private void onOnHello(ServerboundHelloPacket packet, CallbackInfo ci) {
        if (OnHello.Companion.shouldCancel((ServerLoginPacketListenerImpl) (Object) this)) {
            ci.cancel();
        }
    }
}
