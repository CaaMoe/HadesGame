package moe.caa.fabric.hadesgame.gameevent

import moe.caa.fabric.hadesgame.GameCore
import moe.caa.fabric.hadesgame.util.getLocation
import moe.caa.fabric.hadesgame.util.teleport
import net.minecraft.world.GameMode
import java.util.*

data object SwapLocation : AbstractGameEvent() {
    override val eventName = "交换位置"

    override suspend fun callEvent() {
        val players = ArrayList(GameCore.server.playerManager.playerList)
            .filter { it.interactionManager.gameMode != GameMode.SPECTATOR }
            .toMutableList()
            .apply { shuffle() }

        val posList = LinkedList(players.map { it.getLocation() }).apply { offer(remove()) }

        players.forEach {
            val pos = posList.remove()
            it.teleport(pos)
        }
    }
}