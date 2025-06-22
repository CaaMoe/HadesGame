package moe.caa.fabric.hadesgame

import kotlinx.coroutines.*
import moe.caa.fabric.hadesgame.handler.ScoreboardHandler
import moe.caa.fabric.hadesgame.stage.AbstractStage
import moe.caa.fabric.hadesgame.stage.InitStage
import moe.caa.fabric.hadesgame.util.ThreadExecutorDispatcher
import net.minecraft.server.MinecraftServer
import org.slf4j.Logger
import kotlin.math.max
import kotlin.reflect.full.createInstance

object GameCore {
    lateinit var server: MinecraftServer
    lateinit var logger: Logger
    lateinit var coroutineScope: CoroutineScope

    var currentStage: AbstractStage = InitStage

    fun setup(logger: Logger, server: MinecraftServer) {
        this.logger = logger
        this.server = server
        this.coroutineScope = CoroutineScope(SupervisorJob() + ThreadExecutorDispatcher(server))

        logger.info("正在加载 阴间游戏V3...")

        ScoreboardHandler.setup()

        AbstractStage::class.sealedSubclasses.forEach { it.createInstance() }

        coroutineScope.launch {
            currentStage.startStage()
            delay(50)
            while (true) {
                runCatching {
                    val startTimeMills = System.currentTimeMillis()
                    tickInPrimaryThread()
                    val endTimeMills = System.currentTimeMillis()
                    val keepTimeMills = endTimeMills - startTimeMills
                    delay(max(0, 50 - keepTimeMills))
                }.onFailure {
                    logger.error("game loop error", it)
                }
            }
        }
    }

    private suspend fun tickInPrimaryThread() {
        currentStage.tickStage()
        if (currentStage.shouldEndStage()) {
            currentStage.endStage()
            logger.info("阶段 ${currentStage.stageName} 已结束")
            currentStage = currentStage.nextStage
            currentStage.startStage()
            logger.info("切换到阶段 ${currentStage.stageName}")
        }
    }

    fun stop() {
        if (::coroutineScope.isInitialized) {
            coroutineScope.cancel()
        }
    }
}
