package moe.caa.fabric.hadesgame.event

import moe.caa.fabric.hadesgame.event.OnHello.Result.KICK
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.network.ServerLoginNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

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
            if (result is KICK) {
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