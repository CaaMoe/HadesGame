package moe.caa.fabric.hadesgame.game.event

import moe.caa.fabric.hadesgame.util.Message
import moe.caa.fabric.hadesgame.util.Sound
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import java.awt.Color

data object ReverseDamage : AbstractSustainGameEvent() {
    override val name = "伤害反转"

    private var handlingEntity = ArrayList<Entity>()

    init {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register { entity, source, amount ->
            if (!eventRunning) return@register true
            val attacker = source.entity as? LivingEntity ?: return@register true

            if (handlingEntity.contains(entity)) return@register true
            if (handlingEntity.contains(attacker)) return@register true

            try {
                handlingEntity.add(attacker)
                handlingEntity.add(entity)

                val level = entity.level() as net.minecraft.server.level.ServerLevel
                attacker.hurtServer(level, level.damageSources().thorns(attacker), amount)
            } finally {
                handlingEntity.remove(attacker)
                handlingEntity.remove(entity)
            }
            return@register false
        }
    }

    override val startMessage = Message(
        Component.literal("伤害反转效果已生效").withColor(Color.GREEN.rgb),
        Component.literal("伤害反转效果已生效").withColor(Color.GREEN.rgb),
        Sound(SoundEvents.SCULK_CLICKING, 1F, 1.5F)
    )

    override val endMessage = Message(
        Component.literal("伤害反转效果已失效").withColor(Color.RED.rgb),
        Component.literal("伤害反转效果已失效").withColor(Color.RED.rgb),
        Sound(SoundEvents.CHAIN_BREAK, 1F, 0.5F)
    )
}