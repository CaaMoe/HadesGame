package moe.caa.fabric.hadesgame.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.Entity

val spawnEntityEvent: Event<OnSpawnEntity> = EventFactory.createArrayBacked(
    OnSpawnEntity::class.java
) { callbacks ->
    OnSpawnEntity { entity: Entity ->
        for (callback in callbacks) {
            callback.onSpawnEntity(entity)
        }
    }
}


fun interface OnSpawnEntity {
    fun onSpawnEntity(entity: Entity)

    companion object {
        fun callEvent(entity: Entity) {
            spawnEntityEvent.invoker().onSpawnEntity(entity)
        }
    }
}