package moe.caa.fabric.hadesgame.gameevent

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.caa.fabric.hadesgame.GameCore
import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.broadcastOverlay
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import java.awt.Color


data object SuperMiners : AbstractGameEvent() {
    override val eventName = "超级矿工"

    private var activeJob: Job? = null

    override fun initEvent() {

        PlayerBlockBreakEvents.AFTER.register { world, player, pos, state, blockEntity ->
            if (activeJob?.isActive == true) {
                val speech = 2
                for (y in pos.y - speech..<pos.y + speech) {
                    for (x in pos.x - speech..<pos.x + speech) {
                        for (z in pos.z - speech..<pos.z + speech) {
                            world.breakBlock(BlockPos(x, y, z), true)
                        }
                    }
                }
            }
        }
    }

    override suspend fun callEvent() {
        activeJob?.cancel()

        activeJob = GameCore.coroutineScope.launch {
            // 30 秒
            delay(1000 * 30)
            Text.literal("超级矿工已失效").withColor(Color.RED.rgb).broadcastOverlay()
            SoundEvents.ENTITY_VILLAGER_NO.broadcast(1.0F, 1.0F)
        }
        Text.literal("超级矿工已生效").withColor(Color.GREEN.rgb).broadcastOverlay()
        SoundEvents.ENTITY_VILLAGER_YES.broadcast(1.0F, 1.0F)

    }

    override suspend fun endEvent() {
        activeJob?.cancel()
    }
}