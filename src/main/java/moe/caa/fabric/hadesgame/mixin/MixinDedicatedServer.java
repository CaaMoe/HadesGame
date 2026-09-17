package moe.caa.fabric.hadesgame.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DedicatedServer.class)
public class MixinDedicatedServer {

    @Inject(method = "isUnderSpawnProtection", at = @At("HEAD"), cancellable = true)
    private void onIsUnderSpawnProtection(ServerLevel level, BlockPos pos, Player player, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
        cir.cancel();
    }

}