package moe.caa.fabric.hadesgame.gameevent

import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.getPlayers
import moe.caa.fabric.hadesgame.util.sendOverlay
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import java.awt.Color
import kotlin.math.max

data object InvertStatus : AbstractGameEvent() {
    override val eventName = "反转状态"

    override suspend fun callEvent() {
        val minHealth = 1.0f
        val minFoodLevel = 1

        for (player in getPlayers()) {
            val oldHealth = player.health
            val oldFoodLevel = player.hungerManager.foodLevel

            // 反转
            val newHealth = max(minHealth, player.maxHealth - oldHealth)
            val newFoodLevel = max(minFoodLevel, 20 - oldFoodLevel)

            player.health = newHealth
            player.hungerManager.foodLevel = newFoodLevel

            Text.literal("你的血量与饥饿值已反转").withColor(Color.LIGHT_GRAY.rgb).sendOverlay(player)
        }
        SoundEvents.ENTITY_WITCH_DRINK.broadcast(1.0F, 1.0F)
    }
}