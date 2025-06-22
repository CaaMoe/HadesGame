package moe.caa.fabric.hadesgame.stage

import moe.caa.fabric.hadesgame.GameCore

// 阶段
sealed class AbstractStage {
    // 阶段名称
    abstract val stageName: String

    // 初始化
    open fun init() {}

    // 开始阶段
    open suspend fun startStage() {}

    // 阶段tick
    open suspend fun tickStage() {}

    // 是否要结束这个阶段
    open suspend fun shouldEndStage(): Boolean = false

    // 结束阶段
    // 他可能不看 shouldEndStage 的结果就执行了
    open suspend fun endStage() {}

    // 下一阶段
    abstract val nextStage: AbstractStage

    fun isCurrentRunStage(): Boolean = GameCore.currentStage == this
}