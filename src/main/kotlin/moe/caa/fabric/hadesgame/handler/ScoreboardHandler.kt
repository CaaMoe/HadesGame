package moe.caa.fabric.hadesgame.handler

import moe.caa.fabric.hadesgame.GameCore
import net.minecraft.scoreboard.*
import net.minecraft.scoreboard.number.BlankNumberFormat
import net.minecraft.text.Style
import net.minecraft.text.Text
import java.awt.Color


object ScoreboardHandler {
    private lateinit var serverScoreboard: ServerScoreboard
    private lateinit var scoreboardObjective: ScoreboardObjective

    private val scoreHolder = HashMap<String, ScoreHolder>()

    private val scoreboardTitle by lazy {
        Text.empty().append(
            Text.literal("阴间游戏")
                .setStyle(Style.EMPTY.withBold(true).withColor(Color.YELLOW.rgb))
        )
            .append(Text.literal("v3").withColor(Color.LIGHT_GRAY.rgb))
    }

    fun setup() {
        serverScoreboard = GameCore.server.scoreboard
        serverScoreboard.getNullableObjective("hades_game_scoreboard")?.also { board ->
            serverScoreboard.removeObjective(board)
        }

        scoreboardObjective = serverScoreboard.addObjective(
            "hades_game_scoreboard",
            ScoreboardCriterion.DUMMY,
            Text.empty(),
            ScoreboardCriterion.RenderType.INTEGER,
            true,
            null
        )

        serverScoreboard.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, scoreboardObjective)
    }

    fun updateContents(title: Text = scoreboardTitle, contents: List<Text>) {
        val contents = contents.reversed()

        scoreboardObjective.displayName = title

        for ((index, content) in contents.withIndex()) {
            val teamName = index.toString()

            serverScoreboard.getTeam(teamName) ?: serverScoreboard.addTeam(teamName)

            val scoreAccess = serverScoreboard.getOrCreateScore(scoreHolder.getOrPut(teamName) {
                ScoreHolder { teamName }
            }, scoreboardObjective, true)

            scoreAccess.score = index
            scoreAccess.displayText = content
            scoreAccess.setNumberFormat(BlankNumberFormat.INSTANCE)
        }

        val shouldRemoveTeams = serverScoreboard.teams.filter { team ->
            val name = team.name
            val index = name.toIntOrNull() ?: return@filter true
            return@filter index >= contents.size
        }

        shouldRemoveTeams.forEach { team ->
            team.playerList.clear()
            scoreHolder[team.name]?.also { serverScoreboard.removeScore(it, scoreboardObjective) }
            serverScoreboard.removeTeam(team)
        }
    }
}