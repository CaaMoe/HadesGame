package moe.caa.fabric.hadesgame.stage

import kotlinx.coroutines.*
import moe.caa.fabric.hadesgame.GameCore
import moe.caa.fabric.hadesgame.event.OnHello
import moe.caa.fabric.hadesgame.event.networkHelloEvent
import moe.caa.fabric.hadesgame.util.Location
import moe.caa.fabric.hadesgame.util.broadcastOverlay
import moe.caa.fabric.hadesgame.util.randomSafeLocation
import moe.caa.fabric.hadesgame.util.teleport
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import java.awt.Color
import kotlin.properties.Delegates

// 地图初始化阶段
data object InitStage : AbstractStage() {
    private var spawnLoc by Delegates.notNull<Location>()

    fun lobbyLoc() = spawnLoc.copy(y = 301.0)

    override val stageName = "地图初始化"
    override val nextStage = WaitReadyStage
    override suspend fun shouldEndStage() = true

    override fun init() {
        networkHelloEvent.register {
            if (runCatching { lobbyLoc() }.getOrNull() == null) {
                return@register OnHello.Result.KICK(
                    Text.literal("请稍后再试, 游戏尚未初始化完成!").withColor(Color.RED.rgb)
                )
            }
            return@register OnHello.Result.ALLOWED
        }

        ServerPlayerEvents.JOIN.register {
            if (isCurrentRunStage()) {
                it.teleport(lobbyLoc())
            }
        }
    }


    override suspend fun tickStage() {
        GameCore.logger.info("开始随机地图位置...")
        val tipJob = GameCore.coroutineScope.launch {
            withContext(Dispatchers.IO) {
                var second = 0
                while (isActive) {
                    second++
                    var tip = Text.literal("随机地图中").withColor(Color.LIGHT_GRAY.rgb)
                    repeat(second % 5 + 1) {
                        tip = tip.append(Text.literal(".").withColor(Color.LIGHT_GRAY.rgb))
                    }
                    tip.broadcastOverlay()
                    delay(500)
                }
            }
        }

        val startTimeMills = System.currentTimeMillis()
        spawnLoc = GameCore.server.overworld.randomSafeLocation()
        tipJob.cancel()

        val endTimeMills = System.currentTimeMillis()
        val elapsedTime = endTimeMills - startTimeMills

        GameCore.logger.info("地图随机完成, 随机到位置: x = ${spawnLoc.x}, z = ${spawnLoc.z}, 耗时: $elapsedTime ms")
        Text.literal("随机成功").withColor(Color.GREEN.rgb).append(
            Text.literal(" (耗时: $elapsedTime ms)").withColor(Color.LIGHT_GRAY.rgb)
        ).broadcastOverlay()

        placeLobbyBlock(Blocks.BARRIER.defaultState)
    }

    fun placeLobbyBlock(state: BlockState) {
        // 大厅地面
        for (x in -10 + spawnLoc.x.toInt()..10 + spawnLoc.x.toInt()) {
            for (z in -10 + spawnLoc.z.toInt()..10 + spawnLoc.z.toInt()) {
                spawnLoc.world.setBlockState(BlockPos(x, 300, z), state)
            }
        }

        // 大厅墙壁
        for (xz in -10..10) {
            for (y in 300..307) {
                spawnLoc.world.setBlockState(
                    BlockPos(
                        (spawnLoc.x + xz).toInt(),
                        y,
                        (spawnLoc.z + 10).toInt()
                    ), state
                )
                spawnLoc.world.setBlockState(
                    BlockPos(
                        (spawnLoc.x + xz).toInt(),
                        y,
                        (spawnLoc.z - 10).toInt()
                    ), state
                )
                spawnLoc.world.setBlockState(
                    BlockPos(
                        (spawnLoc.x + 10).toInt(),
                        y,
                        (spawnLoc.z + xz).toInt()
                    ), state
                )
                spawnLoc.world.setBlockState(
                    BlockPos(
                        (spawnLoc.x - 10).toInt(),
                        y,
                        (spawnLoc.z + xz).toInt()
                    ), state
                )
            }
        }
    }
}