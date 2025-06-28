package moe.caa.fabric.hadesgame.gameevent

import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.getPlayers
import moe.caa.fabric.hadesgame.util.sendOverlay
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import java.awt.Color
import kotlin.random.Random

/**
 * 虚假伤害事件
 * 玩家会听到受伤声音和看到虚假的伤害效果，但实际没有受伤
 * 心理战术事件，增加玩家的紧张感和不确定性
 */
data object FakeDamageEvent : AbstractGameEvent() {
    override val eventName = "幻痛感应"

    override suspend fun callEvent() {
        for (player in getPlayers()) {
            // 播放受伤音效
            SoundEvents.ENTITY_PLAYER_HURT.playSound(player, 0.8F, 1.0F)
            
            // 显示虚假伤害信息
            Text.literal("你感受到了幻痛！但这只是错觉...")
                .withColor(Color.RED.rgb)
                .sendOverlay(player)
            
            // 随机震动效果（如果支持）
            if (Random.nextBoolean()) {
                SoundEvents.ENTITY_GENERIC_HURT.playSound(player, 0.5F, Random.nextFloat() + 0.5F)
            }
        }
        
        Text.literal("所有玩家都感受到了幻痛！")
            .withColor(Color.RED.rgb)
            .broadcast()
        
        SoundEvents.ENTITY_VEX_HURT.broadcast(1.0F, 0.3F)
    }
}
