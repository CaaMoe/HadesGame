package moe.caa.fabric.hadesgame.stage

import moe.caa.fabric.hadesgame.GameCore
import moe.caa.fabric.hadesgame.event.sneakStateChangeEvent
import moe.caa.fabric.hadesgame.handler.ScoreboardHandler
import moe.caa.fabric.hadesgame.stage.InitStage.lobbyLoc
import moe.caa.fabric.hadesgame.util.*
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.minecraft.text.Text
import net.minecraft.world.GameMode
import java.awt.Color
import java.time.LocalDateTime
import java.util.*

data object WaitStage : AbstractStage() {
    override val stageName = "等待开始指令"
    override val nextStage = InitStage


    var autoStart = false
    var shouldStartGame = false
    private var tick = 0
    private var tipNoPreparedIndex = 0
    private val preparedPlayers = mutableSetOf<UUID>()

    private val tipState: Text = Text.empty()
        .append(Text.literal("三击 ").withColor(Color.LIGHT_GRAY.rgb))
        .append(Text.keybind("key.sneak").withColor(Color.WHITE.rgb))
        .append(Text.literal(" 键可切换等待状态, 当前状态: ").withColor(Color.LIGHT_GRAY.rgb))


    private val tipGlobalState: Text = Text.empty()
        .append(Text.literal("当所有人状态都切换为 ").withColor(Color.LIGHT_GRAY.rgb))
        .append(Text.literal("已准备").withColor(Color.GREEN.rgb))
        .append(Text.literal(" 状态后, 游戏将会自动开始.").withColor(Color.LIGHT_GRAY.rgb))


    private val tipNotPreparedPlayers: Text = Text.empty()
        .append(Text.literal("当前未准备的玩家").withColor(Color.LIGHT_GRAY.rgb))


    private val tipInsufficient: Text = Text.empty()
        .append(Text.literal("游戏人数不足 ").withColor(Color.RED.rgb))
        .append(Text.literal("2").withColor(Color.WHITE.rgb))
        .append(Text.literal(" 人, 无法开始游戏.").withColor(Color.RED.rgb))


    private val changeStateCacheMap = WeakHashMap<UUID, MutableList<Long>>()
    override fun init() {
        ServerPlayerEvents.JOIN.register {
            if (isCurrentRunStage()) {
                it.teleport(lobbyLoc())
            }
        }

        sneakStateChangeEvent.register { player, newSneakingState ->
            if (isCurrentRunStage()) {
                if (newSneakingState) {
                    val currentTimeMillis = System.currentTimeMillis()
                    val longs = changeStateCacheMap.getOrPut(player.uuid) { mutableListOf() }


                    longs.addFirst(currentTimeMillis)

                    while (longs.size > 3) {
                        longs.removeLast()
                    }

                    if (longs.size == 3) {
                        if (longs[2] + 1000 > currentTimeMillis && longs[1] + 1000 > currentTimeMillis) {
                            // 三击Shift
                            if (player.uuid in preparedPlayers) {
                                preparedPlayers.remove(player.uuid)
                                player.sendMessage(
                                    Text.literal("准备状态切换为: ").withColor(Color.LIGHT_GRAY.rgb)
                                        .append(Text.literal("未准备").withColor(Color.RED.rgb))
                                )
                            } else {
                                preparedPlayers.add(player.uuid)
                                player.sendMessage(
                                    Text.literal("准备状态切换为: ").withColor(Color.LIGHT_GRAY.rgb)
                                        .append(Text.literal("已准备").withColor(Color.GREEN.rgb))
                                )
                            }
                            longs.clear()
                        }
                    }
                }
            }
        }
    }

    override suspend fun endStage() {
        changeStateCacheMap.clear()
        preparedPlayers.clear()
    }

    override suspend fun startStage() {
        preparedPlayers.clear()
        changeStateCacheMap.clear()
        tick = 0
        shouldStartGame = false
        GameCore.server.playerManager.playerList.forEach { player ->
            player.teleport(lobbyLoc())
            player.changeGameMode(GameMode.ADVENTURE)
            player.resetState()
        }
    }

    override suspend fun tickStage() {
        tick++

        val players = getPlayers()
        if (players.isEmpty()) return

        if (players.size < 2) {
            if (tick % 10 == 0) {
                ScoreboardHandler.updateContents(contents = buildList {
                    add(Text.literal(DATE_FORMAT.format(LocalDateTime.now())).withColor(Color.LIGHT_GRAY.rgb))
                    add(Text.literal("                    "))
                    add(Text.literal(" 至少需要").append(Text.literal("2").withColor(Color.RED.rgb)).append("名玩家"))
                    add(Text.literal(" 才能进行游戏, 请"))
                    add(Text.literal(" 邀请和等待更多的"))
                    add(Text.literal(" 玩家加入游戏!"))
                    add(Text.literal(" "))
                    add(Text.literal("(╯°□°)╯").withColor(Color.YELLOW.rgb))
                })
            }
            return
        }

        val noPreparedPlayers = players.filter { it.uuid !in preparedPlayers }
        if (noPreparedPlayers.isNotEmpty()) {
            if (tick % 10 == 0) {
                ScoreboardHandler.updateContents(contents = buildList {
                    add(Text.literal(DATE_FORMAT.format(LocalDateTime.now())).withColor(Color.LIGHT_GRAY.rgb))
                    add(Text.literal("                    "))
                    add(Text.literal(" 当所有人状态都"))
                    add(Text.literal(" 切换为").append(Text.literal("已准备").withColor(Color.GREEN.rgb)).append("状"))
                    add(Text.literal(" 态后, 游戏将会"))
                    add(Text.literal(" 自动开始."))
                    add(Text.literal(" "))
                    add(
                        Text.literal("未准备: ").withColor(Color.WHITE.rgb)
                            .append(Text.literal(noPreparedPlayers.size.toString()).withColor(Color.RED.rgb))
                    )
                    add(
                        Text.literal("已加入: ").withColor(Color.WHITE.rgb)
                            .append(Text.literal(players.size.toString()).withColor(Color.YELLOW.rgb))
                    )
                    add(Text.literal(" "))
                    add(Text.literal("(╯°□°)╯").withColor(Color.YELLOW.rgb))
                })

                if (tick % 20 == 0) {
                    if (players.size < 2) {
                        tipInsufficient.broadcastOverlay()
                    } else when (tick / 100 % 3) {
                        0 -> {
                            for (player in players) {
                                tipState.copy()
                                    .append(kotlin.run {
                                        if (player.uuid in preparedPlayers)
                                            Text.literal("已准备").withColor(Color.GREEN.rgb)
                                        else
                                            Text.literal("未准备").withColor(Color.RED.rgb)
                                    })
                                    .sendOverlay(player)
                            }
                        }

                        1 -> {
                            tipGlobalState.broadcastOverlay()
                        }

                        2 -> {
                            tipNotPreparedPlayers.copy()
                                .append(Text.literal("(").withColor(Color.LIGHT_GRAY.rgb))
                                .append(Text.literal(noPreparedPlayers.size.toString()).withColor(Color.RED.rgb))
                                .append(Text.literal("): ").withColor(Color.LIGHT_GRAY.rgb))
                                .append(noPreparedPlayers[tipNoPreparedIndex % noPreparedPlayers.size].name)
                                .broadcastOverlay()
                        }
                    }
                }
            }
        }
    }

    override suspend fun shouldEndStage(): Boolean {
        if (autoStart) return true
        return shouldStartGame
    }
}