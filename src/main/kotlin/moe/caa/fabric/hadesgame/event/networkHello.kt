package moe.caa.fabric.hadesgame.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.server.network.ServerLoginNetworkHandler
import net.minecraft.text.Text


val networkHelloEvent: Event<OnHello> = EventFactory.createArrayBacked(
    OnHello::class.java
) { callbacks ->
    OnHello { handler: ServerLoginNetworkHandler ->
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
    fun onHello(handler: ServerLoginNetworkHandler): Result

    companion object {
        fun shouldCancel(handler: ServerLoginNetworkHandler): Boolean {
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
        class KICK(val reason: Text) : Result
    }
}
