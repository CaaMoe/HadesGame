package moe.caa.fabric.hadesgame.gameevent

import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.getPlayers
import moe.caa.fabric.hadesgame.util.sendOverlay
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import java.awt.Color
import kotlin.random.Random

/**
 * 终极考验事件
 * 最后阶段的高难度挑战，结合多种效果
 * 为游戏结局增加戏剧性和挑战性
 */
data object UltimateTrialEvent : AbstractGameEvent() {
    override val eventName = "终极考验"

    override suspend fun callEvent() {
        val players = getPlayers()
        if (players.size <= 3) { // 只在最后几个玩家时触发
            
            Text.literal("终极考验开始！这将是最后的战斗！")
                .withColor(Color.RED.rgb)
                .broadcast()
            
            // 设置倒计时边界收缩
            Text.literal("危险区域将在30秒后开始收缩！")
                .withColor(Color.DARK_RED.rgb)
                .broadcast()
            
            kotlinx.coroutines.delay(30000)
            
            // 开始对边界外的玩家造成伤害
            Text.literal("危险区域开始收缩！远离边界！")
                .withColor(Color.DARK_RED.rgb)
                .broadcast()
            
            val centerX = players.map { it.x }.average()
            val centerZ = players.map { it.z }.average()
            var safeRadius = 50.0
            
            // 逐渐收缩安全区域
            repeat(10) {
                kotlinx.coroutines.delay(3000)
                safeRadius -= 5.0
                
                for (player in getPlayers()) {
                    val distance = kotlin.math.sqrt(
                        (player.x - centerX) * (player.x - centerX) + 
                        (player.z - centerZ) * (player.z - centerZ)
                    )
                    
                    if (distance > safeRadius) {
                        player.damage(player.damageSources.generic(), 2.0f)
                        Text.literal("你在危险区域！快回到安全区！")
                            .withColor(Color.RED.rgb)
                            .sendOverlay(player)
                    }
                }
                
                Text.literal("安全区域缩小到半径 ${safeRadius.toInt()} 米！")
                    .withColor(Color.DARK_RED.rgb)
                    .broadcast()
            }
        }
        
        SoundEvents.ENTITY_WITHER_SPAWN.broadcast(1.0F, 0.5F)
    }
}
