package moe.caa.fabric.hadesgame.handler

import moe.caa.fabric.hadesgame.GameCore
import moe.caa.fabric.hadesgame.stage.EndStage
import moe.caa.fabric.hadesgame.stage.GamingStage
import moe.caa.fabric.hadesgame.stage.InitStage
import moe.caa.fabric.hadesgame.stage.WaitReadyStage
import moe.caa.fabric.hadesgame.stage.WaitReadyStage.preparedPlayers
import moe.caa.fabric.hadesgame.util.resetState
import moe.caa.fabric.hadesgame.util.teleport
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.minecraft.entity.Entity
import net.minecraft.world.GameMode

object JoinLeaveHandler {
    fun setup() {

        ServerPlayerEvents.JOIN.register {
            preparedPlayers.remove(it.uuid)

            var player = it
            if (player.isDead) {
                player = GameCore.server.playerManager.respawnPlayer(
                    player,
                    true,
                    Entity.RemovalReason.CHANGED_DIMENSION
                )
            }

            player.teleport(InitStage.lobbySpawnLoc)
            player.resetState()

            when (GameCore.currentStage) {
                EndStage, GamingStage -> {
                    player.changeGameMode(GameMode.SPECTATOR)
                }

                InitStage, WaitReadyStage -> {
                    player.changeGameMode(GameMode.ADVENTURE)
                }
            }
        }

        ServerPlayerEvents.LEAVE.register {
            when (GameCore.currentStage) {
                GamingStage -> it.kill(it.world)
                else -> {}
            }
        }
    }
}