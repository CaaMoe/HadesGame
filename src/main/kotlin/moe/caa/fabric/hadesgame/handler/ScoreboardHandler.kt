package moe.caa.fabric.hadesgame.handler

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.caa.fabric.hadesgame.GameCore
import moe.caa.fabric.hadesgame.GameCore.coroutineScope
import net.minecraft.scoreboard.ScoreboardCriterion
import net.minecraft.scoreboard.ScoreboardDisplaySlot
import net.minecraft.scoreboard.ScoreboardObjective
import net.minecraft.scoreboard.ServerScoreboard
import net.minecraft.text.Style
import net.minecraft.text.Text
import java.awt.Color


object ScoreboardHandler {
    private lateinit var serverScoreboard: ServerScoreboard
    private lateinit var scoreboardObjective: ScoreboardObjective

    fun setup() {
        serverScoreboard = GameCore.server.scoreboard
        scoreboardObjective = serverScoreboard.addObjective(
            "hades_game_scoreboard",
            ScoreboardCriterion.DUMMY,
            Text.empty(),
            ScoreboardCriterion.RenderType.INTEGER,
            true,
            null
        )

        serverScoreboard.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, scoreboardObjective)

        coroutineScope.launch {
            coroutineScope.launch {
                while (true) {
                    runCatching {
                        setReversedContents(
                            Text.empty()
                                .append(
                                    Text.literal("阴间游戏")
                                        .setStyle(Style.EMPTY.withBold(true).withColor(Color.YELLOW.rgb))
                                )
                                .append(
                                    Text.literal("v3").withColor(Color.DARK_GRAY.rgb)
                                ), listOf(
                                "",
                                "§f下一事件:",
                                "§a等待指令  §700:15",
                                "",
                                "§a存活: §c1",
                                "",
                                "§a边界: §c500",
                                "",
                                "§7{5}",
                            ).reversed()
                        )
                        delay(1000)
                    }.onFailure {
                        GameCore.logger.error("game loop error", it)
                    }
                }
            }
        }
    }

    private fun setReversedContents(title: Text, contents: List<String>) {
        scoreboardObjective.displayName = title

        for ((index, content) in contents.withIndex()) {
            val teamName = "§${index.toChar()}"
            val team = serverScoreboard.getTeam(teamName) ?: serverScoreboard.addTeam(teamName)

            team.playerList.add(teamName)
            serverScoreboard.getOrCreateScore({ teamName }, scoreboardObjective, true).score = index
            team.prefix = Text.literal(content)
        }

        val shouldRemoveTeams = serverScoreboard.teams.filter { team ->
            val name = team.name
            if (!name.startsWith("§") || name.length != 2) {
                return@filter true
            }
            val id = name[1].code
            return@filter id >= contents.size
        }

        shouldRemoveTeams.forEach { team ->
            team.playerList.clear()
            serverScoreboard.removeTeam(team)
            serverScoreboard.removeScore({ team.name }, scoreboardObjective)
        }
    }
}