package moe.caa.fabric.hadesgame.game.stage

import moe.caa.fabric.hadesgame.game.GameCore
import moe.caa.fabric.hadesgame.game.event.AbstractGameEvent
import moe.caa.fabric.hadesgame.game.event.InternalInvincible
import moe.caa.fabric.hadesgame.game.handler.ScoreboardHandler
import moe.caa.fabric.hadesgame.util.*
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.level.GameType
import java.awt.Color
import kotlin.properties.Delegates
import kotlin.random.Random

data object GamingStage : AbstractStage() {
    override val name = "游戏"
    override val next = EndStage

    var invincible = true

    private val allGameEvents = AbstractGameEvent.fetchAllGameEvents()
    private val extractableEvents = allGameEvents.filter { it.extractable }

    private var eventCountdownRange = 20..70
    private var tickNumber = 0
    private var eventCountdown = 0
    private var nextEvent by Delegates.notNull<AbstractGameEvent>()
    private var codType by Delegates.notNull<CodType>()

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

        randomNextEvent()
        InternalInvincible.call()
    }

    override suspend fun end() {
        for (event in allGameEvents) {
            event.gameStop()
        }
    }

    private fun randomNextEvent() {
        eventCountdown = eventCountdownRange.random()
        codType = CodType.entries.toTypedArray().random()
        nextEvent = extractableEvents.random()
    }

    override suspend fun tick() {
        tickNumber++

        if (eventCountdown <= 2) {
            if (tickNumber % 2 == 0) {
                SoundEvents.NOTE_BLOCK_BIT.value().broadcast(1F, 2.0F)
            }
        }

        if (tickNumber % 20 == 0) {
            eventCountdown--
            if (eventCountdown <= 0) {
                if (Random.nextDouble() < 0.7) {
                    nextEvent.call()
                } else {
                    Component.literal("FAKE EVENT").withColor(Color.RED.rgb).broadcastOverlay()
                    SoundEvents.NOTE_BLOCK_DIDGERIDOO.value().broadcast(1F, 1.5F)
                }
                randomNextEvent()
            }

            ScoreboardHandler.updateContents(contents = buildList {
                add(Component.literal(" "))
                add(Component.literal(" 下一事件:"))

                fun Int.countdownFormat() = String.format("%02d:%02d", this / 60, this % 60)

                if (eventCountdown > 10) {
                    add(
                        Component.literal("   ")
                            .append(
                                Component.literal(if (codType.hideEventName) "§kCaaMoe" else nextEvent.name)
                                    .withColor(Color.GREEN.rgb)
                            )
                            .append(Component.literal("  ").withColor(Color.GREEN.rgb))
                            .append(
                                Component.literal(if (codType.hideCountdown) "§k00:10" else eventCountdown.countdownFormat())
                                    .withColor(Color.LIGHT_GRAY.rgb)
                            )
                    )
                } else {
                    add(
                        Component.literal("   ")
                            .append(Component.literal(nextEvent.name).withColor(Color.GREEN.rgb))
                            .append(Component.literal("  ").withColor(Color.GREEN.rgb))
                            .append(Component.literal(eventCountdown.countdownFormat()).withColor(Color.LIGHT_GRAY.rgb))
                    )
                }
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