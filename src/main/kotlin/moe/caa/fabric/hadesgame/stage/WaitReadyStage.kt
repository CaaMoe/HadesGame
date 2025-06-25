package moe.caa.fabric.hadesgame.stage

import kotlinx.coroutines.delay
import moe.caa.fabric.hadesgame.GameCore
import moe.caa.fabric.hadesgame.event.sneakStateChangeEvent
import moe.caa.fabric.hadesgame.handler.ScoreboardHandler
import moe.caa.fabric.hadesgame.util.*
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.minecraft.block.Blocks
import net.minecraft.entity.Entity
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.world.GameMode
import java.awt.Color
import java.time.LocalDateTime
import java.util.*

data object WaitReadyStage : AbstractStage() {
    override val stageName = "等待开始"
    override val nextStage = GamingStage

    private var tick = 0
    private var tipNoPreparedIndex = 0
    private val preparedPlayers = mutableSetOf<UUID>()

    private var scheduleStart = false
    private var scheduleStartCountdown = 5

    private var shouldEndStage = false

    private val tipState: Text = Text.empty()
        .append(Text.literal("双击 ").withColor(Color.LIGHT_GRAY.rgb))
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


    private val changeStateCacheMap = WeakHashMap<UUID, Long>()
    override fun init() {

        ServerPlayerEvents.JOIN.register {
            preparedPlayers.remove(it.uuid)
            if (isCurrentRunStage()) {
                var player = it
                if (player.isDead) {
                    player = GameCore.server.playerManager.respawnPlayer(
                        player,
                        true,
                        Entity.RemovalReason.CHANGED_DIMENSION
                    )
                }

                player.changeGameMode(GameMode.ADVENTURE)
                player.teleport(InitStage.lobbySpawnLoc)
                player.resetState()
            }
        }

        ServerLivingEntityEvents.ALLOW_DAMAGE.register { livingEntity, damageSource, _ ->
            if (isCurrentRunStage()) {
                if (livingEntity is ServerPlayerEntity) {
                    if (livingEntity.world.damageSources.outOfWorld() == damageSource) {
                        livingEntity.teleport(InitStage.lobbySpawnLoc)
                    }
                }
                return@register !isCurrentRunStage()
            }
            return@register true
        }

        ServerLivingEntityEvents.ALLOW_DEATH.register { livingEntity, _, _ ->
            if (isCurrentRunStage()) {
                if (livingEntity is ServerPlayerEntity) {
                    livingEntity.teleport(InitStage.lobbySpawnLoc)
                    livingEntity.resetState()
                    return@register false
                }
            }
            return@register true
        }

        sneakStateChangeEvent.register { player, newSneakingState ->
            if (isCurrentRunStage()) {
                if (newSneakingState) {
                    val currentTimeMillis = System.currentTimeMillis()
                    val lastClick = changeStateCacheMap[player.uuid]
                    if ((lastClick ?: 0) + 500 > currentTimeMillis) {
                        changeStateCacheMap.remove(player.uuid)
                        SoundEvents.UI_BUTTON_CLICK.value().playSound(player, 2F, 1000F)

                        // 三击Shift
                        if (player.uuid in preparedPlayers) {
                            preparedPlayers.remove(player.uuid)
                            player.sendMessage(
                                Text.literal("准备状态切换为: ").withColor(Color.LIGHT_GRAY.rgb)
                                    .append(Text.literal("未准备").withColor(Color.RED.rgb))
                            )

                            if (scheduleStart) {
                                scheduleStart = false
                                player.name.copy().append(
                                    Text.literal("取消了准备状态, 已终止倒计时开始游戏!").withColor(Color.RED.rgb)
                                )
                                    .broadcast()


                                SoundEvents.UI_BUTTON_CLICK.value().playSound(player, 2F, 1000F)
                            }
                        } else {
                            preparedPlayers.add(player.uuid)
                            player.sendMessage(
                                Text.literal("准备状态切换为: ").withColor(Color.LIGHT_GRAY.rgb)
                                    .append(Text.literal("已准备").withColor(Color.GREEN.rgb))
                            )
                        }
                    } else {
                        changeStateCacheMap[player.uuid] = currentTimeMillis
                    }
                }
            }
        }
    }

    override suspend fun endStage() {
        changeStateCacheMap.clear()
        preparedPlayers.clear()

        ScoreboardHandler.updateContents(contents = buildList {
            add(Text.literal(DATE_FORMAT.format(LocalDateTime.now())).withColor(Color.LIGHT_GRAY.rgb))
            add(Text.literal(" "))
            add(Text.literal(" 笼子即将打开 "))
            add(Text.literal(" "))
            add(Text.literal("(╯°□°)╯").withColor(Color.YELLOW.rgb))
        })

        // 塞, 硬塞, 私密马塞
        repeat(24) {
            SoundEvents.BLOCK_NOTE_BLOCK_BIT.value().broadcast(1000F, 2F)
            Text.literal("笼子即将打开").withColor(Color.LIGHT_GRAY.rgb).broadcastOverlay()
            delay(100)
        }
        delay(100)
        SoundEvents.ENTITY_GENERIC_EXPLODE.value().broadcast(1000F, 0F)
        for (viewer in getPlayers()) {
            for (pos in getPlayers().map { it.pos }) {
                viewer.world.spawnParticles(
                    viewer,
                    ParticleTypes.EXPLOSION_EMITTER,
                    false,
                    false,
                    pos.x,
                    pos.y,
                    pos.z,
                    1,
                    0.0,
                    0.0,
                    0.0,
                    0.0
                )
            }
        }

        InitStage.placeLobbyBlock(Blocks.AIR.defaultState)
    }

    override suspend fun startStage() {
        shouldEndStage = false
        scheduleStart = false
        preparedPlayers.clear()
        changeStateCacheMap.clear()
        tick = 0
        GameCore.server.playerManager.playerList.forEach { player ->
            player.teleport(InitStage.lobbySpawnLoc)
            player.changeGameMode(GameMode.ADVENTURE)
            player.resetState()
        }
    }

    override suspend fun tickStage() {
        tick++

        val players = getPlayers()
        if (players.isEmpty()) return

        if (tick % 20 == 0) {
            for (p0 in players) {
                var player = p0
                if (player.isDead) {
                    player = GameCore.server.playerManager.respawnPlayer(
                        player,
                        true,
                        Entity.RemovalReason.CHANGED_DIMENSION
                    )
                    player.teleport(InitStage.lobbySpawnLoc)
                }

                player.heal()
            }

            GameCore.server.overworld.timeOfDay = 1000
            GameCore.server.overworld.resetWeather()
        }

        if (players.size < 2) {
            if (scheduleStart) {
                scheduleStart = false
            }

            if (tick % 10 == 0) {
                ScoreboardHandler.updateContents(contents = buildList {
                    add(Text.literal(DATE_FORMAT.format(LocalDateTime.now())).withColor(Color.LIGHT_GRAY.rgb))
                    add(Text.literal("                    "))
                    add(Text.literal(" 至少需要").append(Text.literal("2").withColor(Color.RED.rgb)).append("名玩家"))
                    add(Text.literal(" 才能进行游戏, 请"))
                    add(Text.literal(" 等待或邀请更多的"))
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
                        Text.literal("已准备: ").withColor(Color.WHITE.rgb)
                            .append(
                                Text.literal(players.filter { it.uuid in preparedPlayers }.size.toString())
                                    .withColor(Color.YELLOW.rgb)
                            )
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
                                .append(noPreparedPlayers[tipNoPreparedIndex++ % noPreparedPlayers.size].name)
                                .broadcastOverlay()
                        }
                    }
                }
            }
        } else {
            if (!scheduleStart) {
                scheduleStart = true
                scheduleStartCountdown = 5

                SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP.broadcast(1000F, 2F)

                Text.literal("所有玩家已准备就绪, 游戏即将开始!").withColor(Color.GREEN.rgb).broadcast()
            }
            if (tick % 20 == 0) {
                scheduleStartCountdown--
                if (scheduleStartCountdown <= 0) {
                    shouldEndStage = true
                }
            }

            if (tick % 10 == 0) {
                ScoreboardHandler.updateContents(contents = buildList {
                    add(Text.literal(DATE_FORMAT.format(LocalDateTime.now())).withColor(Color.LIGHT_GRAY.rgb))
                    add(Text.literal("                        "))
                    add(Text.literal(" 游戏即将开始, 请做").withColor(Color.WHITE.rgb))
                    add(
                        Text.literal(" 好准备, 将在")
                            .append(Text.literal(scheduleStartCountdown.toString()).withColor(Color.RED.rgb))
                            .append("秒后").withColor(Color.WHITE.rgb)
                    )
                    add(Text.literal(" 传送到游戏位置!").withColor(Color.WHITE.rgb))
                    add(Text.literal(" "))
                    add(Text.literal("(╯°□°)╯").withColor(Color.YELLOW.rgb))
                })

                Text.literal("游戏将在 ").withColor(Color.LIGHT_GRAY.rgb)
                    .append(Text.literal(scheduleStartCountdown.toString()).withColor(Color.RED.rgb))
                    .append(" 秒后开始!").withColor(Color.LIGHT_GRAY.rgb)
                    .broadcastOverlay()
            }
        }
    }

    override suspend fun shouldEndStage(): Boolean {
        return shouldEndStage
    }
}