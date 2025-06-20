package moe.caa.fabric.hadesgame.stage

// 只跑一次的阶段
sealed class AbstractOnlyTickOnceStage : AbstractStage() {
    final override suspend fun startStage() {}
    final override suspend fun shouldEndStage(): Boolean = true
    final override suspend fun endStage() {}
}