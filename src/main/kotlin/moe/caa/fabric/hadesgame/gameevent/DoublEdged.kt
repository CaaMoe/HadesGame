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
 * 双刃剑事件
 * 玩家同时获得力量提升和伤害加深效果
 * 高风险高回报的平衡机制
 */
data object DoublEdgedEvent : AbstractGameEvent() {
    override val eventName = "双刃剑"

    override suspend fun callEvent() {
        for (player in getPlayers()) {
            player.addStatusEffect(StatusEffectInstance(StatusEffects.STRENGTH, 400, 1)) // 20秒力量II
            player.addStatusEffect(StatusEffectInstance(StatusEffects.WEAKNESS, 400, 0)) // 20秒虚弱（受到更多伤害）
            
            Text.literal("你获得了强大的力量，但也变得更加脆弱！")
                .withColor(Color.MAGENTA.rgb)
                .sendOverlay(player)
        }
        
        Text.literal("所有玩家都获得了双刃剑效果！")
            .withColor(Color.MAGENTA.rgb)
            .broadcast()
        
        SoundEvents.ITEM_TRIDENT_THUNDER.broadcast(1.0F, 1.2F)
    }
}
