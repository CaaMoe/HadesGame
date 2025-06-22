package moe.caa.fabric.hadesgame.util

import moe.caa.fabric.hadesgame.GameCore
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvent
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

fun Text.sendOverlay(spe: ServerPlayerEntity) {
    spe.sendMessage(this, true)
}

fun SoundEvent.playSound(spe: ServerPlayerEntity, volume: Float = 1.0F, pitch: Float = spe.pitch) {
    spe.playSound(this, volume, pitch)
}

fun SoundEvent.broadcast(volume: Float, pitch: Float) {
    for (player in getPlayers()) {
        playSound(player, volume, pitch)
    }
}