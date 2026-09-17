package moe.caa.fabric.hadesgame.game.stage

import moe.caa.fabric.hadesgame.game.GameCore
import moe.caa.fabric.hadesgame.game.event.AbstractGameEvent
import moe.caa.fabric.hadesgame.game.event.InternalInvincible
import moe.caa.fabric.hadesgame.game.handler.ScoreboardHandler
import moe.caa.fabric.hadesgame.util.*
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.level.GameType
import java.awt.Color
import kotlin.random.Random

data object GamingStage : AbstractStage() {
    override val name = "游戏"
    override val next = EndStage

    var invincible = true

    val allGameEvents = AbstractGameEvent.fetchAllGameEvents()
    private val extractableEvents = allGameEvents.filter { it.extractable }

    private var eventCountdownRange = 40..85
    private var tickNumber = 0

    private var currentEvents = emptyList<EventCarrier>()

    data class EventCarrier(
        var event: AbstractGameEvent,
        var eventCountdown: Int,
        var codType: CodType
    ) {
        fun call() {
            if (Random.nextDouble() < 0.8) {
                event.call()
            } else {
                Component.literal("FAKE EVENT").withColor(Color.RED.rgb).broadcastOverlay()
                SoundEvents.NOTE_BLOCK_DIDGERIDOO.value().broadcast(1F, 1.5F)
            }
        }

        fun buildScoreboardContentEntries(): Component {

            fun Int.countdownFormat(): String {
                return String.format("%02d:%02d", this / 60, this % 60)
            }

            var component = Component.literal("   ")

            if (eventCountdown <= 0) {
                component = component.append(
                    Component.literal(event.name)
                        .withColor(Color.GREEN.rgb)
                        .withStyle(Style.EMPTY.withStrikethrough(true))
                )
                component = component.append("  ")
                component = component.append(Component.literal("00:00").withColor(Color.LIGHT_GRAY.rgb))
            } else if (eventCountdown > 10) {
                component = if (codType.hideEventName) {
                    component.append(Component.literal("§kCaaMoe").withColor(Color.GREEN.rgb))
                } else {
                    component.append(Component.literal(event.name).withColor(Color.GREEN.rgb))
                }

                component = component.append("  ")

                component = if (codType.hideCountdown) {
                    component.append(Component.literal("§k00:10").withColor(Color.LIGHT_GRAY.rgb))
                } else {
                    component.append(
                        Component.literal(eventCountdown.countdownFormat()).withColor(Color.LIGHT_GRAY.rgb)
                    )
                }

            } else {
                component = component.append(Component.literal(event.name).withColor(Color.GREEN.rgb))
                component = component.append("  ")
                component = component.append(
                    Component.literal(eventCountdown.countdownFormat()).withColor(Color.LIGHT_GRAY.rgb)
                )
            }

            return component
        }
    }

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

    override suspend fun start() {
        tickNumber = 0
        for (event in allGameEvents) {
            event.gameStart()
        }

        for (player in getPlayers()) {
            player.resetState()
            player.setGameMode(GameType.SURVIVAL)
        }

        for (level in GameCore.server.allLevels) {
            val border = level.worldBorder
            // 10分钟一局
            border.setCenter(MapInitStage.lobbySpawnLoc.x, MapInitStage.lobbySpawnLoc.z)
            border.size = 1000.0
            border.lerpSizeBetween(1000.0, 3.0, 20 * 60 * 10, 0L)
        }

        randomEvents()
        InternalInvincible.call()
    }

    override suspend fun end() {
        for (event in allGameEvents) {
            event.gameStop()
        }
    }

    private fun randomEvents() {
        val shuffled = extractableEvents.shuffled().toMutableList()

        currentEvents = buildList {
            val repeat = 2
            // todo repeat

            repeat(repeat) {
                shuffled.removeLastOrNull()?.also { event ->
                    add(
                        EventCarrier(
                            event,
                            eventCountdownRange.random(),
                            CodType.entries.random()
                        )
                    )
                }
            }
        }
    }

    override suspend fun tick() {
        tickNumber++

        if (tickNumber % 20 == 0) {

            var playedCountdownSound = false
            currentEvents.forEach {
                it.eventCountdown--

                if (it.eventCountdown == 0) {
                    it.call()
                } else if (!playedCountdownSound && it.eventCountdown in 1..5) {
                    SoundEvents.NOTE_BLOCK_HAT.value().broadcast(1F, 1.0F)
                    playedCountdownSound = true
                }
            }

            if (!currentEvents.any { it.eventCountdown > 0 }) {
                randomEvents()
            }

            ScoreboardHandler.updateContents(contents = buildList {
                add(Component.literal(" "))
                add(Component.literal(" 下一事件:"))

                currentEvents.forEach { add(it.buildScoreboardContentEntries()) }

                add(Component.literal(" "))
                add(
                    Component.literal(" 边界: ").append(
                        Component.literal(GameCore.server.overworld().worldBorder.size.toInt().toString())
                            .withColor(Color.GREEN.rgb)
                    )
                )
                add(
                    Component.literal(" 存活: ")
                        .append(
                            Component.literal(getPlayers().count { it.gameMode() != GameType.SPECTATOR }.toString())
                                .withColor(Color.GREEN.rgb)
                        )
                )
                add(Component.literal(" "))
            })
        }
    }

    override suspend fun shouldEnd(): Boolean {
        if (tickNumber % 20 == 0) {
            if (getActivePlayers().size <= 1) {
                return true
            }
        }
        return false
    }
}