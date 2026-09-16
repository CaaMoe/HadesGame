package moe.caa.fabric.hadesgame.game.stage

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import moe.caa.fabric.hadesgame.event.OnHello
import moe.caa.fabric.hadesgame.event.networkHelloEvent
import moe.caa.fabric.hadesgame.game.GameCore
import moe.caa.fabric.hadesgame.util.Location
import moe.caa.fabric.hadesgame.util.broadcastOverlay
import moe.caa.fabric.hadesgame.util.randomLobbySpawnLocation
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import java.awt.Color
import kotlin.properties.Delegates
import kotlin.time.Duration.Companion.milliseconds

data object MapInitStage : AbstractStage() {
    override val name = "地图初始化"

    var lobbySpawnLoc by Delegates.notNull<Location>()
        private set

    override val next = WaitReadyStage

    override suspend fun shouldEnd() = true

    init {
        networkHelloEvent.register {
            if (runCatching { lobbySpawnLoc }.getOrNull() == null) {
                return@register OnHello.Result.KICK(
                    Component.literal("游戏地图尚未初始化完成, 请稍后重试!").withColor(Color.RED.rgb)
                )
            }
            return@register OnHello.Result.ALLOWED
        }
    }

    override suspend fun tick() {
        GameCore.logger.info("正在随机刷新游戏地图...")
        val notifyJob = GameCore.coroutineScope.launch {
            var seconds = 0
            while (isActive) {
                seconds++
                var tip = Component.literal("随机地图中").withColor(Color.LIGHT_GRAY.rgb)
                repeat(seconds % 5 + 1) {
                    tip = tip.append(Component.literal(".").withColor(Color.LIGHT_GRAY.rgb))
                }
                tip.broadcastOverlay()
                delay(500.milliseconds)
            }
        }

        val startTimeMills = System.currentTimeMillis()
        lobbySpawnLoc = GameCore.server.overworld().randomLobbySpawnLocation()
        notifyJob.cancel()

        val endTimeMills = System.currentTimeMillis()
        val elapsedTime = endTimeMills - startTimeMills

        GameCore.logger.info("地图随机完成, 随机到位置: x = ${lobbySpawnLoc.x}, z = ${lobbySpawnLoc.z}, 耗时: $elapsedTime ms")
        Component.literal("随机成功").withColor(Color.GREEN.rgb).append(
            Component.literal(" (耗时: $elapsedTime ms)").withColor(Color.LIGHT_GRAY.rgb)
        ).broadcastOverlay()

        placeLobbyBlock(Blocks.BARRIER.defaultBlockState())
    }

    fun placeLobbyBlock(state: BlockState) {
        for (x in -10 + lobbySpawnLoc.x.toInt()..10 + lobbySpawnLoc.x.toInt()) {
            for (z in -10 + lobbySpawnLoc.z.toInt()..10 + lobbySpawnLoc.z.toInt()) {
                lobbySpawnLoc.world.setBlock(BlockPos(x, lobbySpawnLoc.y.toInt() - 2, z), state, 3)
            }
        }

        for (xz in -10..10) {
            for (y in lobbySpawnLoc.y.toInt() - 2..lobbySpawnLoc.y.toInt() + 7) {
                lobbySpawnLoc.world.setBlock(
                    BlockPos((lobbySpawnLoc.x + xz).toInt(), y, (lobbySpawnLoc.z + 10).toInt()),
                    state,
                    3
                )
                lobbySpawnLoc.world.setBlock(
                    BlockPos((lobbySpawnLoc.x + xz).toInt(), y, (lobbySpawnLoc.z - 10).toInt()),
                    state,
                    3
                )
                lobbySpawnLoc.world.setBlock(
                    BlockPos((lobbySpawnLoc.x + 10).toInt(), y, (lobbySpawnLoc.z + xz).toInt()),
                    state,
                    3
                )
                lobbySpawnLoc.world.setBlock(
                    BlockPos((lobbySpawnLoc.x - 10).toInt(), y, (lobbySpawnLoc.z + xz).toInt()),
                    state,
                    3
                )
            }
        }
    }
}