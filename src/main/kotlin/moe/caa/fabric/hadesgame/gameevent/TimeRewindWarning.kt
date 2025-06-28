package moe.caa.fabric.hadesgame.gameevent

import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.getPlayers
import moe.caa.fabric.hadesgame.util.sendOverlay
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import java.awt.Color
import kotlin.random.Random

/**
 * 时间倒流预告事件
 * 预告即将发生的位置回退，给玩家准备时间
 * 增加策略深度和时间管理的重要性
 */
data object TimeRewindWarningEvent : AbstractGameEvent() {
    override val eventName = "时间倒流预告"
    private val playerPositions = mutableMapOf<String, Pair<Double, Double>>()

    override suspend fun callEvent() {
        // 记录当前位置
        for (player in getPlayers()) {
            playerPositions[player.uuidAsString] = player.x to player.z
        }
        
        Text.literal("时间扭曲开始！15秒后你将回到当前位置！")
            .withColor(Color.PURPLE.rgb)
            .broadcast()
        
        SoundEvents.BLOCK_PORTAL_AMBIENT.broadcast(1.0F, 0.5F)
        
        // 15秒倒计时
        for (i in 15 downTo 1) {
            kotlinx.coroutines.delay(1000)
            if (i <= 5) {
                Text.literal("时间倒流倒计时：$i")
                    .withColor(Color.RED.rgb)
                    .broadcast()
            }
        }
        
        // 执行时间倒流
        for (player in getPlayers()) {
            val savedPos = playerPositions[player.uuidAsString]
            if (savedPos != null) {
                val y = player.world.getTopY(savedPos.first.toInt(), savedPos.second.toInt()).toDouble()
                player.teleport(savedPos.first, y, savedPos.second)
                
                Text.literal("时间倒流完成！你回到了15秒前的位置")
                    .withColor(Color.PURPLE.rgb)
                    .sendOverlay(player)
            }
        }
        
        SoundEvents.ENTITY_ENDERMAN_TELEPORT.broadcast(1.0F, 0.3F)
    }

    override suspend fun endEvent() {
        playerPositions.clear()
    }
}
