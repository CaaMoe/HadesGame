package moe.caa.fabric.hadesgame.gameevent

import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.eventSwap
import moe.caa.fabric.hadesgame.util.sendOverlay
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import java.awt.Color

data object SwapStatus : AbstractGameEvent() {
    override val eventName = "交换状态"

    override suspend fun callEvent() {
        eventSwap({ it.health to it.hungerManager.foodLevel }) { self, source, status ->
            self.health = status.first
            self.hungerManager.foodLevel = status.second

            Text.literal("你已应用 ").withColor(Color.LIGHT_GRAY.rgb)
                .append(Text.literal(source.name.literalString).withColor(Color.WHITE.rgb)).append(" 的状态")
                .sendOverlay(self)
        }
        SoundEvents.ENTITY_FOX_TELEPORT.broadcast(100F, 0F)
    }
}