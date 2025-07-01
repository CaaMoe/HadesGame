package moe.caa.fabric.hadesgame.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.Entity


val entityFlagChangeEvent: Event<OnEntityFlagChange> = EventFactory.createArrayBacked(
    OnEntityFlagChange::class.java
) { callbacks ->
    OnEntityFlagChange { entity: Entity, mask: Int, value: Boolean ->
        for (callback in callbacks) {
            when (callback.onEntityFlagChange(entity, mask, value)) {
                true -> {}
                false -> return@OnEntityFlagChange false
            }
        }
        return@OnEntityFlagChange true
    }
}

fun interface OnEntityFlagChange {
    fun onEntityFlagChange(entity: Entity, mask: Int, value: Boolean): Boolean

    companion object {
        fun shouldContinue(entity: Entity, mask: Int, value: Boolean): Boolean {
            return entityFlagChangeEvent.invoker().onEntityFlagChange(entity, mask, value)
        }
    }
}