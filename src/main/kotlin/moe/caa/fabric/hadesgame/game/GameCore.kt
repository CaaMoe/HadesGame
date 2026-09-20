package moe.caa.fabric.hadesgame.game

import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import moe.caa.fabric.hadesgame.BuildInfo
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
import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.StandardCharsets
import kotlin.math.max
import kotlin.properties.Delegates
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

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

        startCheckUpdater()

        ScoreboardHandler.init()
        CommandHandler.init()
        DamageAndDeathHandler.init()
        JoinLeaveHandler.init()

        // init stage
        AbstractStage::class.sealedSubclasses.forEach { it.objectInstance!! }

        // 清理bossBar
        server.customBossEvents.ids.filter { it.namespace == "hadesgame" }.forEach {
            server.customBossEvents.get(it)?.let { server.customBossEvents.remove(it) }
        }

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


    private fun startCheckUpdater() {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                delay(10.seconds)

                while (isActive) {
                    logger.info("正在检查最新版本...")
                    val latestCommitId = getLatestCommitId()

                    val url = "https://github.com/CaaMoe/HadesGame/tree/${BuildInfo.BRANCH_NAME}"
                    if (latestCommitId == "") {
                        logger.warn("无法获取版本更新, 可前往 $url 手动检查更新.")
                    } else {
                        if (latestCommitId == BuildInfo.COMMIT_ID) {
                            logger.info("当前已是最新版本.")
                        } else {
                            logger.warn("当前版本过期了, 请前往 $url 获取最新版本.")
                        }
                    }

                    delay(2.hours)
                }
            }
        }
    }

    private fun getLatestCommitId(): String {
        return runCatching {
            val apiUrl = "https://api.github.com/repos/CaaMoe/HadesGame/branches/${BuildInfo.BRANCH_NAME}"
            val url = URI(apiUrl).toURL()
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 10000
            conn.readTimeout = 10000

            if (conn.responseCode != 200) {
                return@runCatching ""
            }

            val jsonStr = conn.inputStream.use {
                it.readAllBytes().toString(StandardCharsets.UTF_8)
            }
            conn.disconnect()

            val element = Json.decodeFromString<JsonObject>(jsonStr)
            return@runCatching element["commit"]?.jsonObject?.get("sha")?.jsonPrimitive?.content ?: ""
        }.getOrElse { "" }
    }
}