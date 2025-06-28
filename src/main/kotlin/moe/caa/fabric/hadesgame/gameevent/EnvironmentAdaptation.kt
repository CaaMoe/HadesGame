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
 * 环境适应事件
 * 根据玩家当前所在生物群系给予相应的适应能力
 * 鼓励玩家利用地形优势，增加环境策略
 */
data object EnvironmentAdaptationEvent : AbstractGameEvent() {
    override val eventName = "环境适应"

    override suspend fun callEvent() {
        for (player in getPlayers()) {
            val biome = player.world.getBiome(player.blockPos)
            val biomeName = biome.value().toString()
            
            when {
                biomeName.contains("desert") -> {
                    player.addStatusEffect(StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 600, 0))
                    Text.literal("你适应了沙漠环境，获得火焰抗性！")
                        .withColor(Color.ORANGE.rgb)
                        .sendOverlay(player)
                }
                biomeName.contains("ocean") || biomeName.contains("river") -> {
                    player.addStatusEffect(StatusEffectInstance(StatusEffects.WATER_BREATHING, 600, 0))
                    Text.literal("你适应了水域环境，获得水下呼吸！")
                        .withColor(Color.CYAN.rgb)
                        .sendOverlay(player)
                }
                biomeName.contains("mountain") || biomeName.contains("hill") -> {
                    player.addStatusEffect(StatusEffectInstance(StatusEffects.JUMP_BOOST, 600, 1))
                    Text.literal("你适应了山地环境，获得跳跃提升！")
                        .withColor(Color.GRAY.rgb)
                        .sendOverlay(player)
                }
                biomeName.contains("forest") || biomeName.contains("jungle") -> {
                    player.addStatusEffect(StatusEffectInstance(StatusEffects.SPEED, 600, 0))
                    Text.literal("你适应了森林环境，获得速度提升！")
                        .withColor(Color.GREEN.rgb)
                        .sendOverlay(player)
                }
                else -> {
                    player.addStatusEffect(StatusEffectInstance(StatusEffects.LUCK, 600, 0))
                    Text.literal("你适应了当前环境，获得幸运效果！")
                        .withColor(Color.LIGHT_GRAY.rgb)
                        .sendOverlay(player)
                }
            }
        }
        
        Text.literal("所有玩家都适应了当前环境！")
            .withColor(Color.GREEN.rgb)
            .broadcast()
        
        SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP.broadcast(1.0F, 0.8F)
    }
}
