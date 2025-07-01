package moe.caa.fabric.hadesgame.gameevent

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.caa.fabric.hadesgame.GameCore
import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.broadcastOverlay
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import java.awt.Color

data object TickDown : AbstractGameEvent() {
    override val eventName = "超级减速"

    private var activeJob: Job? = null


    override suspend fun callEvent() {
        activeJob?.cancel()

        activeJob = GameCore.coroutineScope.launch {
            GameCore.server.tickManager.tickRate = 10F
            // 30 秒
            delay(1000 * 30)

            Text.literal("超级减速效果已失效").withColor(Color.GREEN.rgb).broadcastOverlay()

            SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP.broadcast(1F, 1F)

        }.apply {
            invokeOnCompletion {
                GameCore.server.tickManager.tickRate = 20F
            }
        }
        Text.literal("超级减速效果已生效").withColor(Color.GREEN.rgb).broadcastOverlay()

        SoundEvents.BLOCK_BEACON_ACTIVATE.broadcast(1F, 1F)
    }

    override suspend fun endEvent() {
        activeJob?.cancel()
    }
}