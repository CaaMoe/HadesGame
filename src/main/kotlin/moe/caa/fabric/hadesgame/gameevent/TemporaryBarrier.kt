package moe.caa.fabric.hadesgame.gameevent

import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.getPlayers
import moe.caa.fabric.hadesgame.util.sendOverlay
import net.minecraft.block.Blocks
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import java.awt.Color
import kotlin.random.Random

/**
 * 临时屏障事件
 * 在地图上随机生成临时的屏障墙，改变地形和战术
 * 30秒后自动消失，为战斗增加新的掩体选择
 */
data object TemporaryBarrierEvent : AbstractGameEvent() {
    override val eventName = "临时屏障"
    private val placedBlocks = mutableListOf<Pair<BlockPos, net.minecraft.world.World>>()

    override suspend fun callEvent() {
        val players = getPlayers()
        if (players.isEmpty()) return
        
        for (player in getPlayers()) {
            // 在每个玩家附近创建1-2个小型屏障
            repeat(Random.nextInt(1, 3)) {
                val centerX = player.blockX + Random.nextInt(-10, 11)
                val centerZ = player.blockZ + Random.nextInt(-10, 11)
                val centerY = player.world.getTopY(centerX, centerZ)
                
                // 创建一个小型屏障墙
                for (dx in -1..1) {
                    for (dy in 0..2) {
                        val pos = BlockPos(centerX + dx, centerY + dy, centerZ)
                        if (player.world.getBlockState(pos).isAir) {
                            player.world.setBlockState(pos, Blocks.COBBLESTONE.defaultState)
                            placedBlocks.add(pos to player.world)
                        }
                    }
                }
            }
            
            Text.literal("临时屏障出现了！30秒后会消失")
                .withColor(Color.GRAY.rgb)
                .sendOverlay(player)
        }
        
        Text.literal("临时屏障已生成！")
            .withColor(Color.GRAY.rgb)
            .broadcast()
        
        SoundEvents.BLOCK_STONE_PLACE.broadcast(1.0F, 0.8F)
        
        // 30秒后清理
        kotlinx.coroutines.delay(30000)
        clearBarriers()
    }

    override suspend fun endEvent() {
        clearBarriers()
    }
    
    private fun clearBarriers() {
        for ((pos, world) in placedBlocks) {
            if (world.getBlockState(pos).block == Blocks.COBBLESTONE) {
                world.setBlockState(pos, Blocks.AIR.defaultState)
            }
        }
        placedBlocks.clear()
    }
}
