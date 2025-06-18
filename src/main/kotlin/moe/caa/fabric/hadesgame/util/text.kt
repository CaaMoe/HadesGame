package moe.caa.fabric.hadesgame.util

import moe.caa.fabric.hadesgame.GameCore
import net.minecraft.text.Text

fun Text.broadcast() {
    GameCore.server.sendMessage(this)
    for (player in getPlayers()) {
        player.sendMessage(this, false)
    }
}

fun Text.broadcastOverlay() {
    for (player in getPlayers()) {
        player.sendMessage(this, true)
    }
}