package moe.caa.fabric.hadesgame.game.handler

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import moe.caa.fabric.hadesgame.game.GameCore
import moe.caa.fabric.hadesgame.util.DATE_FORMAT
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.numbers.BlankFormat
import net.minecraft.server.ServerScoreboard
import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.scores.Objective
import net.minecraft.world.scores.ScoreHolder
import net.minecraft.world.scores.criteria.ObjectiveCriteria
import java.awt.Color
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.milliseconds

data object ScoreboardHandler {
    private const val SCORE_HOLDER_PREFIX = "hades_game_line_"

    private lateinit var serverScoreboard: ServerScoreboard
    private lateinit var scoreboardObjective: Objective

    private val scoreHolder = HashMap<String, ScoreHolder>()

    private val dynamicTitleContents = buildList {
        val title = "阴间游戏"
        val appendFixedTitle = Component.literal("v4").withStyle(Style.EMPTY.withBold(false).withColor(Color.GRAY.rgb))

        val originColorRGB = Color.YELLOW.rgb
        val whiteColorRGB = Color.WHITE.rgb
        val flashColorRGB = Color.ORANGE.rgb

        for (index in title.indices) {
            add(
                Component.empty()
                    .append(
                        Component.literal(title.substring(0, index))
                            .setStyle(Style.EMPTY.withBold(true).withColor(whiteColorRGB))
                    )
                    .append(
                        Component.literal(title.substring(index, index + 1))
                            .setStyle(Style.EMPTY.withBold(true).withColor(flashColorRGB))
                    )
                    .append(
                        Component.literal(title.substring(index + 1))
                            .setStyle(Style.EMPTY.withBold(true).withColor(originColorRGB))
                    )
                    .append(appendFixedTitle)
            )
        }

        val whiteTitle = Component.literal(title).setStyle(Style.EMPTY.withBold(true).withColor(whiteColorRGB))
            .append(appendFixedTitle)
        val originTitle = Component.literal(title).setStyle(Style.EMPTY.withBold(true).withColor(originColorRGB))
            .append(appendFixedTitle)
        repeat(3) {
            add(whiteTitle)
            add(whiteTitle)
            add(originTitle)
            add(originTitle)
        }
    }

    fun init() {
        serverScoreboard = GameCore.server.scoreboard


        serverScoreboard.getObjective("hades_game_scoreboard")?.also { board ->
            serverScoreboard.removeObjective(board)
        }
        scoreHolder.clear()

        scoreboardObjective = serverScoreboard.addObjective(
            "hades_game_scoreboard",
            ObjectiveCriteria.DUMMY,
            Component.empty(),
            ObjectiveCriteria.RenderType.INTEGER,
            true,
            BlankFormat.INSTANCE
        )

        serverScoreboard.setDisplayObjective(DisplaySlot.SIDEBAR, scoreboardObjective)

        setupDynamicTitle()
    }

    private fun setupDynamicTitle() {
        GameCore.coroutineScope.launch {
            while (isActive) {
                dynamicTitleContents.forEach {
                    delay(200.milliseconds)
                    scoreboardObjective.displayName = it
                }
                delay(5000.milliseconds)
            }
        }
    }

    fun updateContents(contents: List<Component>) {
        val reversedContents = contents.toMutableList().apply {
        }.asReversed().apply {
            this.add(Component.literal(DATE_FORMAT.format(LocalDateTime.now())).withColor(Color.LIGHT_GRAY.rgb))
        }

        for ((index, content) in reversedContents.withIndex()) {
            val holderName = "$SCORE_HOLDER_PREFIX$index"
            val scoreAccess = serverScoreboard.getOrCreatePlayerScore(
                scoreHolder.getOrPut(holderName) { ScoreHolder.forNameOnly(holderName) },
                scoreboardObjective,
                true
            )

            scoreAccess.set(index)
            scoreAccess.display(content)
            scoreAccess.numberFormatOverride(BlankFormat.INSTANCE)
        }

        val activeHolderNames = reversedContents.indices.mapTo(HashSet()) { index ->
            "$SCORE_HOLDER_PREFIX$index"
        }

        val shouldRemoveHolderNames = scoreHolder.keys.filter { it !in activeHolderNames }
        shouldRemoveHolderNames.forEach { holderName ->
            scoreHolder.remove(holderName)?.also { holder ->
                serverScoreboard.resetSinglePlayerScore(holder, scoreboardObjective)
            }
        }
    }
}
