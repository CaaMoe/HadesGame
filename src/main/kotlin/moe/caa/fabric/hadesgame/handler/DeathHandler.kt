package moe.caa.fabric.hadesgame.handler

import moe.caa.fabric.hadesgame.GameCore
import moe.caa.fabric.hadesgame.event.preDeathEvent
import moe.caa.fabric.hadesgame.mixin.LivingEntityAccessor
import moe.caa.fabric.hadesgame.stage.EndStage
import moe.caa.fabric.hadesgame.stage.GamingStage
import moe.caa.fabric.hadesgame.stage.InitStage
import moe.caa.fabric.hadesgame.stage.WaitReadyStage
import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.resetState
import moe.caa.fabric.hadesgame.util.teleport
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.GameMode

object DeathHandler {
    fun setup() {
        ServerLivingEntityEvents.ALLOW_DEATH.register { livingEntity, _, _ ->
            if (livingEntity !is ServerPlayerEntity) return@register true

            livingEntity.resetState()
            when (GameCore.currentStage) {
                EndStage, GamingStage -> {
                    livingEntity.changeGameMode(GameMode.SPECTATOR)
                }

                InitStage, WaitReadyStage -> {
                    livingEntity.changeGameMode(GameMode.ADVENTURE)
                    livingEntity.teleport(InitStage.lobbySpawnLoc)
                }
            }

            return@register false
        }

        preDeathEvent.register { livingEntity: LivingEntity, damageSource: DamageSource ->
            if (livingEntity !is ServerPlayerEntity) return@register false
            livingEntity as LivingEntityAccessor

            when (GameCore.currentStage) {
                EndStage, GamingStage -> {
                    livingEntity.changeGameMode(GameMode.SPECTATOR)
                    livingEntity.invokeDrop(livingEntity.world, damageSource)
                }

                InitStage, WaitReadyStage -> {
                    livingEntity.changeGameMode(GameMode.ADVENTURE)
                    livingEntity.teleport(InitStage.lobbySpawnLoc)
                }
            }
            livingEntity.resetState()
            livingEntity.damageTracker.deathMessage.broadcast()

            return@register true
        }
    }
}