package moe.caa.fabric.hadesgame.gameevent

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.caa.fabric.hadesgame.GameCore
import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.broadcastOverlay
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.level.border.WorldBorder
import java.awt.Color
import kotlin.math.abs
import kotlin.math.max

data object CloseEncounter : AbstractGameEvent() {
    override val eventName = "狭路相逢"

    private const val effectDurationMillis = 30_000L
    private const val effectDurationTicks = 30L * 20L
    private const val minBorderSize = 3.0
    private const val shrinkRatio = 0.7

    private var activeJob: Job? = null
    private val borderPlanMap = LinkedHashMap<ServerLevel, BorderPlan>()

    override suspend fun callEvent() {
        activeJob?.cancel()
        restoreBorders()
        accelerateBorders()

        activeJob = GameCore.coroutineScope.launch {
            delay(effectDurationMillis)
            restoreBorders()
            Component.literal("狭路相逢已结束").withColor(Color.RED.rgb).broadcastOverlay()
            SoundEvents.VILLAGER_NO.broadcast(1.0F, 1.0F)
        }

        Component.literal("狭路相逢已生效").withColor(Color.GREEN.rgb).broadcast()
        Component.literal("边界正在急速收缩, 准备贴脸交战").withColor(Color.ORANGE.rgb).broadcastOverlay()
        SoundEvents.BEACON_ACTIVATE.broadcast(1.0F, 0.9F)
    }

    override suspend fun endEvent() {
        activeJob?.cancel()
        restoreBorders()
    }

    private fun accelerateBorders() {
        borderPlanMap.clear()

        for (world in GameCore.server.allLevels) {
            val border = world.worldBorder
            val currentSize = border.size
            val originalTarget = border.lerpTarget
            val originalTime = border.lerpTime

            borderPlanMap[world] = BorderPlan(
                targetSize = originalTarget,
                remainingTime = originalTime,
                capturedAtMillis = System.currentTimeMillis()
            )

            val acceleratedTarget = calculateAcceleratedTarget(currentSize, originalTarget)
            if (acceleratedTarget >= currentSize - 0.01) continue

            border.lerpSizeBetween(currentSize, acceleratedTarget, effectDurationTicks, 0L)
        }
    }

    private fun restoreBorders() {
        if (borderPlanMap.isEmpty()) return

        val cachedPlans = borderPlanMap.toMap()
        borderPlanMap.clear()

        for ((world, plan) in cachedPlans) {
            plan.restore(world.worldBorder)
        }
    }

    private fun calculateAcceleratedTarget(currentSize: Double, originalTarget: Double): Double {
        if (currentSize <= minBorderSize + 0.01) return currentSize

        val ratioTarget = currentSize * shrinkRatio
        val floorTarget = if (originalTarget < currentSize) originalTarget else minBorderSize
        return max(floorTarget, ratioTarget).coerceAtMost(currentSize)
    }

    private data class BorderPlan(
        val targetSize: Double,
        val remainingTime: Long,
        val capturedAtMillis: Long
    ) {
        fun restore(border: WorldBorder) {
            val currentSize = border.size
            val adjustedTime = adjustedRemainingTime()

            if (remainingTime <= 0L || abs(currentSize - targetSize) <= 0.01) {
                border.size = targetSize
                return
            }

            border.lerpSizeBetween(currentSize, targetSize, 0L, adjustedTime)
        }

        private fun adjustedRemainingTime(): Long {
            val elapsedTicks = ((System.currentTimeMillis() - capturedAtMillis) / 50L).coerceAtLeast(0L)
            return (remainingTime - elapsedTicks).coerceAtLeast(1L)
        }
    }
}
