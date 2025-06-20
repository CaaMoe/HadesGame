package moe.caa.fabric.hadesgame.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.server.network.ServerLoginNetworkHandler
import net.minecraft.text.Text

val asyncHelloEvent: Event<OnHello> = EventFactory.createArrayBacked(
    OnHello::class.java
) { callbacks ->
    OnHello { handler: ServerLoginNetworkHandler ->
        for (callback in callbacks) {
            when (val result = callback.onPreLogin(handler)) {
                is OnHello.Result.ALLOWED -> {}
                is OnHello.Result.KICK -> return@OnHello OnHello.Result.KICK(result.reason)
            }
        }
        OnHello.Result.ALLOWED
    }
}


fun interface OnHello {
    fun onPreLogin(handler: ServerLoginNetworkHandler): Result

    sealed interface Result {
        object ALLOWED : Result
        class KICK(val reason: Text) : Result
    }
}