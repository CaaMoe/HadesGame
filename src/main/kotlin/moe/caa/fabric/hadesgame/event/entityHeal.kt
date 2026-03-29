package moe.caa.fabric.hadesgame.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.world.entity.LivingEntity

val entityHealEvent: Event<OnEntityHeal> = EventFactory.createArrayBacked(
    OnEntityHeal::class.java
) { callbacks ->
    OnEntityHeal { livingEntity: LivingEntity, amount: Float ->
        for (callback in callbacks) {
            when (callback.onEntityHeal(livingEntity, amount)) {
                true -> {}
                false -> return@OnEntityHeal false
            }
        }
        return@OnEntityHeal true
    }
}

fun interface OnEntityHeal {
    fun onEntityHeal(livingEntity: LivingEntity, amount: Float): Boolean

    companion object {
        fun shouldContinue(livingEntity: LivingEntity, amount: Float): Boolean {
            return entityHealEvent.invoker().onEntityHeal(livingEntity, amount)
        }
    }
}
