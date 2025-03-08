package moe.caa.fabric.hadesgame.stage

import kotlinx.coroutines.delay
import moe.caa.fabric.hadesgame.GameCore
import moe.caa.fabric.hadesgame.util.Location
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.FluidBlock
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Heightmap
import java.awt.Color

// 地图初始化阶段
object InitStage : AbstractOnlyTickOnceStage() {
    private var spawnLoc: Lazy<Location> = lazy { throw UnsupportedOperationException() }
    override val stageName = "地图初始化"
    fun lobbySpawnLocation() = spawnLoc.value

    override suspend fun tickStage() {
        while (true) {
            GameCore.server.playerManager.playerList.forEach { player ->
                player.sendMessage(
                    Text.literal("正在随机游戏地图, 请稍后...").withColor(Color.YELLOW.rgb), true
                )
            }

            val lobbyLocation = randomSpawnLocation()
            if (lobbyLocation == null) {
                delay(1000)
                continue
            }

            spawnLoc = lazy { lobbyLocation }

            GameCore.logger.info("随机到下一把游戏位置: x = ${lobbyLocation.x}, z = ${lobbyLocation.z}")
            break
        }

        GameCore.server.playerManager.playerList.forEach { player ->
            player.sendMessage(
                Text.literal("随机成功").withColor(Color.GREEN.rgb), true
            )
        }
        placeLobbyBlock(Blocks.BARRIER.defaultState)
    }

    private fun randomSpawnLocation(): Location? {
        val posX = (-20000000 + Math.random() * 40000000).toInt()
        val posZ = (-20000000 + Math.random() * 40000000).toInt()

        val server = GameCore.server
        val world = server.overworld

        world.getChunk(posX shr 4, posZ shr 4)
        val posY = world.getTopY(Heightmap.Type.MOTION_BLOCKING, posX, posZ)
        val block = world.getBlockState(BlockPos(posX, posY - 1, posZ))

        if (block.block is FluidBlock) {
            return null
        }
        if (block.block == Blocks.AIR) {
            return null
        }
        return Location(world, posX.toDouble(), 303.0, posZ.toDouble())
    }

    fun placeLobbyBlock(blockState: BlockState) {
        // 放置大厅方块

        // 大厅地面
        for (x in -10 + lobbySpawnLocation().x.toInt()..10 + lobbySpawnLocation().x.toInt()) {
            for (z in -10 + lobbySpawnLocation().z.toInt()..10 + lobbySpawnLocation().z.toInt()) {
                lobbySpawnLocation().world.setBlockState(BlockPos(x, 300, z), blockState)
            }
        }

        // 大厅墙壁
        for (i in -10..10) {
            for (y in 300..307) {
                lobbySpawnLocation().world.setBlockState(
                    BlockPos(
                        (lobbySpawnLocation().x + i).toInt(),
                        y,
                        (lobbySpawnLocation().z + 10).toInt()
                    ), blockState
                )
                lobbySpawnLocation().world.setBlockState(
                    BlockPos(
                        (lobbySpawnLocation().x + i).toInt(),
                        y,
                        (lobbySpawnLocation().z - 10).toInt()
                    ), blockState
                )
                lobbySpawnLocation().world.setBlockState(
                    BlockPos(
                        (lobbySpawnLocation().x + 10).toInt(),
                        y,
                        (lobbySpawnLocation().z + i).toInt()
                    ), blockState
                )
                lobbySpawnLocation().world.setBlockState(
                    BlockPos(
                        (lobbySpawnLocation().x - 10).toInt(),
                        y,
                        (lobbySpawnLocation().z + i).toInt()
                    ), blockState
                )
            }
        }
    }
}