package moe.caa.fabric.hadesgame.game.handler

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import moe.caa.fabric.hadesgame.game.GameCore
import moe.caa.fabric.hadesgame.game.stage.WaitReadyStage
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

data object CommandHandler {
    fun init() {
        GameCore.server.commands.dispatcher.register(
            Commands.literal("game")
                .then(Commands.literal("start").executes(::executeStart))
        )
    }

    private fun executeStart(context: CommandContext<CommandSourceStack>): Int {
        val waitStage = GameCore.currentRunningStage as? WaitReadyStage ?: return 0
        if (waitStage.triggerToStartCountdown()) {
            return Command.SINGLE_SUCCESS
        }

        return 0
    }
}