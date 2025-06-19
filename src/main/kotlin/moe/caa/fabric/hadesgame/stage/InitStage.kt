package moe.caa.fabric.hadesgame.stage

import kotlinx.coroutines.*
import moe.caa.fabric.hadesgame.GameCore
import moe.caa.fabric.hadesgame.util.Location
import moe.caa.fabric.hadesgame.util.broadcastOverlay
import moe.caa.fabric.hadesgame.util.randomSafeLocation
import net.minecraft.text.Text
import java.awt.Color
import kotlin.properties.Delegates

// 地图初始化阶段
object InitStage : AbstractOnlyTickOnceStage() {
    var spawnLoc by Delegates.notNull<Location>()

    override val stageName = "地图初始化"

    private val waitingRandomWorld by lazy { Text.literal("随机地图中...").withColor(Color.LIGHT_GRAY.rgb) }

    override suspend fun tickStage() {

        GameCore.logger.info("开始随机地图位置...")
        val tipJob = GameCore.coroutineScope.launch {
            withContext(Dispatchers.IO) {
                while (isActive) {
                    waitingRandomWorld.broadcastOverlay()
                    delay(1000)
                }
            }
        }

        val startTimeMills = System.currentTimeMillis()
        spawnLoc = GameCore.coroutineScope.async {
            GameCore.server.overworld.randomSafeLocation(1000)
        }.await()
        tipJob.cancel()

        val endTimeMills = System.currentTimeMillis()
        val elapsedTime = endTimeMills - startTimeMills

        GameCore.logger.info("地图随机完成, 随机到位置: x = ${spawnLoc.x}, z = ${spawnLoc.z}, 耗时: $elapsedTime ms")
        Text.literal("随机成功").withColor(Color.GREEN.rgb).append(
            Text.literal(" (耗时: $elapsedTime ms)").withColor(Color.LIGHT_GRAY.rgb)
        ).broadcastOverlay()

        delay(10000)
    }
//
//    fun placeLobbyBlock(blockState: BlockState) {
//        // 放置大厅方块
//
//        // 大厅地面
//        for (x in -10 + lobbySpawnLocation().x.toInt()..10 + lobbySpawnLocation().x.toInt()) {
//            for (z in -10 + lobbySpawnLocation().z.toInt()..10 + lobbySpawnLocation().z.toInt()) {
//                lobbySpawnLocation().world.setBlockState(BlockPos(x, 300, z), blockState)
//            }
//        }
//
//        // 大厅墙壁
//        for (i in -10..10) {
//            for (y in 300..307) {
//                lobbySpawnLocation().world.setBlockState(
//                    BlockPos(
//                        (lobbySpawnLocation().x + i).toInt(),
//                        y,
//                        (lobbySpawnLocation().z + 10).toInt()
//                    ), blockState
//                )
//                lobbySpawnLocation().world.setBlockState(
//                    BlockPos(
//                        (lobbySpawnLocation().x + i).toInt(),
//                        y,
//                        (lobbySpawnLocation().z - 10).toInt()
//                    ), blockState
//                )
//                lobbySpawnLocation().world.setBlockState(
//                    BlockPos(
//                        (lobbySpawnLocation().x + 10).toInt(),
//                        y,
//                        (lobbySpawnLocation().z + i).toInt()
//                    ), blockState
//                )
//                lobbySpawnLocation().world.setBlockState(
//                    BlockPos(
//                        (lobbySpawnLocation().x - 10).toInt(),
//                        y,
//                        (lobbySpawnLocation().z + i).toInt()
//                    ), blockState
//                )
//            }
//        }
//    }
}