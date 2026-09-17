package moe.caa.fabric.hadesgame.game.event

import moe.caa.fabric.hadesgame.game.GameCore
import moe.caa.fabric.hadesgame.util.Message
import moe.caa.fabric.hadesgame.util.Sound
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import java.awt.Color

data object TickDown : AbstractSustainGameEvent() {
    override val name = "超级减速"
    override val mutualExclusions = listOf(TickUp)

    override val startMessage = Message(
        Component.literal("超级减速效果已生效").withColor(Color.GREEN.rgb),
        Component.literal("超级减速效果已生效").withColor(Color.GREEN.rgb),
        Sound(SoundEvents.BEACON_ACTIVATE, 1F, 1.2F)
    )

    override val endMessage = Message(
        Component.literal("超级减速效果已失效").withColor(Color.RED.rgb),
        Component.literal("超级减速效果已失效").withColor(Color.RED.rgb),
        Sound(SoundEvents.BEACON_DEACTIVATE, 1F, 0.9F)
    )

    override fun eventStart() {
        GameCore.server.tickRateManager().setTickRate(10F)
    }

    override fun eventEnd() {
        GameCore.server.tickRateManager().setTickRate(20F)
    }
}