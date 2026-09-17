package moe.caa.fabric.hadesgame.game.event

import moe.caa.fabric.hadesgame.event.entityHealEvent
import moe.caa.fabric.hadesgame.util.Message
import moe.caa.fabric.hadesgame.util.Sound
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import java.awt.Color

data object AntiHeal : AbstractSustainGameEvent() {
    override val name = "禁止治疗"

    init {
        entityHealEvent.register { _, _ ->
            return@register !eventRunning
        }
    }

    override val startMessage = Message(
        Component.literal("禁止治疗效果已生效").withColor(Color.GREEN.rgb),
        Component.literal("禁止治疗效果已生效").withColor(Color.GREEN.rgb),
        Sound(SoundEvents.EVOKER_CAST_SPELL, 1F, 0.5F)
    )

    override val endMessage = Message(
        Component.literal("禁止治疗效果已失效").withColor(Color.RED.rgb),
        Component.literal("禁止治疗效果已失效").withColor(Color.RED.rgb),
        Sound(SoundEvents.AMETHYST_BLOCK_BREAK, 1F, 0.5F)
    )

    override fun eventStart() {

    }

    override fun eventEnd() {

    }
}