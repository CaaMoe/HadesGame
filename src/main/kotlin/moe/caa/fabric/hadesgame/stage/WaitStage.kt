package moe.caa.fabric.hadesgame.stage

import moe.caa.fabric.hadesgame.GameCore
import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.resetState
import moe.caa.fabric.hadesgame.util.teleport
import net.minecraft.text.Text
import net.minecraft.world.GameMode
import java.awt.Color

object WaitStage : AbstractStage() {
    override val stageName = "等待开始指令"
    var autoStart = false
    var shouldStartGame = false
    private var tick = 0

    val tipStart: Text = Text.empty()
        .append(Text.literal("执行 ").withColor(Color.GRAY.rgb))
        .append(Text.literal("/game start").withColor(Color.WHITE.rgb))
        .append(Text.literal(" 指令可开启一局新游戏.").withColor(Color.GRAY.rgb))
    val tipAutoStart: Text = Text.empty()
        .append(Text.literal("执行 ").withColor(Color.GRAY.rgb))
        .append(Text.literal("/game autoStart").withColor(Color.WHITE.rgb))
        .append(Text.literal(" 指令可设置足人时自动开始游戏.").withColor(Color.GRAY.rgb))

    override suspend fun startStage() {
        tick = 0
        shouldStartGame = false
        GameCore.server.playerManager.playerList.forEach { player ->
            player.teleport(InitStage.lobbySpawnLocation())
            player.changeGameMode(GameMode.ADVENTURE)
            player.resetState()
        }
    }

    override suspend fun tickStage() {
        if (tick++ % 20 == 0) {
            if ((tick / 100 % 2) == 0) {
                tipStart.broadcast()
            } else {
                tipAutoStart.broadcast()
            }
        }
    }

    override suspend fun shouldEndStage(): Boolean {
        if (autoStart) return true
        return shouldStartGame
    }

    override suspend fun endStage() {
        GameCore.server.playerManager.playerList.forEach { player ->
            player.teleport(InitStage.lobbySpawnLocation())
            player.changeGameMode(GameMode.ADVENTURE)
            player.resetState()
        }
    }
}