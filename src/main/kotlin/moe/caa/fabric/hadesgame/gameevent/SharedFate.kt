package moe.caa.fabric.hadesgame.gameevent

import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.getPlayers
import moe.caa.fabric.hadesgame.util.sendOverlay
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import java.awt.Color

/**
 * 共享命运事件
 * 所有玩家的血量变化会同步影响其他玩家
 * 创造高风险的群体生存挑战
 */
data object SharedFateEvent : AbstractGameEvent() {
    override val eventName = "共享命运"

    override suspend fun callEvent() {
        val players = getPlayers()
        if (players.isEmpty()) return
        
        // 计算平均血量
        val totalHealth = players.sumOf { it.health.toDouble() }
        val averageHealth = (totalHealth / players.size).toFloat()
        
        for (player in players) {
            player.health = averageHealth.coerceAtLeast(1.0f)
            player.addStatusEffect(StatusEffectInstance(StatusEffects.GLOWING, 300, 0)) // 15秒发光效果
            
            Text.literal("你的命运与其他玩家相连！血量已同步为 ${averageHealth.toInt()}")
                .withColor(Color.MAGENTA.rgb)
                .sendOverlay(player)
        }
        
        Text.literal("所有玩家的命运相连！血量已同步")
            .withColor(Color.MAGENTA.rgb)
            .broadcast()
        
        SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE.broadcast(1.0F, 0.8F)
    }
}
