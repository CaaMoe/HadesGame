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
 * 轻飘飘事件
 * 所有玩家获得缓降效果和跳跃提升，改变地形优势
 * 让高度不再是绝对的安全保障
 */
data object FeatherFallEvent : AbstractGameEvent() {
    override val eventName = "轻如羽毛"

    override suspend fun callEvent() {
        for (player in getPlayers()) {
            player.addStatusEffect(StatusEffectInstance(StatusEffects.SLOW_FALLING, 600, 0)) // 30秒缓降
            player.addStatusEffect(StatusEffectInstance(StatusEffects.JUMP_BOOST, 600, 1)) // 30秒跳跃提升II
            
            Text.literal("你变得轻如羽毛！跳跃和坠落都变得不同了")
                .withColor(Color.WHITE.rgb)
                .sendOverlay(player)
        }
        
        Text.literal("所有玩家都变得轻如羽毛！")
            .withColor(Color.WHITE.rgb)
            .broadcast()
        
        SoundEvents.ENTITY_CHICKEN_AMBIENT.broadcast(1.0F, 1.5F)
    }
}
