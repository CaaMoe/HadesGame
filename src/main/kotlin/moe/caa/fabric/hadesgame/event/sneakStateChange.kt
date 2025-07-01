package moe.caa.fabric.hadesgame.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.server.network.ServerPlayerEntity


val sneakStateChangeEvent: Event<OnSneakStateChange> = EventFactory.createArrayBacked(
    OnSneakStateChange::class.java
) { callbacks ->
    OnSneakStateChange { player: ServerPlayerEntity, newSneakingState: Boolean ->
        for (callback in callbacks) {
            callback.onSneakStateChange(player, newSneakingState)
        }
    }
}

fun interface OnSneakStateChange {
    fun onSneakStateChange(player: ServerPlayerEntity, newSneakingState: Boolean)

    companion object {
        fun trigger(player: ServerPlayerEntity, newSneakingState: Boolean) {
            sneakStateChangeEvent.invoker().onSneakStateChange(player, newSneakingState)
        }
    }
}