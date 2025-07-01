package moe.caa.fabric.hadesgame.gameevent

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.caa.fabric.hadesgame.GameCore
import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.broadcastOverlay
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import java.awt.Color

data object ReverseDamage : AbstractGameEvent() {
    override val eventName = "反向伤害"

    private var activeJob: Job? = null
    private var handlingEntity = ArrayList<Entity>()

    override fun initEvent() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register { entity, source, amount ->
            val attacker = source.attacker as? LivingEntity ?: return@register true

            if (handlingEntity.contains(entity)) return@register true
            if (handlingEntity.contains(attacker)) return@register true

            if (activeJob?.isActive == true) {
                handlingEntity.add(entity)
                handlingEntity.add(attacker)
                attacker.damage((entity.world as ServerWorld), entity.world.damageSources.generic(), amount)
                handlingEntity.remove(entity)
                handlingEntity.remove(attacker)
                return@register false
            }
            return@register true
        }
    }

    override suspend fun callEvent() {
        activeJob?.cancel()

        activeJob = GameCore.coroutineScope.launch {
            // 30 秒
            delay(1000 * 30)
            Text.literal("反向伤害效果已失效").withColor(Color.RED.rgb).broadcastOverlay()
            SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP.broadcast(1F, 1F)
        }
        Text.literal("反向伤害效果已生效").withColor(Color.GREEN.rgb).broadcastOverlay()
        SoundEvents.UI_TOAST_CHALLENGE_COMPLETE.broadcast(1F, 1F)
    }

    override suspend fun endEvent() {
        handlingEntity.clear()
        activeJob?.cancel()
    }
}