package moe.caa.fabric.hadesgame.game.event

import moe.caa.fabric.hadesgame.util.*
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import java.awt.Color

data object SwapLocation : AbstractSwapTypeGameEvent<Location>() {
    override val name = "互换位置"

    override fun extract(source: ServerPlayer) = source.getLocation()

    override fun apply(target: ServerPlayer, data: Location) {
        target.teleport(data)
    }

    override fun buildSwapedMessage(
        receiver: ServerPlayer,
        swapedTarget: ServerPlayer
    ): Message {
        val message = Component.empty()
            .append("你与 ").withColor(Color.LIGHT_GRAY.rgb)
            .append(Component.literal(swapedTarget.nameAndId().name).withColor(0XFFD700))
            .append(" 互换了位置.").withColor(Color.LIGHT_GRAY.rgb)

        return Message(
            message,
            message,
            Sound(SoundEvents.CHORUS_FRUIT_TELEPORT, 1F, 1F)
        )
    }

    override fun buildNoGoalSwapMessage(receiver: ServerPlayer): Message {
        val message = Component.empty()
            .append("你没有与任何玩家互换位置.").withColor(Color.LIGHT_GRAY.rgb)

        return Message(
            message, message,
            Sound(SoundEvents.NOTE_BLOCK_DIDGERIDOO.value(), 1F, 0.8F)
        )
    }
}