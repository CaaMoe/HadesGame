package moe.caa.fabric.hadesgame.game

import kotlinx.coroutines.*
import moe.caa.fabric.hadesgame.game.event.AbstractGameEvent
import moe.caa.fabric.hadesgame.game.handler.CommandHandler
import moe.caa.fabric.hadesgame.game.handler.DamageAndDeathHandler
import moe.caa.fabric.hadesgame.game.handler.JoinLeaveHandler
import moe.caa.fabric.hadesgame.game.handler.ScoreboardHandler
import moe.caa.fabric.hadesgame.game.stage.AbstractStage
import moe.caa.fabric.hadesgame.game.stage.MapInitStage
import moe.caa.fabric.hadesgame.util.ThreadExecutorDispatcher
import moe.caa.fabric.hadesgame.util.randomLocationChunkTicketType
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.MinecraftServer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.math.max
import kotlin.properties.Delegates
import kotlin.time.Duration.Companion.milliseconds

data object GameCore {
    lateinit var server: MinecraftServer
    lateinit var coroutineScope: CoroutineScope
    lateinit var logger: Logger
    var currentRunningStage by Delegates.notNull<AbstractStage>()
        private set

    private fun stop() {
        if (::coroutineScope.isInitialized) {
            coroutineScope.cancel()
        }
    }

    private fun setup(server: MinecraftServer) {
        this.server = server
        this.coroutineScope = CoroutineScope(SupervisorJob() + ThreadExecutorDispatcher(this.server))
        this.logger = LoggerFactory.getLogger("HadesGame")

        logger.info("正在加载 阴间游戏v4...")

        ScoreboardHandler.init()
        CommandHandler.init()
        DamageAndDeathHandler.init()
        JoinLeaveHandler.init()

        AbstractStage::class.sealedSubclasses.forEach { it.objectInstance!! }
        val gameEvents = AbstractGameEvent.fetchAllGameEvents()

        logger.info("游戏加载完成, 正在初始化第一轮游戏.")
        startLoop()
    }

    private fun startLoop() {
        coroutineScope.launch {
            currentRunningStage = MapInitStage
            currentRunningStage.start()
            delay(50.milliseconds)

            while (isActive) {
                val startTimeMills = System.currentTimeMillis()
                runCatching {
                    currentRunningStage.tick()
                    if (currentRunningStage.shouldEnd()) {
                        currentRunningStage.end()
                        logger.info("阶段 ${currentRunningStage.name} 结束, 切换到下一阶段 ${currentRunningStage.next.name}")
                        currentRunningStage = currentRunningStage.next
                        currentRunningStage.start()
                    }

                }.onFailure {
                    if (it is CancellationException) throw it
                    logger.error("game loop error", it)
                }
                val endTimeMills = System.currentTimeMillis()
                val keepTimeMills = endTimeMills - startTimeMills
                delay(max(0, 50 - keepTimeMills).milliseconds)
            }
        }
    }

    fun init() {
        randomLocationChunkTicketType // init chunk ticket
        ServerLifecycleEvents.SERVER_STARTED.register { setup(it) }
        ServerLifecycleEvents.SERVER_STOPPING.register { stop() }
    }
}