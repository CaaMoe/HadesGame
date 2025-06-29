package moe.caa.fabric.hadesgame.stage

import moe.caa.fabric.hadesgame.GameCore
import moe.caa.fabric.hadesgame.gameevent.AbstractGameEvent
import moe.caa.fabric.hadesgame.handler.ScoreboardHandler
import moe.caa.fabric.hadesgame.util.*
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.world.GameMode
import java.awt.Color
import java.time.LocalDateTime

data object GamingStage : AbstractStage() {
    override val stageName: String = "游戏中"
    override val nextStage: AbstractStage = EndStage

    private var eventCountdownRange = 30..90

    private var tick = 0
    var invincibleCountdown = 0

    var winner: Text? = null

    private var codType: CodType = CodType.ALL
    private var eventCountdown = 0
    private var event = AbstractGameEvent::class.sealedSubclasses.map { it.objectInstance!! }.random()

    enum class CodType {
        ALL {
            override val hideEventName = true
            override val hideCountdown = true
        },
        COUNTDOWN {
            override val hideEventName = false
            override val hideCountdown = true
        },
        NAME {
            override val hideEventName = true
            override val hideCountdown = false
        };

        abstract val hideEventName: Boolean
        abstract val hideCountdown: Boolean
    }

    private fun randomNext() {
        eventCountdown = eventCountdownRange.random()
        codType = CodType.entries.toTypedArray().random()
        event = AbstractGameEvent::class.sealedSubclasses.map { it.objectInstance!! }.random()
    }

    override suspend fun startStage() {
        tick = 0
        invincibleCountdown = 20
        winner = null

        for (player in getPlayers()) {
            player.resetState()
            player.changeGameMode(GameMode.SURVIVAL)
        }

        for (world in GameCore.server.worlds) {
            val border = world.worldBorder
            border.size = 900.0
            border.interpolateSize(900.0, 1.0, 1000 * 60 * 15)
        }
    }

    override suspend fun tickStage() {
        tick++

        if (invincibleCountdown < 0 && eventCountdown <= 2) {
            if (tick % 2 == 0) {
                SoundEvents.BLOCK_NOTE_BLOCK_BIT.value().broadcast(1000F, 2.0F)
            }
        }

        if (tick % 20 == 0) {
            invincibleCountdown--


            if (invincibleCountdown > 0) {
                ScoreboardHandler.updateContents(contents = buildList {
                    add(Text.literal(DATE_FORMAT.format(LocalDateTime.now())).withColor(Color.LIGHT_GRAY.rgb))
                    add(Text.literal(" "))
                    add(Text.literal(" 请做好准备, 无敌  "))
                    add(
                        Text.literal(" 时间还剩余")
                            .append(Text.literal(invincibleCountdown.toString()).withColor(Color.RED.rgb)).append("秒.")
                    )
                    add(Text.literal(" "))

                    add(
                        Text.literal(" 边界: ").append(
                            Text.literal(GameCore.server.overworld.worldBorder.size.toInt().toString())
                                .withColor(Color.RED.rgb)
                        )
                    )
                    add(
                        Text.literal(" 存活: ")
                            .append(
                                Text.literal(getPlayers().filter { it.gameMode != GameMode.SPECTATOR }.size.toString())
                                    .withColor(Color.RED.rgb)
                            )
                    )

                    add(Text.literal(" "))
                    add(Text.literal("(╯°□°)╯").withColor(Color.YELLOW.rgb))
                })
                Text.literal("无敌时间还剩余 ").withColor(Color.LIGHT_GRAY.rgb)
                    .append(Text.literal(invincibleCountdown.toString()).withColor(Color.RED.rgb))
                    .append(Text.literal(" 秒").withColor(Color.LIGHT_GRAY.rgb)).broadcastOverlay()

            } else if (invincibleCountdown == 0) {
                SoundEvents.GOAT_HORN_SOUNDS[2].value().broadcast(1000F, 1F)
                Text.literal("鲨了他们!").withColor(Color.RED.rgb).broadcastOverlay()
                randomNext()
            } else {
                eventCountdown--
                if (eventCountdown <= 0) {
                    event.callEvent()
                    randomNext()
                }
                ScoreboardHandler.updateContents(contents = buildList {
                    add(Text.literal(DATE_FORMAT.format(LocalDateTime.now())).withColor(Color.LIGHT_GRAY.rgb))
                    add(Text.literal(" "))
                    add(Text.literal(" 下一事件:"))

                    fun Int.countdownFormat() = String.format("%02d:%02d", this / 60, this % 60)

                    if (eventCountdown > 10) {
                        add(
                            Text.literal("   ")
                                .append(
                                    Text.literal(if (codType.hideEventName) "§kCaaMoe" else event.eventName)
                                        .withColor(Color.GREEN.rgb)
                                )
                                .append(Text.literal("  ").withColor(Color.GREEN.rgb))
                                .append(
                                    Text.literal(if (codType.hideCountdown) "§k00:10" else eventCountdown.countdownFormat())
                                        .withColor(Color.LIGHT_GRAY.rgb)
                                )
                        )
                    } else {
                        add(
                            Text.literal("   ")
                                .append(Text.literal(event.eventName).withColor(Color.GREEN.rgb))
                                .append(Text.literal("  ").withColor(Color.GREEN.rgb))
                                .append(Text.literal(eventCountdown.countdownFormat()).withColor(Color.LIGHT_GRAY.rgb))
                        )
                    }
                    add(Text.literal(" "))
                    add(
                        Text.literal(" 边界: ").append(
                            Text.literal(GameCore.server.overworld.worldBorder.size.toInt().toString())
                                .withColor(Color.RED.rgb)
                        )
                    )
                    add(
                        Text.literal(" 存活: ")
                            .append(
                                Text.literal(getPlayers().filter { it.gameMode != GameMode.SPECTATOR }.size.toString())
                                    .withColor(Color.RED.rgb)
                            )
                    )
                    add(Text.literal(" "))
                    add(Text.literal("(╯°□°)╯").withColor(Color.YELLOW.rgb))
                })
            }
        }
    }

    override suspend fun shouldEndStage(): Boolean {
        if (tick % 20 == 0) {
            val playerEntities = getPlayers().filter { it.gameMode != GameMode.SPECTATOR }
            if (playerEntities.size <= 1) {
                if (playerEntities.isNotEmpty()) {
                    winner = playerEntities.first().name
                }
                return true
            }
        }

        return false
    }

    override suspend fun endStage() {
        AbstractGameEvent::class.sealedSubclasses.map { it.objectInstance!! }.map { it.endEvent() }
    }
}