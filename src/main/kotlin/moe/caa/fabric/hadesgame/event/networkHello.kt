package moe.caa.fabric.hadesgame.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.network.chat.Component
import net.minecraft.server.network.ServerLoginPacketListenerImpl


val networkHelloEvent: Event<OnHello> = EventFactory.createArrayBacked(
    OnHello::class.java
) { callbacks ->
    OnHello { handler: ServerLoginPacketListenerImpl ->
        for (callback in callbacks) {
            when (val result = callback.onHello(handler)) {
                is OnHello.Result.ALLOWED -> {}
                is OnHello.Result.KICK -> return@OnHello OnHello.Result.KICK(result.reason)
            }
        }
        OnHello.Result.ALLOWED
    }
}

fun interface OnHello {
    fun onHello(handler: ServerLoginPacketListenerImpl): Result

    companion object {
        fun shouldCancel(handler: ServerLoginPacketListenerImpl): Boolean {
            val result = networkHelloEvent.invoker().onHello(handler)
            if (result is Result.KICK) {
                handler.disconnect(result.reason)
                return true
            }
            return false
        }
    }

    sealed interface Result {
        data object ALLOWED : Result
        data class KICK(val reason: Component) : Result
    }
}
