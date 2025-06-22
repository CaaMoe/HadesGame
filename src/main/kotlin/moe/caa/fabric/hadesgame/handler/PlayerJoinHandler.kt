package moe.caa.fabric.hadesgame.handler

import moe.caa.fabric.hadesgame.GameCore
import moe.caa.fabric.hadesgame.event.OnHello
import moe.caa.fabric.hadesgame.event.networkHelloEvent
import moe.caa.fabric.hadesgame.stage.InitStage
import moe.caa.fabric.hadesgame.stage.WaitStage
import moe.caa.fabric.hadesgame.util.teleport
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import java.awt.Color

object PlayerJoinHandler {
    fun init() {
        networkHelloEvent.register {
            if (runCatching { InitStage.lobbyLoc() }.getOrNull() == null) {
                return@register OnHello.Result.KICK(
                    Text.literal("请稍后再试, 游戏尚未初始化完成!").withColor(Color.RED.rgb)
                )
            }
            return@register OnHello.Result.ALLOWED
        }

        ServerPlayerEvents.JOIN.register { onPlayerJoin(it) }
    }

    private fun onPlayerJoin(player: ServerPlayerEntity) {
        when (GameCore.currentStage) {
            InitStage -> {
                player.teleport(InitStage.lobbyLoc())
            }

            WaitStage -> {
                player.teleport(InitStage.lobbyLoc())
            }
        }
    }
}