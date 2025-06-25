package moe.caa.fabric.hadesgame.util

import moe.caa.fabric.hadesgame.GameCore
import net.minecraft.server.network.ServerPlayerEntity

fun getPlayers(): List<ServerPlayerEntity> = GameCore.server.playerManager.playerList

fun ServerPlayerEntity.resetState() {
    dismountVehicle()
    removeAllPassengers()
    setVelocity(0.0, 0.0, 0.0)
    inventory.clear()
    inventory.selectedSlot = 0
    enderChestInventory.clear()

    experienceLevel = 0
    experienceProgress = 0.toFloat()
    totalExperience = 0


    heal()
    sendAbilitiesUpdate()
}

fun ServerPlayerEntity.heal() {
    hungerManager.foodLevel = 20
    hungerManager.saturationLevel = 5.0f
    health = maxHealth
    kotlin.runCatching {
        statusEffects.forEach { removeStatusEffect(it.effectType) }
    }
}