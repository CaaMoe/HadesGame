package moe.caa.fabric.hadesgame.gameevent

import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.eventSwap
import moe.caa.fabric.hadesgame.util.sendOverlay
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import java.awt.Color

data object SwapHealth : AbstractGameEvent() {
    override val eventName = "交换血量"

    override suspend fun callEvent() {
        eventSwap({ it.health }) { self, source, health ->
            self.health = health
            Text.literal("你已应用 ").withColor(Color.LIGHT_GRAY.rgb)
                .append(Text.literal(source.name.literalString).withColor(Color.WHITE.rgb)).append(" 的血量")
                .sendOverlay(self)
        }
        SoundEvents.ENTITY_FOX_TELEPORT.broadcast(100F, 0F)
    }
}