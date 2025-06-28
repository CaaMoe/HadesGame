package moe.caa.fabric.hadesgame.gameevent

import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.getPlayers
import moe.caa.fabric.hadesgame.util.sendOverlay
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import java.awt.Color
import kotlin.random.Random

/**
 * 随机传声事件
 * 玩家的脚步声会随机传递给其他玩家，增加心理战术
 * 让玩家无法完全依赖声音定位，增加不确定性
 */
data object RandomSoundTransmissionEvent : AbstractGameEvent() {
    override val eventName = "声音幻象"

    override suspend fun callEvent() {
        Text.literal("声音开始产生幻象！你听到的脚步声可能来自任何地方")
            .withColor(Color.PURPLE.rgb)
            .broadcast()
        
        for (player in getPlayers()) {
            // 播放随机的脚步声
            repeat(Random.nextInt(2, 6)) {
                val randomSound = listOf(
                    SoundEvents.BLOCK_STONE_STEP,
                    SoundEvents.BLOCK_GRASS_STEP,
                    SoundEvents.BLOCK_SAND_STEP,
                    SoundEvents.BLOCK_GRAVEL_STEP
                ).random()
                
                randomSound.playSound(player, 0.8F, Random.nextFloat() * 0.4F + 0.8F)
            }
            
            Text.literal("你的脚步声现在可能会迷惑其他玩家")
                .withColor(Color.PURPLE.rgb)
                .sendOverlay(player)
        }
        
        SoundEvents.ENTITY_VEX_AMBIENT.broadcast(1.0F, 0.5F)
    }
}
