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
 * 临时隐身事件
 * 所有玩家获得10秒隐身效果，但移动时会暴露位置
 * 这个事件增加策略性，玩家需要在隐身和移动之间做出选择
 */
data object TemporaryInvisibilityEvent : AbstractGameEvent() {
    override val eventName = "隐身迷雾"

    override suspend fun callEvent() {
        for (player in getPlayers()) {
            player.addStatusEffect(StatusEffectInstance(StatusEffects.INVISIBILITY, 200, 0)) // 10秒
            Text.literal("你获得了短暂的隐身能力，但移动时要小心！")
                .withColor(Color.CYAN.rgb)
                .sendOverlay(player)
        }
        
        Text.literal("所有玩家都隐身了！")
            .withColor(Color.CYAN.rgb)
            .broadcast()
        
        SoundEvents.BLOCK_BELL_USE.broadcast(1.0F, 0.8F)
    }
}
