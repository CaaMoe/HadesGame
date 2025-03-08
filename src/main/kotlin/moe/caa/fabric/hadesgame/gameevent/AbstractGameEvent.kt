package moe.caa.fabric.hadesgame.gameevent

/**
 * 游戏事件
 */
sealed class AbstractGameEvent {
    // 事件名称
    abstract val eventName: String

    // 游戏事件触发时执行
    abstract suspend fun callEvent()

    // 当局游戏结束时执行
    open suspend fun endEvent() {}
}