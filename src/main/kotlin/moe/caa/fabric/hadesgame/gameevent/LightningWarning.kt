package moe.caa.fabric.hadesgame.gameevent

import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.getPlayers
import moe.caa.fabric.hadesgame.util.sendOverlay
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import java.awt.Color
import kotlin.random.Random

/**
 * 闪电预警事件
 * 在随机位置预告闪电将要降落，给玩家时间反应和移动
 * 增加地形控制和预判的策略性
 */
data object LightningWarningEvent : AbstractGameEvent() {
    override val eventName = "雷暴预警"

    override suspend fun callEvent() {
        for (player in getPlayers()) {
            val playerPos = player.blockPos
            
            // 预告3-5个闪电位置
            repeat(Random.nextInt(3, 6)) {
                val targetX = playerPos.x + Random.nextInt(-15, 16)
                val targetZ = playerPos.z + Random.nextInt(-15, 16)
                val targetY = player.world.getTopY() - 1
                val targetPos = BlockPos(targetX, targetY, targetZ)
                
                // 在目标位置放置临时光源作为预警
                val surfacePos = player.world.getTopPosition(targetPos)
                
                Text.literal("闪电即将在 ($targetX, $targetZ) 降落！")
                    .withColor(Color.YELLOW.rgb)
                    .sendOverlay(player)
                
                // 延迟3秒后降落闪电
                kotlinx.coroutines.delay(3000)
                player.world.lightningRod.spawnLightning(surfacePos)
            }
        }
        
        Text.literal("雷暴即将来临！注意预警位置！")
            .withColor(Color.YELLOW.rgb)
            .broadcast()
        
        SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER.broadcast(0.5F, 2.0F)
    }
}
