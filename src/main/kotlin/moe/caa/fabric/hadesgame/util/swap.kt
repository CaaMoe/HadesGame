package moe.caa.fabric.hadesgame.util

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.GameMode

fun <DATA> eventSwap(
    attributeDataGetter: (ServerPlayerEntity) -> DATA,
    attributeSetter: (ServerPlayerEntity, ServerPlayerEntity, DATA) -> Unit,
) {
    val targets = getPlayers()
        .filter { it.interactionManager.gameMode != GameMode.SPECTATOR }
        .toMutableList()
        .apply { shuffle() }

    val sources = targets.map { it to attributeDataGetter.invoke(it) }.toMutableList().apply {
        add(removeFirst())
    }

    targets.forEach {
        val (player, data) = sources.removeFirst()
        attributeSetter.invoke(it, player, data)
    }
}