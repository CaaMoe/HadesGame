package moe.caa.fabric.hadesgame.main

import moe.caa.fabric.hadesgame.GameCore
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents
import net.minecraft.util.WorldSavePath
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Files
import kotlin.io.path.walk

class HadesGame : DedicatedServerModInitializer {
    private val logger: Logger = LoggerFactory.getLogger("HadesGameV3")

    override fun onInitializeServer() {
        // 清理世界
        ServerWorldEvents.LOAD.register(ServerWorldEvents.Load { server, world ->
            server.getSavePath(WorldSavePath.ROOT).resolve(world.registryKey.value.path).walk()
                .forEach { path -> Files.delete(path) }
            logger.info("清理世界: ${world.registryKey.value.path}")
            world.savingDisabled = true
        })

        ServerLifecycleEvents.SERVER_STARTED.register {
            GameCore.setup(logger, it)
        }
    }
}

