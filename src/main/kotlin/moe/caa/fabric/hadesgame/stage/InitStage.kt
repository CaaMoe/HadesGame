package moe.caa.fabric.hadesgame.stage

import kotlinx.coroutines.*
import moe.caa.fabric.hadesgame.GameCore
import moe.caa.fabric.hadesgame.event.OnHello
import moe.caa.fabric.hadesgame.event.networkHelloEvent
import moe.caa.fabric.hadesgame.util.Location
import moe.caa.fabric.hadesgame.util.broadcastOverlay
import moe.caa.fabric.hadesgame.util.randomLobbySpawnLocation
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import java.awt.Color
import kotlin.properties.Delegates

// 地图初始化阶段
data object InitStage : AbstractStage() {
    var lobbySpawnLoc by Delegates.notNull<Location>()

    override val stageName = "地图初始化"
    override val nextStage = WaitReadyStage
    override suspend fun shouldEndStage() = true

    override fun init() {
        networkHelloEvent.register {
            if (runCatching { lobbySpawnLoc }.getOrNull() == null) {
                return@register OnHello.Result.KICK(
                    Text.literal("请稍后再试, 游戏尚未初始化完成!").withColor(Color.RED.rgb)
                )
            }
            return@register OnHello.Result.ALLOWED
        }
    }

    override suspend fun endStage() {
        val border = GameCore.server.overworld.worldBorder
        border.setCenter(lobbySpawnLoc.x, lobbySpawnLoc.z)
        border.size = 500.0
    }

    override suspend fun tickStage() {
        GameCore.logger.info("开始随机地图位置...")
        val tipJob = GameCore.coroutineScope.launch {
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

        val startTimeMills = System.currentTimeMillis()
        lobbySpawnLoc = GameCore.server.overworld.randomLobbySpawnLocation()
        tipJob.cancel()

        val endTimeMills = System.currentTimeMillis()
        val elapsedTime = endTimeMills - startTimeMills

        GameCore.logger.info("地图随机完成, 随机到位置: x = ${lobbySpawnLoc.x}, z = ${lobbySpawnLoc.z}, 耗时: $elapsedTime ms")
        Text.literal("随机成功").withColor(Color.GREEN.rgb).append(
            Text.literal(" (耗时: $elapsedTime ms)").withColor(Color.LIGHT_GRAY.rgb)
        ).broadcastOverlay()

        placeLobbyBlock(Blocks.BARRIER.defaultState)
    }

    fun placeLobbyBlock(state: BlockState) {
        // 大厅地面
        for (x in -10 + lobbySpawnLoc.x.toInt()..10 + lobbySpawnLoc.x.toInt()) {
            for (z in -10 + lobbySpawnLoc.z.toInt()..10 + lobbySpawnLoc.z.toInt()) {
                lobbySpawnLoc.world.setBlockState(BlockPos(x, lobbySpawnLoc.y.toInt() - 2, z), state)
            }
        }

        // 大厅墙壁
        for (xz in -10..10) {
            for (y in lobbySpawnLoc.y.toInt() - 2..lobbySpawnLoc.y.toInt() + 7) {
                lobbySpawnLoc.world.setBlockState(
                    BlockPos(
                        (lobbySpawnLoc.x + xz).toInt(),
                        y,
                        (lobbySpawnLoc.z + 10).toInt()
                    ), state
                )
                lobbySpawnLoc.world.setBlockState(
                    BlockPos(
                        (lobbySpawnLoc.x + xz).toInt(),
                        y,
                        (lobbySpawnLoc.z - 10).toInt()
                    ), state
                )
                lobbySpawnLoc.world.setBlockState(
                    BlockPos(
                        (lobbySpawnLoc.x + 10).toInt(),
                        y,
                        (lobbySpawnLoc.z + xz).toInt()
                    ), state
                )
                lobbySpawnLoc.world.setBlockState(
                    BlockPos(
                        (lobbySpawnLoc.x - 10).toInt(),
                        y,
                        (lobbySpawnLoc.z + xz).toInt()
                    ), state
                )
            }
        }
    }
}