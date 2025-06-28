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
 * 重力混乱事件
 * 所有玩家会被随机弹射到空中，增加环境的不可预测性
 * 所有玩家受到相同影响，保持平衡性
 */
data object GravityChaosEvent : AbstractGameEvent() {
    override val eventName = "重力混乱"

    override suspend fun callEvent() {
        for (player in getPlayers()) {
            // 随机向上弹射
            val upwardForce = Random.nextDouble(0.8, 1.5)
            val horizontalX = Random.nextDouble(-0.3, 0.3)
            val horizontalZ = Random.nextDouble(-0.3, 0.3)
            
            player.setVelocity(Vec3d(horizontalX, upwardForce, horizontalZ))
            player.velocityModified = true
            
            Text.literal("重力发生了混乱！")
                .withColor(Color.MAGENTA.rgb)
                .sendOverlay(player)
        }
        
        Text.literal("重力失控了！所有玩家被弹射！")
            .withColor(Color.MAGENTA.rgb)
            .broadcast()
        
        SoundEvents.ENTITY_ENDER_DRAGON_FLAP.broadcast(1.0F, 0.7F)
    }
}
