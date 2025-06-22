package moe.caa.fabric.hadesgame.main

import moe.caa.fabric.hadesgame.GameCore
import moe.caa.fabric.hadesgame.util.randomLocationChunkTicketType
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class HadesGame : DedicatedServerModInitializer {
    private val logger: Logger = LoggerFactory.getLogger("HadesGame")

    override fun onInitializeServer() {
        randomLocationChunkTicketType

        // 启动游戏
        ServerLifecycleEvents.SERVER_STARTED.register {
            GameCore.setup(logger, it)
        }
        // 结束游戏
        ServerLifecycleEvents.SERVER_STOPPING.register {
            GameCore.stop()
        }
    }
}

