package moe.caa.fabric.hadesgame.game.event

import moe.caa.fabric.hadesgame.game.stage.GamingStage
import moe.caa.fabric.hadesgame.util.Message
import moe.caa.fabric.hadesgame.util.Sound
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.BossEvent
import java.awt.Color

internal data object InternalInvincible : AbstractSustainGameEvent() {
    override val keepTickNumber = 20 * 30

    override val bossBarColor = BossEvent.BossBarColor.RED

    override fun eventStart() {
        GamingStage.invincible = true
    }

    override fun eventEnd() {
        GamingStage.invincible = false
    }

    override val name = "无敌阶段"
    override val extractable = false

    override val endMessage = Message(
        Component.literal("无敌阶段已结束").withColor(Color.RED.rgb),
        Component.literal("无敌阶段已结束").withColor(Color.RED.rgb),
        Sound(SoundEvents.GOAT_HORN_SOUND_VARIANTS[2].value(), 1F, 1F)
    )
}