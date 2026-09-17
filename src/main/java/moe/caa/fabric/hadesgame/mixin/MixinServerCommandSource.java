package moe.caa.fabric.hadesgame.mixin;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CommandSourceStack.class)
public abstract class MixinServerCommandSource {

    @Redirect(method = "broadcastToAdmins", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;isOp(Lnet/minecraft/server/players/NameAndId;)Z"))
    private boolean redirectIsOperator(PlayerList playerList, NameAndId nameAndId) {
        return true;
    }
}