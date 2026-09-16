package moe.caa.fabric.hadesgame.game.handler

import moe.caa.fabric.hadesgame.game.GameCore
import moe.caa.fabric.hadesgame.game.stage.EndStage
import moe.caa.fabric.hadesgame.game.stage.GamingStage
import moe.caa.fabric.hadesgame.game.stage.MapInitStage
import moe.caa.fabric.hadesgame.game.stage.WaitReadyStage
import moe.caa.fabric.hadesgame.mixin.LivingEntityAccessor
import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.resetState
import moe.caa.fabric.hadesgame.util.teleport
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.GameType

data object DamageAndDeathHandler {
    fun init() {

        // 控制伤害
        ServerLivingEntityEvents.ALLOW_DAMAGE.register { livingEntity, _, _ ->
            return@register livingEntity !is Player || when (GameCore.currentRunningStage) {
                EndStage -> false
                GamingStage -> GamingStage.invincible.not()
                MapInitStage -> false
                WaitReadyStage -> false
            }
        }

        // 控制死亡
        ServerLivingEntityEvents.ALLOW_DEATH.register { livingEntity, source, _ ->
            if (livingEntity !is ServerPlayer) return@register true
            livingEntity as LivingEntityAccessor


            when (GameCore.currentRunningStage) {
                GamingStage -> {
                    livingEntity.invokeDropAllDeathLoot(livingEntity.level(), source)

                    livingEntity.resetState()
                    livingEntity.setGameMode(GameType.SPECTATOR)
                    livingEntity.combatTracker.deathMessage.broadcast()
                }

                MapInitStage, WaitReadyStage -> {
                    livingEntity.resetState()
                    livingEntity.setGameMode(GameType.ADVENTURE)
                    livingEntity.teleport(MapInitStage.lobbySpawnLoc)
                }

                EndStage -> {}
            }

            return@register false
        }
    }
}