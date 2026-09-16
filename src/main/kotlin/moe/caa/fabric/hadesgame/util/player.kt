package moe.caa.fabric.hadesgame.util

import moe.caa.fabric.hadesgame.game.GameCore
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.GameType

fun getPlayers(): List<ServerPlayer> = GameCore.server.playerList.players

fun getActivePlayers(): List<ServerPlayer> = getPlayers().filter {
    it.gameMode() != GameType.SPECTATOR
}

fun ServerPlayer.resetState() {
    stopRiding()
    ejectPassengers()
    setDeltaMovement(0.0, 0.0, 0.0)
    inventory.clearContent()
    enderChestInventory.clearContent()

    experienceLevel = 0
    experienceProgress = 0f
    totalExperience = 0

    heal()
    onUpdateAbilities()
}

fun ServerPlayer.heal() {
    foodData.foodLevel = 20
    foodData.setSaturation(5.0f)
    health = maxHealth
    kotlin.runCatching {
        for (entry in activeEffects.map { it.effect }) {
            removeEffect(entry)
        }
    }
}

fun ServerPlayer.syncInventory() {
    inventory.setChanged()
    inventoryMenu.slotsChanged(inventory)
    containerMenu.broadcastChanges()
}

fun ServerPlayer.copyHotbar(): List<ItemStack> {
    return List(Inventory.getSelectionSize()) { slot ->
        inventory.getItem(slot).copy()
    }
}

fun ServerPlayer.setHotbar(stacks: List<ItemStack>) {
    repeat(Inventory.getSelectionSize()) { slot ->
        inventory.setItem(slot, stacks.getOrNull(slot)?.copy() ?: ItemStack.EMPTY)
    }
    syncInventory()
}

private val armorSlotIndices = listOf(36, 37, 38, 39)

fun ServerPlayer.copyArmorContents(): List<ItemStack> {
    return armorSlotIndices.map { slot ->
        inventory.getItem(slot).copy()
    }
}

fun ServerPlayer.setArmorContents(stacks: List<ItemStack>) {
    armorSlotIndices.forEachIndexed { index, slot ->
        inventory.setItem(slot, stacks.getOrNull(index)?.copy() ?: ItemStack.EMPTY)
    }
    syncInventory()
}
