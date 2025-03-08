package moe.caa.fabric.hadesgame.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerCommandSource.class)
public abstract class MixinServerCommandSource {

    // Redirect the isOperator check to always return true
    @Redirect(method = "sendToOps", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;isOperator(Lcom/mojang/authlib/GameProfile;)Z"))
    private boolean redirectIsOperator(final PlayerManager playerManager, final GameProfile gameProfile) {
        return true;
    }
}
