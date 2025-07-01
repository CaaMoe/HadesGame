package moe.caa.fabric.hadesgame.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.LivingEntity


val entityLivingFlagChangeEvent: Event<OnEntityLivingFlagChange> = EventFactory.createArrayBacked(
    OnEntityLivingFlagChange::class.java
) { callbacks ->
    OnEntityLivingFlagChange { livingEntity: LivingEntity, mask: Int, value: Boolean ->
        for (callback in callbacks) {
            when (callback.onEntityLivingFlagChange(livingEntity, mask, value)) {
                true -> {}
                false -> return@OnEntityLivingFlagChange false
            }
        }
        return@OnEntityLivingFlagChange true
    }
}

fun interface OnEntityLivingFlagChange {
    fun onEntityLivingFlagChange(livingEntity: LivingEntity, mask: Int, value: Boolean): Boolean

    companion object {
        fun shouldContinue(livingEntity: LivingEntity, mask: Int, value: Boolean): Boolean {
            return entityLivingFlagChangeEvent.invoker().onEntityLivingFlagChange(livingEntity, mask, value)
        }
    }
}