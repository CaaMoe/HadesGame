package moe.caa.fabric.hadesgame.game.event

import kotlin.reflect.KClass

/**
 * 所有游戏事件的统一抽象基类
 */
sealed class AbstractGameEvent {

    companion object {
        internal fun fetchAllGameEvents(): Set<AbstractGameEvent> = buildSet {
            fun KClass<out AbstractGameEvent>.search() {
                this.sealedSubclasses.forEach {
                    it.search()
                }
                this.objectInstance?.also { add(it) }
            }

            AbstractGameEvent::class.sealedSubclasses.forEach {
                it.search()
            }
        }
    }

    /**
     * 是否允许被随机事件随机选中
     */
    open val extractable = true

    /**
     * 事件名称
     */
    abstract val name: String

    /**
     * 当当局游戏开始时调用.
     */
    open fun gameStart() {}

    /**
     * 当事件被触发时调用.
     */
    open fun call() {}

    /**
     * 当当局游戏结束时调用.
     */
    open fun gameStop() {}
}