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
 * 快速代谢事件
 * 所有玩家获得急迫效果但饥饿消耗加快
 * 平衡了挖掘效率和资源消耗
 */
data object RapidMetabolismEvent : AbstractGameEvent() {
    override val eventName = "快速代谢"

    override suspend fun callEvent() {
        for (player in getPlayers()) {
            player.addStatusEffect(StatusEffectInstance(StatusEffects.HASTE, 600, 1)) // 30秒急迫II
            player.addStatusEffect(StatusEffectInstance(StatusEffects.HUNGER, 600, 1)) // 30秒饥饿II
            
            Text.literal("你的新陈代谢加快了！动作更快但也更容易饿")
                .withColor(Color.RED.rgb)
                .sendOverlay(player)
        }
        
        Text.literal("所有玩家的新陈代谢都加快了！")
            .withColor(Color.RED.rgb)
            .broadcast()
        
        SoundEvents.ENTITY_PLAYER_HEARTBEAT.broadcast(1.0F, 1.5F)
    }
}
