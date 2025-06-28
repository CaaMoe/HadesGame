package moe.caa.fabric.hadesgame.gameevent

import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.getPlayers
import moe.caa.fabric.hadesgame.util.sendOverlay
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import java.awt.Color
import kotlin.random.Random

/**
 * 随机传送陷阱事件
 * 在地图上设置隐形传送陷阱，踩到会被随机传送
 * 增加移动时的风险和不确定性
 */
data object TeleportTrapEvent : AbstractGameEvent() {
    override val eventName = "传送陷阱"
    private val trapLocations = mutableListOf<Pair<Int, Int>>()

    override suspend fun callEvent() {
        val players = getPlayers()
        if (players.isEmpty()) return
        
        // 在地图上设置5-8个隐形陷阱
        repeat(Random.nextInt(5, 9)) {
            val randomPlayer = players.random()
            val trapX = randomPlayer.blockX + Random.nextInt(-30, 31)
            val trapZ = randomPlayer.blockZ + Random.nextInt(-30, 31)
            
            trapLocations.add(trapX to trapZ)
        }
        
        Text.literal("隐形传送陷阱已布置在地图上！小心行走")
            .withColor(Color.PURPLE.rgb)
            .broadcast()
        
        // 定期检查玩家是否踩到陷阱
        for (i in 0 until 60) { // 检查60秒
            kotlinx.coroutines.delay(1000)
            
            for (player in getPlayers()) {
                val playerX = player.blockX
                val playerZ = player.blockZ
                
                for ((trapX, trapZ) in trapLocations) {
                    val distance = kotlin.math.sqrt(
                        ((playerX - trapX) * (playerX - trapX) + 
                         (playerZ - trapZ) * (playerZ - trapZ)).toDouble()
                    )
                    
                    if (distance <= 2.0) {
                        // 触发陷阱
                        val newX = Random.nextDouble(-100.0, 100.0)
                        val newZ = Random.nextDouble(-100.0, 100.0)
                        val newY = player.world.getTopY(newX.toInt(), newZ.toInt()).toDouble()
                        
                        player.teleport(newX, newY, newZ)
                        
                        Text.literal("你踩到了传送陷阱！")
                            .withColor(Color.PURPLE.rgb)
                            .sendOverlay(player)
                        
                        SoundEvents.ENTITY_ENDERMAN_TELEPORT.playSound(player, 1.0F, 1.0F)
                        
                        // 移除已触发的陷阱
                        trapLocations.remove(trapX to trapZ)
                        break
                    }
                }
            }
        }
        
        SoundEvents.BLOCK_TRIPWIRE_ATTACH.broadcast(1.0F, 0.8F)
    }

    override suspend fun endEvent() {
        trapLocations.clear()
    }
}
