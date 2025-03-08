package moe.caa.fabric.hadesgame

import moe.caa.fabric.hadesgame.GameState.*
import moe.caa.fabric.hadesgame.gameevent.AbstractGameEvent
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.Vec3d
import org.slf4j.Logger

enum class GameState {
    INIT,
    WAITING,
    READY_STARTING,
    GAMING,
    END,
}

object GameCore {
    private lateinit var server: MinecraftServer
    private lateinit var logger: Logger
    private lateinit var gameEvents: List<AbstractGameEvent>

    private var lobbyPos = Vec3d.ZERO
    private var gameState = INIT
    private var ticks = 0

    fun setup(logger: Logger, server: MinecraftServer) {
        this.logger = logger
        this.server = server
        ServerTickEvents.START_SERVER_TICK.register { tickInPrimary() }

        gameEvents = AbstractGameEvent::class.sealedSubclasses.map { event -> event.objectInstance!! }
        logger.info("正在加载 阴间游戏V3...")
        gameEvents.forEach { gameEvent ->
            logger.info("添加游戏事件: ${gameEvent.eventName}")
        }
    }

    private fun tickInPrimary() {
        ticks++
        when (gameState) {
            INIT -> TODO()
            WAITING -> TODO()
            READY_STARTING -> TODO()
            GAMING -> TODO()
            END -> TODO()
        }
    }
}