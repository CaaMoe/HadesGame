package moe.caa.fabric.hadesgame.game.event

import moe.caa.fabric.hadesgame.util.Message
import moe.caa.fabric.hadesgame.util.Sound
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import java.awt.Color

data object SwapHealth : AbstractSwapTypeGameEvent<Float>() {
    override val name = "互换血量"
    override fun extract(source: ServerPlayer) = source.health


    override fun apply(target: ServerPlayer, data: Float) {
        target.health = data
    }


    override fun buildSwapedMessage(
        receiver: ServerPlayer,
        swapedTarget: ServerPlayer
    ): Message {
        val message = Component.empty()
            .append("你与 ").withColor(Color.LIGHT_GRAY.rgb)
            .append(Component.literal(swapedTarget.nameAndId().name).withColor(0XFFD700))
            .append(" 互换了血量值.").withColor(Color.LIGHT_GRAY.rgb)

        return Message(
            message,
            message,
            Sound(SoundEvents.BREWING_STAND_BREW, 1F, 1.5F)
        )
    }

    override fun buildNoGoalSwapMessage(receiver: ServerPlayer): Message {
        val message = Component.empty()
            .append("你没有与任何玩家互换血量值.").withColor(Color.LIGHT_GRAY.rgb)

        return Message(
            message, message,
            Sound(SoundEvents.NOTE_BLOCK_DIDGERIDOO.value(), 1F, 0.8F)
        )
    }
}