package moe.caa.fabric.hadesgame.game.stage

import moe.caa.fabric.hadesgame.game.GameCore

sealed class AbstractStage {
    abstract val name: String

    open suspend fun start() {}

    open suspend fun tick() {}

    open suspend fun shouldEnd(): Boolean = false

    open suspend fun end() {}

    abstract val next: AbstractStage

    fun isCurrentRunningStage() = GameCore.currentRunningStage == this
}