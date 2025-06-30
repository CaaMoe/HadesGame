package moe.caa.fabric.hadesgame.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftDedicatedServer.class)
public class MixinMinecraftDedicatedServer {

    @Inject(method = "isSpawnProtected", at = @At("HEAD"), cancellable = true)
    private void onIsSpawnProtected(ServerWorld world, BlockPos pos, PlayerEntity player, CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(false);
        cir.cancel();
    }

}
