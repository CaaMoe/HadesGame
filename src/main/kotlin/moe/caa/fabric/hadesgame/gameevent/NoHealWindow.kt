package moe.caa.fabric.hadesgame.gameevent

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.caa.fabric.hadesgame.GameCore
import moe.caa.fabric.hadesgame.event.entityHealEvent
import moe.caa.fabric.hadesgame.stage.GamingStage
import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.broadcastOverlay
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.level.GameType
import java.awt.Color

data object NoHealWindow : AbstractGameEvent() {
    override val eventName = "禁疗时刻"

    private var activeJob: Job? = null

    override fun initEvent() {
        entityHealEvent.register { livingEntity, amount ->
            if (activeJob?.isActive != true) return@register true
            if (GameCore.currentStage != GamingStage) return@register true
            if (amount <= 0.0f) return@register true

            val player = livingEntity as? ServerPlayer ?: return@register true
            return@register player.gameMode() == GameType.SPECTATOR
        }
    }

    override suspend fun callEvent() {
        activeJob?.cancel()

        activeJob = GameCore.coroutineScope.launch {
            delay(30_000L)
            Component.literal("禁疗时刻已结束").withColor(Color.RED.rgb).broadcastOverlay()
            SoundEvents.VILLAGER_NO.broadcast(1.0F, 1.0F)
        }

        Component.literal("禁疗时刻已生效").withColor(Color.GREEN.rgb).broadcast()
        Component.literal("30 秒内所有治疗都会失效").withColor(Color.RED.rgb).broadcastOverlay()
        SoundEvents.BEACON_ACTIVATE.broadcast(1.0F, 0.5F)
    }

    override suspend fun endEvent() {
        activeJob?.cancel()
    }
}
