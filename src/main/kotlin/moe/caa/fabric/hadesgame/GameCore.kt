package moe.caa.fabric.hadesgame

import kotlinx.coroutines.*
import moe.caa.fabric.hadesgame.gameevent.AbstractGameEvent
import moe.caa.fabric.hadesgame.handler.ScoreboardHandler
import moe.caa.fabric.hadesgame.stage.AbstractStage
import moe.caa.fabric.hadesgame.stage.InitStage
import moe.caa.fabric.hadesgame.stage.WaitStage
import moe.caa.fabric.hadesgame.util.ThreadExecutorDispatcher
import net.minecraft.server.MinecraftServer
import org.slf4j.Logger

object GameCore {
    lateinit var server: MinecraftServer
    lateinit var logger: Logger
    lateinit var coroutineScope: CoroutineScope
    lateinit var gameEvents: List<AbstractGameEvent>
    lateinit var stages: List<AbstractStage>

    private var currentStageIndex = -1

    fun currentStage() = stages.getOrNull(currentStageIndex)

    fun setup(logger: Logger, server: MinecraftServer) {
        this.logger = logger
        this.server = server
        this.coroutineScope = CoroutineScope(SupervisorJob() + ThreadExecutorDispatcher(server))

        gameEvents = AbstractGameEvent::class.sealedSubclasses.map { event -> event.objectInstance!! }
        logger.info("正在加载 阴间游戏V3...")
        gameEvents.forEach { gameEvent ->
            logger.info("添加游戏事件: ${gameEvent.eventName}")
        }

        stages = listOf(
            InitStage,
            WaitStage
        )

        ScoreboardHandler.setup()

        coroutineScope.launch {
            nextStage()
            while (true) {
                runCatching {
                    delay(50)
                    tickInPrimaryThread()
                }.onFailure {
                    logger.error("game loop error", it)
                }
            }
        }
    }

    private suspend fun tickInPrimaryThread() {
        stages.first().startStage()

        stages[currentStageIndex].apply {
            tickStage()
            if (shouldEndStage()) {
                nextStage()
            }
        }
    }

    private suspend fun nextStage() {
        val prevStage = currentStage()
        val nextStage = stages.getOrNull(++currentStageIndex).let {
            if (it == null) {
                currentStageIndex = 0
                return@let stages[currentStageIndex]
            }
            return@let it
        }
        if (prevStage != null) {
            prevStage.endStage()
            logger.info("阶段 ${prevStage.stageName} 已结束")
        }
        nextStage.startStage()
        logger.info("切换到下一阶段 ${nextStage.stageName}")
    }

    fun stop() {
        coroutineScope.cancel()
    }
}
