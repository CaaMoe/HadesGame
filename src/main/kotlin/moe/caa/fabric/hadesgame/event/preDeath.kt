package moe.caa.fabric.hadesgame.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource


val preDeathEvent: Event<OnPreDeath> = EventFactory.createArrayBacked(
    OnPreDeath::class.java
) { callbacks ->
    OnPreDeath { livingEntity: LivingEntity, damageSource: DamageSource ->
        for (callback in callbacks) {
            when (callback.onPreDeath(livingEntity, damageSource)) {
                true -> {
                    return@OnPreDeath true
                }

                false -> {}
            }
        }
        return@OnPreDeath false
    }
}

fun interface OnPreDeath {
    fun onPreDeath(livingEntity: LivingEntity, damageSource: DamageSource): Boolean

    companion object {
        fun shouldCancel(livingEntity: LivingEntity, damageSource: DamageSource): Boolean {
            return preDeathEvent.invoker().onPreDeath(livingEntity, damageSource)
        }
    }
}