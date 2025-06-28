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
 * 缓慢治愈事件
 * 所有玩家获得缓慢的持续治疗效果，但移动速度降低
 * 平衡了治疗和机动性，适合生存游戏的平衡性
 */
data object SlowHealingEvent : AbstractGameEvent() {
    override val eventName = "缓慢治愈"

    override suspend fun callEvent() {
        for (player in getPlayers()) {
            player.addStatusEffect(StatusEffectInstance(StatusEffects.REGENERATION, 400, 0)) // 20秒再生
            player.addStatusEffect(StatusEffectInstance(StatusEffects.SLOWNESS, 400, 0)) // 20秒缓慢
            
            Text.literal("你的身体正在缓慢恢复，但行动变得迟缓")
                .withColor(Color.GREEN.rgb)
                .sendOverlay(player)
        }
        
        Text.literal("所有玩家获得缓慢治愈效果！")
            .withColor(Color.GREEN.rgb)
            .broadcast()
        
        SoundEvents.BLOCK_BEACON_AMBIENT.broadcast(1.0F, 0.8F)
    }
}
