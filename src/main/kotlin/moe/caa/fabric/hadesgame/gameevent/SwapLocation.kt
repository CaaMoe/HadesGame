package moe.caa.fabric.hadesgame.gameevent

import moe.caa.fabric.hadesgame.util.*
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.world.GameMode
import java.awt.Color

data object SwapLocation : AbstractGameEvent() {
    override val eventName = "交换位置"

    override suspend fun callEvent() {
        val players = getPlayers()
            .filter { it.interactionManager.gameMode != GameMode.SPECTATOR }
            .toMutableList()
            .apply { shuffle() }

        val list = players.map { it.name to it.getLocation() }.toMutableList().apply {
            add(removeFirst())
        }

        players.forEach {
            val (name, pos) = list.removeFirst()
            it.teleport(pos)
            Text.literal("你被传送到 ").withColor(Color.LIGHT_GRAY.rgb)
                .append(Text.literal(name.literalString).withColor(Color.WHITE.rgb)).append(" 的位置").sendOverlay(it)
        }
        SoundEvents.ENTITY_FOX_TELEPORT.broadcast(100F, 0F)
    }
}