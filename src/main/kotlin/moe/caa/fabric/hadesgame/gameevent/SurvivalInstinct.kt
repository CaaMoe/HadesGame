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
 * 生存本能事件
 * 血量越低的玩家获得越强的增益效果
 * 为濒死玩家提供反击机会，增加游戏的戏剧性
 */
data object SurvivalInstinctEvent : AbstractGameEvent() {
    override val eventName = "生存本能"

    override suspend fun callEvent() {
        for (player in getPlayers()) {
            val healthPercentage = player.health / player.maxHealth
            
            when {
                healthPercentage <= 0.25f -> {
                    // 血量25%以下：强大增益
                    player.addStatusEffect(StatusEffectInstance(StatusEffects.STRENGTH, 600, 2)) // 30秒力量III
                    player.addStatusEffect(StatusEffectInstance(StatusEffects.SPEED, 600, 2)) // 30秒速度III
                    player.addStatusEffect(StatusEffectInstance(StatusEffects.RESISTANCE, 600, 1)) // 30秒抗性II
                    
                    Text.literal("生存本能爆发！你获得了强大的力量！")
                        .withColor(Color.RED.rgb)
                        .sendOverlay(player)
                }
                healthPercentage <= 0.5f -> {
                    // 血量50%以下：中等增益
                    player.addStatusEffect(StatusEffectInstance(StatusEffects.STRENGTH, 600, 1)) // 30秒力量II
                    player.addStatusEffect(StatusEffectInstance(StatusEffects.SPEED, 600, 1)) // 30秒速度II
                    
                    Text.literal("生存本能觉醒！你变得更加强大")
                        .withColor(Color.ORANGE.rgb)
                        .sendOverlay(player)
                }
                healthPercentage <= 0.75f -> {
                    // 血量75%以下：轻微增益
                    player.addStatusEffect(StatusEffectInstance(StatusEffects.STRENGTH, 600, 0)) // 30秒力量I
                    player.addStatusEffect(StatusEffectInstance(StatusEffects.SPEED, 600, 0)) // 30秒速度I
                    
                    Text.literal("生存本能激活！你感到力量涌现")
                        .withColor(Color.YELLOW.rgb)
                        .sendOverlay(player)
                }
                else -> {
                    // 血量75%以上：无增益
                    Text.literal("你的生命力充沛，没有激发生存本能")
                        .withColor(Color.GREEN.rgb)
                        .sendOverlay(player)
                }
            }
        }
        
        Text.literal("生存本能觉醒！血量越低的玩家获得越强的力量！")
            .withColor(Color.RED.rgb)
            .broadcast()
        
        SoundEvents.ENTITY_PLAYER_LEVELUP.broadcast(1.0F, 0.8F)
    }
}
