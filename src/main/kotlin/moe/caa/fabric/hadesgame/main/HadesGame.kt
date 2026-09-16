package moe.caa.fabric.hadesgame.main

import moe.caa.fabric.hadesgame.game.GameCore
import net.fabricmc.api.DedicatedServerModInitializer

class HadesGame : DedicatedServerModInitializer {
    override fun onInitializeServer() {
        GameCore.init()
    }
}