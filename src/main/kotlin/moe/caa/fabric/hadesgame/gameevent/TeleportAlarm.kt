package moe.caa.fabric.hadesgame.gameevent

import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.getPlayers
import moe.caa.fabric.hadesgame.util.sendOverlay
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import java.awt.Color
import kotlin.random.Random

/**
 * 随机传送警报事件
 * 预先通知玩家即将发生随机传送，给予准备时间
 * 增加战略规划和心理压力
 */
data object TeleportAlarmEvent : AbstractGameEvent() {
    override val eventName = "传送警报"

    override suspend fun callEvent() {
        Text.literal("警报：10秒后将发生随机传送！")
            .withColor(Color.RED.rgb)
            .broadcast()
        
        SoundEvents.BLOCK_NOTE_BLOCK_BELL.broadcast(1.0F, 2.0F)
        
        // 倒计时
        for (i in 10 downTo 1) {
            kotlinx.coroutines.delay(1000)
            Text.literal("传送倒计时：$i")
                .withColor(Color.YELLOW.rgb)
                .broadcast()
        }
        
        // 执行传送
        for (player in getPlayers()) {
            val newX = Random.nextDouble(-200.0, 200.0)
            val newZ = Random.nextDouble(-200.0, 200.0)
            val newY = player.world.getTopY(newX.toInt(), newZ.toInt()).toDouble()
            
            player.teleport(newX, newY, newZ)
            
            Text.literal("传送完成！新位置：(${newX.toInt()}, ${newZ.toInt()})")
                .withColor(Color.GREEN.rgb)
                .sendOverlay(player)
        }
        
        SoundEvents.ENTITY_ENDERMAN_TELEPORT.broadcast(1.0F, 1.0F)
    }
}
