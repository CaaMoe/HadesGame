package moe.caa.fabric.hadesgame.game.stage

import kotlinx.coroutines.delay
import moe.caa.fabric.hadesgame.game.GameCore
import moe.caa.fabric.hadesgame.game.handler.ScoreboardHandler
import moe.caa.fabric.hadesgame.util.*
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.level.GameType
import net.minecraft.world.level.block.Blocks
import java.awt.Color
import kotlin.jvm.optionals.getOrNull
import kotlin.time.Duration.Companion.milliseconds

data object WaitReadyStage : AbstractStage() {
    override val name = "等待开始"
    override val next = GamingStage

    private const val DEFAULT_COUNTDOWN_TO_START = 15
    private var shouldEnd = false
    private var tickNumber = 0
    private var countdownStarting = false
    private var countdownToStart = DEFAULT_COUNTDOWN_TO_START

    private var lessPlayerWrnMessage = Component.literal("游戏人数不足, 将暂停开始游戏!").withColor(Color.RED.rgb)
    private var lessPlayerScoreboardContents = buildList {
        add(Component.literal(" "))
        add(Component.literal(" 至少需要").append(Component.literal("2").withColor(Color.RED.rgb)).append("名玩家 "))
        add(Component.literal(" 才能进行游戏, 请 "))
        add(Component.literal(" 等待或邀请更多的 "))
        add(Component.literal(" 玩家加入游戏! "))
        add(Component.literal(" "))
    }

    private var tipTriggerStartScoreboardContents = buildList {
        add(Component.literal(" "))
        add(Component.literal(" 当所有玩家准备好 "))
        add(Component.literal(" 后, 可输入经验条 "))
        add(Component.literal(" 上方提示的指令以 "))
        add(Component.literal(" 开启一场游戏. "))
    }

    private var tipTriggerStartMessage = Component.literal("输入指令 ").withColor(Color.LIGHT_GRAY.rgb)
        .append(Component.literal("/game start ").withColor(0XFFD700)) // GOLD
        .append(
            Component.literal("以开始一局新游戏.").withColor(Color.LIGHT_GRAY.rgb)
        )

    private var triggeredCountdownToStartMessage =
        Component.literal("请做好准备, 游戏将在 ").withColor(Color.LIGHT_GRAY.rgb)
            .append(Component.literal("$DEFAULT_COUNTDOWN_TO_START ").withColor(0XFFD700)) // GOLD
            .append(Component.literal("秒后开始!")).withColor(Color.LIGHT_GRAY.rgb)

    override suspend fun start() {
        shouldEnd = false
        tickNumber = 0
        countdownStarting = false

        for (player in getPlayers()) {
            player.teleport(MapInitStage.lobbySpawnLoc)
            player.setGameMode(GameType.ADVENTURE)
            player.resetState()
        }

        for (level in GameCore.server.allLevels) {
            val border = level.worldBorder
            border.setCenter(0.0, 0.0)
            border.size = 29999984.0
        }
    }

    override suspend fun tick() {
        tickNumber++

        if (tickNumber % 20 == 0) {
            getPlayers().forEach { it.heal() }

            GameCore.server.allLevels.forEach { level ->
                level.dimensionType().defaultClock().getOrNull()?.let { clock ->
                    level.clockManager().setTotalTicks(clock, 0)
                }
                level.resetWeatherCycle()
            }

            // 不足人数
            if (getPlayers().size < 2) {
                if (countdownStarting) {
                    countdownStarting = false
                    lessPlayerWrnMessage.broadcast()
                    SoundEvents.VILLAGER_NO.broadcast(1.toFloat(), 0.8.toFloat())
                }
                ScoreboardHandler.updateContents(lessPlayerScoreboardContents)
            } else {
                // 没有宣布启动游戏
                if (!countdownStarting) {
                    tipTriggerStartMessage.broadcastOverlay()

                    ScoreboardHandler.updateContents(buildList {
                        addAll(tipTriggerStartScoreboardContents)
                        add(Component.literal(" "))
                        add(
                            Component.literal("存活: ").append(
                                Component.literal(getActivePlayers().size.toString()).withColor(Color.GREEN.rgb)
                            )
                        )
                        add(Component.literal(" "))
                    })

                } else {
                    countdownToStart--

                    ScoreboardHandler.updateContents(buildList {
                        add(Component.literal(" "))
                        add(Component.literal(" 请注意, 游戏将 "))

                        add(
                            Component.literal(" 在")
                                .append(Component.literal(countdownToStart.toString()).withColor(0XFFD700))
                                .append("秒后开始, ").withColor(Color.WHITE.rgb)
                        )

                        add(Component.literal(" 做好准备! "))
                        add(Component.literal(" "))
                        add(
                            Component.literal("存活: ").append(
                                Component.literal(getActivePlayers().size.toString()).withColor(Color.GREEN.rgb)
                            )
                        )
                        add(Component.literal(" "))
                    })

                    Component.literal("游戏将在 ").withColor(Color.LIGHT_GRAY.rgb)
                        .append(Component.literal(countdownToStart.toString()).withColor(0XFFD700)) // GOLD
                        .append(Component.literal(" 秒后开始.").withColor(Color.LIGHT_GRAY.rgb)).broadcastOverlay()

                    if (countdownToStart == 0) {
                        shouldEnd = true
                        // break
                    }
                }
            }
        }
    }

    override suspend fun shouldEnd() = shouldEnd

    fun triggerToStartCountdown(): Boolean {
        if (getPlayers().size < 2) return false
        if (countdownStarting) return false

        countdownToStart = DEFAULT_COUNTDOWN_TO_START + 1
        countdownStarting = true
        SoundEvents.EXPERIENCE_ORB_PICKUP.broadcast(1.0F, 1.1F)
        triggeredCountdownToStartMessage.broadcast()
        Component.empty().broadcastOverlay()
        return true
    }

    override suspend fun end() {
        ScoreboardHandler.updateContents(contents = buildList {
            add(Component.literal(" "))
            add(Component.literal(" 笼子即将打开 "))
            add(Component.literal(" "))
            add(
                Component.literal("存活: ")
                    .append(Component.literal(getActivePlayers().size.toString()).withColor(Color.GREEN.rgb))
            )
            add(Component.literal(" "))
        })

        repeat(24) {
            SoundEvents.NOTE_BLOCK_BIT.value().broadcast(1F, 2F)
            Component.literal("笼子即将打开").withColor(Color.LIGHT_GRAY.rgb).broadcastOverlay()
            delay(100.milliseconds)
        }
        delay(100.milliseconds)
        SoundEvents.GENERIC_EXPLODE.value().broadcast(1F, 0F)

        for (viewer in getPlayers()) {
            for (pos in getPlayers().map { it.position() }) {
                viewer.level().sendParticles(
                    viewer,
                    ParticleTypes.EXPLOSION_EMITTER,
                    false,
                    false,
                    pos.x,
                    pos.y,
                    pos.z, 1, 0.0, 0.0, 0.0, 0.0
                )
            }
        }

        MapInitStage.placeLobbyBlock(Blocks.AIR.defaultBlockState())
    }
}