package moe.caa.fabric.hadesgame.game.handler

import moe.caa.fabric.hadesgame.game.GameCore
import moe.caa.fabric.hadesgame.game.stage.EndStage
import moe.caa.fabric.hadesgame.game.stage.GamingStage
import moe.caa.fabric.hadesgame.game.stage.MapInitStage
import moe.caa.fabric.hadesgame.game.stage.WaitReadyStage
import moe.caa.fabric.hadesgame.util.resetState
import moe.caa.fabric.hadesgame.util.teleport
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.GameType

data object JoinLeaveHandler {
    fun init() {
        ServerPlayerEvents.JOIN.register {
            var player = it
            if (!player.isAlive) {
                player = GameCore.server.playerList.respawn(player, true, Entity.RemovalReason.CHANGED_DIMENSION)
            }

            player.teleport(MapInitStage.lobbySpawnLoc)
            player.resetState()

            when (GameCore.currentRunningStage) {
                EndStage, GamingStage -> {
                    player.setGameMode(GameType.SPECTATOR)
                }

                MapInitStage, WaitReadyStage -> {
                    player.setGameMode(GameType.ADVENTURE)
                }
            }
        }

        ServerPlayerEvents.LEAVE.register {
            when (GameCore.currentRunningStage) {
                GamingStage -> {
                    if (it.gameMode() != GameType.SPECTATOR) {
                        it.kill(it.level())
                    }
                }

                else -> {}
            }
        }
    }
}