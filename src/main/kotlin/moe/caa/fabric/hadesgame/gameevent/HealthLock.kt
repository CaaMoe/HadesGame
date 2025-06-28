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
 * 血量锁定事件
 * 所有玩家的血量暂时锁定在当前值，无法治疗或受伤
 * 创造暂时安全的间歇期，但也阻止治疗策略
 */
data object HealthLockEvent : AbstractGameEvent() {
    override val eventName = "生命锁定"

    override suspend fun callEvent() {
        for (player in getPlayers()) {
            // 给予抗性效果防止受伤，同时阻止治疗
            player.addStatusEffect(StatusEffectInstance(StatusEffects.RESISTANCE, 300, 4)) // 15秒抗性V
            player.addStatusEffect(StatusEffectInstance(StatusEffects.HUNGER, 300, 2)) // 阻止自然治疗
            
            Text.literal("你的生命值被锁定了！15秒内无法受伤或治疗")
                .withColor(Color.CYAN.rgb)
                .sendOverlay(player)
        }
        
        Text.literal("所有玩家的生命值都被暂时锁定！")
            .withColor(Color.CYAN.rgb)
            .broadcast()
        
        SoundEvents.BLOCK_BEACON_POWER_SELECT.broadcast(1.0F, 1.5F)
    }
}
