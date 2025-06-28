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
 * 反重力跳跃事件
 * 所有玩家获得极高的跳跃能力但控制困难
 * 改变垂直移动策略，增加位置控制的挑战
 */
data object AntiGravityJumpEvent : AbstractGameEvent() {
    override val eventName = "反重力跳跃"

    override suspend fun callEvent() {
        for (player in getPlayers()) {
            player.addStatusEffect(StatusEffectInstance(StatusEffects.JUMP_BOOST, 400, 4)) // 20秒跳跃提升V
            player.addStatusEffect(StatusEffectInstance(StatusEffects.SLOW_FALLING, 400, 0)) // 20秒缓降
            
            Text.literal("你获得了超强跳跃能力！小心控制降落")
                .withColor(Color.LIGHT_GRAY.rgb)
                .sendOverlay(player)
        }
        
        Text.literal("所有玩家都获得了反重力跳跃！")
            .withColor(Color.LIGHT_GRAY.rgb)
            .broadcast()
        
        SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH.broadcast(1.0F, 0.8F)
    }
}
