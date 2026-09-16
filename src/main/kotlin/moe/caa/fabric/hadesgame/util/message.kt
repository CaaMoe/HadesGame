package moe.caa.fabric.hadesgame.util

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import java.time.format.DateTimeFormatter
import kotlin.random.Random

data class Sound(val soundEvent: SoundEvent, val volume: Float, val pitch: Float)
data class Message(val chatMessage: Component?, val overlayMessage: Component?, val sound: Sound?)

val EMPTY_MESSAGE = Message(null, null, null)
val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yy")

fun Component.broadcast() {
    for (player in getPlayers()) {
        player.sendSystemMessage(this, false)
    }
}

fun Component.broadcastOverlay() {
    for (player in getPlayers()) {
        player.sendOverlayMessage(this)
    }
}

fun Component.sendOverlay(spe: ServerPlayer) {
    spe.sendOverlayMessage(this)
}


fun Sound.playSoundTo(spe: ServerPlayer) {
    soundEvent.playSound(spe, volume, pitch)
}

fun Sound.broadcast() {
    soundEvent.broadcast(volume, pitch)
}

fun Message.sendTo(spe: ServerPlayer) {
    overlayMessage?.sendOverlay(spe)
    chatMessage?.let { spe.sendSystemMessage(it) }
    sound?.playSoundTo(spe)
}

fun Message.broadcast() {
    overlayMessage?.broadcastOverlay()
    chatMessage?.broadcast()
    sound?.broadcast()
}

fun SoundEvent.playSound(spe: ServerPlayer, volume: Float = 1.0F, pitch: Float = spe.xRot) {
    spe.connection.send(
        ClientboundSoundPacket(
            BuiltInRegistries.SOUND_EVENT.wrapAsHolder(this),
            SoundSource.MASTER,
            spe.x, spe.y, spe.z,
            volume, pitch, Random.nextLong()
        )
    )
}

fun SoundEvent.broadcast(volume: Float, pitch: Float) {
    for (player in getPlayers()) {
        playSound(player, volume, pitch)
    }
}
