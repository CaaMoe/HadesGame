package moe.caa.fabric.hadesgame.gameevent

import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.getPlayers
import moe.caa.fabric.hadesgame.util.sendOverlay
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d
import java.awt.Color
import kotlin.random.Random

/**
 * 磁场推斥事件
 * 玩家之间产生推斥力，无法靠近彼此
 * 阻止近距离战斗，鼓励远程策略
 */
data object MagneticRepulsionEvent : AbstractGameEvent() {
    override val eventName = "磁场推斥"

    override suspend fun callEvent() {
        Text.literal("磁场激活！玩家之间产生推斥力")
            .withColor(Color.BLUE.rgb)
            .broadcast()
        
        val players = getPlayers()
        val pushRadius = 8.0
        
        for (player in players) {
            for (otherPlayer in players) {
                if (player != otherPlayer) {
                    val distance = player.pos.distanceTo(otherPlayer.pos)
                    
                    if (distance < pushRadius) {
                        // 计算推斥方向
                        val direction = player.pos.subtract(otherPlayer.pos).normalize()
                        val pushForce = (pushRadius - distance) / pushRadius * 0.5
                        
                        val pushVector = Vec3d(
                            direction.x * pushForce,
                            0.2, // 轻微向上推
                            direction.z * pushForce
                        )
                        
                        player.setVelocity(player.velocity.add(pushVector))
                        player.velocityModified = true
                        
                        Text.literal("磁场推斥生效！")
                            .withColor(Color.BLUE.rgb)
                            .sendOverlay(player)
                    }
                }
            }
        }
        
        SoundEvents.BLOCK_BEACON_DEACTIVATE.broadcast(1.0F, 1.5F)
    }
}
