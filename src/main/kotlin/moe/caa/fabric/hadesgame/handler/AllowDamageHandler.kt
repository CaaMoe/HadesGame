package moe.caa.fabric.hadesgame.handler

import moe.caa.fabric.hadesgame.GameCore
import moe.caa.fabric.hadesgame.stage.EndStage
import moe.caa.fabric.hadesgame.stage.GamingStage
import moe.caa.fabric.hadesgame.stage.InitStage
import moe.caa.fabric.hadesgame.stage.WaitReadyStage
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.minecraft.server.network.ServerPlayerEntity

object AllowDamageHandler {
    fun setup() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register { livingEntity, _, _ ->
            if (livingEntity !is ServerPlayerEntity) return@register true
            return@register when (GameCore.currentStage) {
                EndStage -> false
                GamingStage -> GamingStage.invincibleCountdown <= 0
                InitStage -> false
                WaitReadyStage -> false
            }
        }
    }
}