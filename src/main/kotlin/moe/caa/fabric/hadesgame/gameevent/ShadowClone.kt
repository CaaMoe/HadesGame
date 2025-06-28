package moe.caa.fabric.hadesgame.gameevent

import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.getPlayers
import moe.caa.fabric.hadesgame.util.sendOverlay
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import java.awt.Color
import kotlin.random.Random

/**
 * 影分身事件
 * 玩家获得隐身效果，但会留下虚假的视觉残影
 * 增加战术欺骗和心理战的元素
 */
data object ShadowCloneEvent : AbstractGameEvent() {
    override val eventName = "影分身术"

    override suspend fun callEvent() {
        for (player in getPlayers()) {
            player.addStatusEffect(StatusEffectInstance(StatusEffects.INVISIBILITY, 300, 0)) // 15秒隐身
            player.addStatusEffect(StatusEffectInstance(StatusEffects.SPEED, 300, 0)) // 15秒速度提升
            
            Text.literal("你变成了影子！快速移动时会留下残影")
                .withColor(Color.DARK_GRAY.rgb)
                .sendOverlay(player)
            
            // 播放假的脚步声制造混乱
            repeat(3) {
                kotlinx.coroutines.delay(Random.nextLong(1000, 3000))
                SoundEvents.BLOCK_STONE_STEP.playSound(player, 0.5F, Random.nextFloat() + 0.5F)
            }
        }
        
        Text.literal("所有玩家都变成了影子！")
            .withColor(Color.DARK_GRAY.rgb)
            .broadcast()
        
        SoundEvents.ENTITY_PHANTOM_AMBIENT.broadcast(1.0F, 0.5F)
    }
}
