package moe.caa.fabric.hadesgame.game.handler

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import moe.caa.fabric.hadesgame.game.GameCore
import moe.caa.fabric.hadesgame.game.stage.GamingStage
import moe.caa.fabric.hadesgame.game.stage.WaitReadyStage
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.suggestions.SuggestionContext
import net.minecraft.server.permissions.PermissionCheck
import net.minecraft.server.permissions.Permissions

data object CommandHandler {
    fun init() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                Commands.literal("game")
                    .then(Commands.literal("start").executes(::executeStart))
                    .then(
                        Commands.literal("call")
                            .then(
                                Commands.argument("name", StringArgumentType.string())
                                    .suggests { _, builder ->
                                        SuggestionContext.suggest(buildList {
                                            GamingStage.allGameEvents.forEach { add(it.javaClass.simpleName.lowercase()) }
                                        }, builder)
                                    }
                                    .requires(Commands.hasPermission(PermissionCheck.Require(Permissions.COMMANDS_GAMEMASTER)))
                                    .executes(::executeCall)
                            )
                    )
            )
        }
    }

    private fun executeStart(context: CommandContext<CommandSourceStack>): Int {
        val waitStage = GameCore.currentRunningStage as? WaitReadyStage ?: return 0
        if (waitStage.triggerToStartCountdown()) {
            return Command.SINGLE_SUCCESS
        }

        return 0
    }


    private fun executeCall(context: CommandContext<CommandSourceStack>): Int {
        if (GameCore.currentRunningStage !is GamingStage) return 0
        val name = StringArgumentType.getString(context, "name")!!

        val event = GamingStage.allGameEvents.firstOrNull {
            it.javaClass.simpleName.equals(name, ignoreCase = true)
        } ?: return 0

        event.call()

        return Command.SINGLE_SUCCESS
    }
}