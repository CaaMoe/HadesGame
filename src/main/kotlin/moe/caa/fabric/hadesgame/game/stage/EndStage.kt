package moe.caa.fabric.hadesgame.game.stage

import moe.caa.fabric.hadesgame.game.GameCore
import moe.caa.fabric.hadesgame.game.handler.ScoreboardHandler
import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.broadcastOverlay
import moe.caa.fabric.hadesgame.util.getActivePlayers
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import java.awt.Color

data object EndStage : AbstractStage() {
    override val name = "结束"
    override val next = MapInitStage

    private var tickNumber = 0
    private var countdownToNext = 0
    private var winner: ServerPlayer? = null

    override suspend fun shouldEnd() = countdownToNext <= 0

    override suspend fun start() {
        tickNumber = 0
        countdownToNext = 15
        winner = getActivePlayers().singleOrNull()

        runCatching {
            for (level in GameCore.server.allLevels) {
                for (entity in level.allEntities) {
                    if (entity !is Player) {
                        entity.remove(Entity.RemovalReason.KILLED)
                    }
                }
            }
        }

        SoundEvents.GOAT_HORN_SOUND_VARIANTS[1].value().broadcast(1F, 1F)

        if (winner == null) {
            Component.literal("游戏结束, 这局没有人获胜.").withColor(Color.LIGHT_GRAY.rgb).broadcast()
        } else {
            Component.literal("游戏结束, 最后的赢家是: ").withColor(Color.LIGHT_GRAY.rgb)
                .append(Component.literal(winner!!.nameAndId().name).withColor(0XFFD700)).broadcast()
        }
    }

    override suspend fun tick() {
        if (tickNumber++ % 20 == 0) {
            countdownToNext--
            ScoreboardHandler.updateContents(contents = buildList {
                add(Component.literal(" "))
                add(Component.literal("   游戏结束    "))

                if (winner == null) {
                    add(Component.literal(" 这局没有人获胜 "))
                } else {
                    add(Component.literal(" 这局的赢家是 "))
                    add(Component.literal("  " + winner!!.nameAndId().name + " ").withColor(0XFFD700))
                }

                add(Component.literal(" "))
            })

            Component.literal("将在 ").withColor(Color.LIGHT_GRAY.rgb)
                .append(Component.literal(countdownToNext.toString()).withColor(0XFFD700))
                .append(Component.literal(" 秒后随机下一轮游戏!").withColor(Color.LIGHT_GRAY.rgb)).broadcastOverlay()
        }
    }
}