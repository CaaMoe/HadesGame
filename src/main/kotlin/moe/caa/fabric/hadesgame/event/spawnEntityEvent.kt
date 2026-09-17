package moe.caa.fabric.hadesgame.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.world.entity.Entity

val addEntityEvent: Event<OnAddEntity> = EventFactory.createArrayBacked(
    OnAddEntity::class.java
) { callbacks ->
    OnAddEntity { entity: Entity ->
        for (callback in callbacks) {
            callback.onAddEntity(entity)
        }
    }
}


fun interface OnAddEntity {
    fun onAddEntity(entity: Entity)

    companion object {
        fun callEvent(entity: Entity) {
            addEntityEvent.invoker().onAddEntity(entity)
        }
    }
}