package moe.caa.fabric.hadesgame.gameevent

import net.minecraft.server.dedicated.MinecraftDedicatedServer

data object SwapLocation : AbstractGameEvent() {
    override val eventName = "斗转星移"

    override fun callEvent() {
        MinecraftDedicatedServer.DEMO_LEVEL_INFO
    }
}