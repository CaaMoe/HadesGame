package moe.caa.fabric.hadesgame.gameevent

import moe.caa.fabric.hadesgame.util.*
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import java.awt.Color

data object SwapLocation : AbstractGameEvent() {
    override val eventName = "交换位置"

    override suspend fun callEvent() {
        eventSwap({ it.getLocation() }) { self, source, loc ->
            self.teleport(loc)

            Text.literal("你被传送到 ").withColor(Color.LIGHT_GRAY.rgb)
                .append(Text.literal(source.name.literalString).withColor(Color.WHITE.rgb)).append(" 的位置")
                .sendOverlay(self)
        }

        SoundEvents.ENTITY_FOX_TELEPORT.broadcast(100F, 0F)
    }
}