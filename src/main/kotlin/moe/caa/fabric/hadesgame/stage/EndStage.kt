package moe.caa.fabric.hadesgame.stage

import moe.caa.fabric.hadesgame.GameCore
import moe.caa.fabric.hadesgame.handler.ScoreboardHandler
import moe.caa.fabric.hadesgame.util.DATE_FORMAT
import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.broadcastOverlay
import net.minecraft.entity.Entity
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import java.awt.Color
import java.time.LocalDateTime

data object EndStage : AbstractStage() {
    override val stageName = "结束"
    override val nextStage = InitStage


    private var tick = 0
    private var countdown = 0

    override suspend fun startStage() {
        tick = 0
        countdown = 15

        for (world in GameCore.server.worlds) {
            for (entity in world.iterateEntities()) {
                entity.remove(Entity.RemovalReason.KILLED)
            }
        }

        SoundEvents.GOAT_HORN_SOUNDS[1].value().broadcast(1000F, 1F)

        val winner = GamingStage.winner
        if (winner == null) {
            Text.literal("游戏结束, 这局没有人获胜").withColor(Color.RED.rgb).broadcast()
        } else {
            Text.literal("游戏结束, 最后的赢家是: ").withColor(Color.RED.rgb)
                .append(Text.literal(winner.literalString).withColor(Color.WHITE.rgb)).broadcast()
        }
    }

    override suspend fun tickStage() {
        if (tick++ % 20 == 0) {
            countdown--

            ScoreboardHandler.updateContents(contents = buildList {
                add(Text.literal(DATE_FORMAT.format(LocalDateTime.now())).withColor(Color.LIGHT_GRAY.rgb))
                add(Text.literal(" "))
                add(Text.literal("   游戏结束    "))

                val winner = GamingStage.winner
                if (winner == null) {
                    add(Text.literal(" 这局没有人获胜 "))
                } else {
                    add(Text.literal(" 这局的赢家是 "))
                    add(Text.literal("  " + winner.literalString + " ").withColor(Color.RED.rgb))
                }

                add(Text.literal(" "))
                add(Text.literal("(╯°□°)╯").withColor(Color.YELLOW.rgb))
            })

            Text.literal("将在 ").withColor(Color.LIGHT_GRAY.rgb)
                .append(Text.literal(countdown.toString()).withColor(Color.RED.rgb))
                .append(Text.literal(" 秒后随机下一轮游戏!").withColor(Color.LIGHT_GRAY.rgb)).broadcastOverlay()
        }
    }

    override suspend fun shouldEndStage(): Boolean {
        return countdown <= 0
    }
}