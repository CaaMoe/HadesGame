package moe.caa.fabric.hadesgame.main

import com.mojang.brigadier.Command
import moe.caa.fabric.hadesgame.GameCore
import moe.caa.fabric.hadesgame.stage.WaitStage
import moe.caa.fabric.hadesgame.util.getPlayers
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.command.CommandManager.RegistrationEnvironment
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.text.Text
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class HadesGame : DedicatedServerModInitializer {
    private val logger: Logger = LoggerFactory.getLogger("HadesGame")

    override fun onInitializeServer() {
        // 启动游戏
        ServerLifecycleEvents.SERVER_STARTED.register {
            GameCore.setup(logger, it)
        }
        // 结束游戏
        ServerLifecycleEvents.SERVER_STOPPING.register {
            GameCore.stop()
        }

        // 注册命令
        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher, _, environment ->
            if (environment != RegistrationEnvironment.DEDICATED) {
                return@CommandRegistrationCallback
            }

            dispatcher.register(

                literal("game").then(literal("start").executes { context ->
                    if (GameCore.currentStage() == WaitStage) {
                        if (getPlayers().size <= 1) {
                            context.source.sendFeedback({ Text.literal("至少需要 2 人才能开启一局游戏.") }, true)
                        } else {
                            WaitStage.shouldStartGame = true
                            context.source.sendFeedback({ Text.literal("已提交开始游戏的指令") }, true)
                        }
                    } else {
                        context.source.sendFeedback({ Text.literal("请等待当局游戏结束才可开启新一局游戏") }, true)
                    }
                    return@executes Command.SINGLE_SUCCESS
                }).then(literal("autoStart").executes { context ->
                    WaitStage.autoStart = !WaitStage.autoStart

                    if (WaitStage.autoStart) {
                        context.source.sendFeedback(
                            { Text.literal("已设置为自动开启游戏, 当满 2 人时将自动开始游戏.") },
                            true
                        )
                    } else {
                        context.source.sendFeedback({ Text.literal("已关闭自动开启游戏") }, true)
                    }

                    return@executes Command.SINGLE_SUCCESS
                })
            )
        })
    }
}

